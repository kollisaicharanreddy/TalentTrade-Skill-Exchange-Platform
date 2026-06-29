import React, { createContext, useState, useEffect, useContext, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { toast } from 'react-toastify';
import { AuthContext } from './AuthContext';
import { chatService } from '../services/chat.service';
import { requestsService } from '../services/requests.service';

export const ChatContext = createContext(null);

export const ChatProvider = ({ children }) => {
  const { user, token, isAuthenticated } = useContext(AuthContext);
  const [conversations, setConversations] = useState([]);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const stompClientRef = useRef(null);
  const subscriptionsRef = useRef({});
  const activeConversationRef = useRef(null);

  // Sync ref to avoid stale state in websocket event handlers
  useEffect(() => {
    activeConversationRef.current = activeConversation;
  }, [activeConversation]);

  // Load conversations list from REST API
  const fetchConversations = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    try {
      console.log("fetchConversations triggered for user:", user);
      
      // 1. Fetch conversations with message history
      const response = await chatService.getConversations(0, 50);
      console.log("getConversations API response:", response);
      let list = [];
      if (response && response.success) {
        list = [...(response.data.content || [])];
      }
      
      // 2. Fetch accepted exchange requests to locate all valid chat partners
      const reqResponse = await requestsService.getAllRequests(0, 100);
      console.log("getAllRequests API response:", reqResponse);
      if (reqResponse && reqResponse.success) {
        const requestsList = reqResponse.data.content || [];
        const acceptedRequests = requestsList.filter(r => r.status === 'ACCEPTED');
        console.log("Filtered accepted requests:", acceptedRequests);
        
        acceptedRequests.forEach(r => {
          const partner = r.sender.id === user.id ? r.receiver : r.sender;
          
          // Verify if this partner already has a conversation history card
          const exists = list.some(c => c.otherUser.id === partner.id);
          if (!exists) {
            list.push({
              otherUser: partner,
              lastMessage: null,
              unreadCount: 0
            });
          }
        });
      }
      
      console.log("Final merged conversations list:", list);
      setConversations(list);
    } catch (error) {
      console.error("Failed to fetch conversations:", error);
    }
  }, [isAuthenticated, user]);

  // Fetch conversations list on load and periodically
  useEffect(() => {
    if (isAuthenticated && user) {
      fetchConversations();
      const interval = setInterval(fetchConversations, 15000);
      return () => clearInterval(interval);
    } else if (!isAuthenticated) {
      setConversations([]);
      setActiveConversation(null);
      setMessages([]);
      disconnectWebSocket();
    }
  }, [isAuthenticated, user, fetchConversations]);

  // Connect to STOMP WebSocket
  const connectWebSocket = () => {
    if (!token) return;
    
    // Disconnect existing if any
    if (stompClientRef.current) {
      disconnectWebSocket();
    }

    try {
      const socket = new SockJS('/ws');
      const client = Stomp.over(socket);
      
      // Mute debug logs for clean console, but keep error reporting
      client.debug = () => {};

      client.connect(
        { Authorization: `Bearer ${token}` },
        (frame) => {
          setConnected(true);
          stompClientRef.current = client;
          console.log("WebSocket connected.");
          
          // Re-subscribe if we had an active conversation during reconnect
          if (activeConversation) {
            subscribeToConversation(activeConversation.otherUser.id);
          }
        },
        (error) => {
          console.error("WebSocket connection error:", error);
          setConnected(false);
          // Try reconnecting after 5 seconds
          setTimeout(connectWebSocket, 5000);
        }
      );
    } catch (err) {
      console.error("Failed to initialize WebSocket client:", err);
    }
  };

  const disconnectWebSocket = () => {
    // Unsubscribe all active background topics
    Object.values(subscriptionsRef.current).forEach(sub => {
      try {
        sub.unsubscribe();
      } catch (e) {}
    });
    subscriptionsRef.current = {};

    if (stompClientRef.current) {
      try {
        stompClientRef.current.disconnect(() => {
          console.log("WebSocket disconnected.");
        });
      } catch (e) {
        // Suppress disconnection errors if socket already closed
      }
      stompClientRef.current = null;
    }
    setConnected(false);
  };

  // Connect WebSocket when authenticated
  useEffect(() => {
    if (isAuthenticated && token) {
      connectWebSocket();
      return () => disconnectWebSocket();
    }
  }, [isAuthenticated, token]);

  // Background subscriptions manager for active conversations
  useEffect(() => {
    if (!connected || !isAuthenticated || !user) {
      // Clean up subscriptions on disconnect / logout
      Object.values(subscriptionsRef.current).forEach(sub => {
        try { sub.unsubscribe(); } catch (e) {}
      });
      subscriptionsRef.current = {};
      return;
    }

    const currentSubs = { ...subscriptionsRef.current };
    const validConvIds = new Set();

    conversations.forEach(c => {
      const u1 = user.id;
      const u2 = c.otherUser.id;
      const conversationId = u1 < u2 ? `${u1}_${u2}` : `${u2}_${u1}`;
      validConvIds.add(conversationId);

      // Subscribe to this topic if we haven't already
      if (!currentSubs[conversationId]) {
        try {
          const sub = stompClientRef.current.subscribe(
            `/topic/chat/${conversationId}`,
            (messageFrame) => {
              const message = JSON.parse(messageFrame.body);
              
              // Determine if this received message is for the currently open chat window
              const isActive = activeConversationRef.current && 
                (activeConversationRef.current.otherUser.id === message.senderId || 
                 activeConversationRef.current.otherUser.id === message.receiverId);

              if (isActive) {
                setMessages((prev) => {
                  // If it's a message from me, replace the optimistic temp message
                  const isFromMe = message.senderId === user.id;
                  if (isFromMe) {
                    const tempIndex = prev.findIndex(m => m.id.toString().startsWith('temp-') && m.message === message.message);
                    if (tempIndex !== -1) {
                      return prev.map((m, idx) => idx === tempIndex ? message : m);
                    }
                  }
                  
                  // Avoid duplicate appends
                  if (prev.some((m) => m.id === message.id)) return prev;
                  return [...prev, message];
                });

                // Auto mark as read if message is from the partner
                if (message.senderId === c.otherUser.id) {
                  chatService.markAsRead(message.id)
                    .then(() => fetchConversations())
                    .catch(err => {
                      console.error(err);
                      fetchConversations();
                    });
                } else {
                  fetchConversations();
                }
              } else {
                // If it is from the partner and the chat window is NOT active, show a toast notification alert
                if (message.senderId === c.otherUser.id) {
                  toast.info(
                    <div className="cursor-pointer" onClick={() => window.location.href = `/chat/${c.otherUser.id}`}>
                      <span className="font-bold block text-xs">💬 Message from {c.otherUser.fullName}</span>
                      <span className="text-xs line-clamp-1 italic text-zinc-650">"{message.message}"</span>
                    </div>,
                    { autoClose: 6000 }
                  );
                }
                fetchConversations();
              }
            }
          );
          subscriptionsRef.current[conversationId] = sub;
        } catch (err) {
          console.error(`Failed to subscribe to conversation ${conversationId}:`, err);
        }
      }
    });

    // Unsubscribe from channels that are no longer in the conversations list
    Object.keys(currentSubs).forEach(convId => {
      if (!validConvIds.has(convId)) {
        try {
          currentSubs[convId].unsubscribe();
        } catch (e) {}
        delete subscriptionsRef.current[convId];
      }
    });

  }, [conversations, connected, isAuthenticated, user]);

  const joinConversation = async (otherUser) => {
    // Set active conversation metadata (structured like ConversationResponseDTO)
    const existingConv = conversations.find(c => c.otherUser.id === otherUser.id);
    const initialConv = existingConv || { otherUser, lastMessage: null, unreadCount: 0 };
    setActiveConversation(initialConv);
    setMessages([]);

    try {
      // Load history
      const response = await chatService.getChatHistory(otherUser.id, 0, 100);
      if (response && response.success) {
        const historyMessages = response.data.content || [];
        setMessages(historyMessages);
        
        // Find all unread messages sent by the other user
        const unreadIncoming = historyMessages.filter(
          m => m.senderId === otherUser.id && !(m.isRead !== undefined ? m.isRead : m.read)
        );

        if (unreadIncoming.length > 0) {
          // Call markAsRead API for each unread message
          await Promise.all(
            unreadIncoming.map(m => chatService.markAsRead(m.id))
          );
          // Refresh conversations to update the unread count badge in sidebar
          fetchConversations();
        } else if (initialConv.unreadCount > 0) {
          // Fallback refresh just in case count was out of sync
          fetchConversations();
        }
      }
    } catch (error) {
      console.error("Failed to load chat history:", error);
    }
  };

  const sendMessage = (messageText) => {
    if (!stompClientRef.current || !connected || !activeConversation || !user) {
      console.warn("Cannot send message: not connected or no active conversation.");
      return false;
    }

    const payload = {
      receiverId: activeConversation.otherUser.id,
      message: messageText
    };

    try {
      stompClientRef.current.send(
        '/app/chat.send',
        {},
        JSON.stringify(payload)
      );

      // Optimistically append the temp message locally so it appears in the chat room instantly!
      const tempMessage = {
        id: `temp-${Date.now()}`,
        senderId: user.id,
        receiverId: activeConversation.otherUser.id,
        message: messageText,
        sentAt: new Date().toISOString(),
        isRead: false,
        read: false
      };
      
      setMessages((prev) => [...prev, tempMessage]);
      return true;
    } catch (err) {
      console.error("Failed to publish message via WebSocket:", err);
      return false;
    }
  };

  const value = {
    conversations,
    activeConversation,
    messages,
    connected,
    joinConversation,
    sendMessage,
    fetchConversations,
    setActiveConversation,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};
