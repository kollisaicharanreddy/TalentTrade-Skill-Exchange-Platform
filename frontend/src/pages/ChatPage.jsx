import React, { useEffect, useState, useRef, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Send, MessageSquare, Compass, ShieldCheck, Smile } from 'lucide-react';
import { useChat } from '../hooks/useChat';
import { useAuth } from '../hooks/useAuth';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/Avatar';
import { authService } from '../services/auth.service';

export const ChatPage = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { 
    conversations, 
    activeConversation, 
    messages, 
    connected, 
    joinConversation, 
    sendMessage 
  } = useChat();

  const [typedMessage, setTypedMessage] = useState('');
  const messagesEndRef = useRef(null);

  // Auto Scroll to bottom when messages update
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Handle routing by parameter e.g. /chat/:userId
  useEffect(() => {
    if (userId) {
      const uId = Number(userId);
      // Check if conversation already selected matches target
      if (activeConversation?.otherUser.id === uId) return;

      // Find user object in active conversations
      const matchInConv = conversations.find(c => c.otherUser.id === uId);
      if (matchInConv) {
        joinConversation(matchInConv.otherUser);
      } else {
        // If not in conversations list, fetch details from backend to start it
        const fetchUserData = async () => {
          try {
            const allUsersRes = await authService.getAllUsers(0, 100);
            if (allUsersRes && allUsersRes.success) {
              const target = allUsersRes.data.content.find(u => u.id === uId);
              if (target) {
                joinConversation(target);
              }
            }
          } catch (e) {
            console.error("Failed to load user details for chat param:", e);
          }
        };
        fetchUserData();
      }
    }
  }, [userId, conversations, activeConversation, joinConversation]);

  const handleSend = (e) => {
    e.preventDefault();
    if (!typedMessage.trim()) return;
    
    const sent = sendMessage(typedMessage);
    if (sent) {
      setTypedMessage('');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend(e);
    }
  };

  return (
    <div className="h-[calc(100vh-8rem)] min-h-[450px] flex bg-white border border-zinc-200 rounded-xl overflow-hidden shadow-sm">
      
      {/* Sidebar Conversation list */}
      <div className="w-80 border-r border-zinc-200 flex flex-col bg-zinc-50/50 shrink-0">
        <div className="p-4 border-b bg-white flex items-center justify-between">
          <h2 className="font-bold text-zinc-900 text-md flex items-center space-x-2">
            <MessageSquare className="h-4 w-4 text-zinc-500" />
            <span>Conversations</span>
          </h2>
          <span className="inline-flex items-center space-x-1 text-xxs font-semibold bg-zinc-100 text-zinc-600 px-2 py-0.5 rounded-full">
            <span className={`h-1.5 w-1.5 rounded-full ${connected ? 'bg-green-500' : 'bg-red-400 animate-pulse'}`} />
            <span>{connected ? 'WS Live' : 'Offline'}</span>
          </span>
        </div>

        {/* Conversations Feed scroll pane */}
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {conversations.length > 0 ? (
            conversations.map((c) => {
              const isActive = activeConversation?.otherUser.id === c.otherUser.id;
              
              return (
                <div
                  key={c.otherUser.id}
                  onClick={() => {
                    joinConversation(c.otherUser);
                    navigate(`/chat/${c.otherUser.id}`);
                  }}
                  className={`flex items-center space-x-3 p-3 rounded-lg cursor-pointer transition-all ${isActive ? 'bg-zinc-950 text-white shadow-sm' : 'hover:bg-zinc-100 bg-white border border-zinc-200/50'}`}
                >
                  <Avatar className="h-9 w-9 border-zinc-200/20">
                    <AvatarFallback className={isActive ? 'bg-zinc-800 text-zinc-300' : 'bg-zinc-200 text-zinc-600'}>
                      {c.otherUser.fullName.charAt(0)}
                    </AvatarFallback>
                  </Avatar>

                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-baseline">
                      <p className={`text-sm font-bold truncate ${isActive ? 'text-white' : 'text-zinc-900'}`}>
                        {c.otherUser.fullName}
                      </p>
                      {c.unreadCount > 0 && (
                        <span className="inline-flex h-4 min-w-[1rem] px-1 items-center justify-center rounded-full bg-primary text-primary-foreground text-xxs font-bold text-center shrink-0">
                          {c.unreadCount}
                        </span>
                      )}
                    </div>
                    {c.lastMessage && (
                      <p className={`text-xs truncate mt-0.5 ${isActive ? 'text-zinc-400' : 'text-zinc-500'}`}>
                        {c.lastMessage.senderId === user.id ? 'You: ' : ''}{c.lastMessage.message}
                      </p>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            <div className="text-center py-10 text-xs text-zinc-400 space-y-1">
              <p>No active conversations.</p>
              <p className="text-xxs text-zinc-500">Go to matches to request exchanges.</p>
            </div>
          )}
        </div>
      </div>

      {/* Message Chat workspace Pane */}
      <div className="flex-1 flex flex-col min-w-0 bg-white">
        {activeConversation ? (
          <>
            {/* Header info bar */}
            <div className="p-4 border-b flex items-center justify-between bg-zinc-50/50">
              <div className="flex items-center space-x-3 min-w-0">
                <Avatar className="h-9 w-9 border-zinc-200">
                  <AvatarFallback className="bg-zinc-200 text-zinc-700">
                    {activeConversation.otherUser.fullName.charAt(0)}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <p className="text-sm font-bold text-zinc-900 truncate">{activeConversation.otherUser.fullName}</p>
                  <p className="text-xxs text-zinc-500 truncate">@{activeConversation.otherUser.username}</p>
                </div>
              </div>

              <div className="flex items-center space-x-2 text-xxs text-green-700 font-semibold bg-green-50 px-2 py-0.5 rounded-full border border-green-150 shrink-0">
                <ShieldCheck className="h-3 w-3" />
                <span>Secure Channel Connected</span>
              </div>
            </div>

            {/* Messages Display Area */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-zinc-50/20">
              {messages.map((m) => {
                const isMe = m.senderId === user.id;
                
                return (
                  <div 
                    key={m.id} 
                    className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}
                  >
                    <div 
                      className={`max-w-[70%] rounded-xl px-4 py-2.5 shadow-sm text-sm border ${
                        isMe 
                          ? 'bg-zinc-900 text-white border-zinc-950 rounded-tr-none' 
                          : 'bg-white text-zinc-950 border-zinc-200 rounded-tl-none'
                      }`}
                    >
                      <p className="whitespace-pre-wrap leading-relaxed">{m.message}</p>
                      <span className={`text-[10px] text-right mt-1.5 block leading-none ${isMe ? 'text-zinc-400' : 'text-zinc-500'}`}>
                        {new Date(m.sentAt).toLocaleTimeString(undefined, {
                          hour: '2-digit',
                          minute: '2-digit',
                          hour12: false
                        })}
                      </span>
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>

            {/* Message input bar */}
            <div className="p-4 border-t">
              <form onSubmit={handleSend} className="flex items-center space-x-2">
                <Input
                  placeholder={`Send a secure message to ${activeConversation.otherUser.fullName}...`}
                  value={typedMessage}
                  onChange={(e) => setTypedMessage(e.target.value)}
                  onKeyDown={handleKeyDown}
                  className="flex-1 rounded-xl h-10 border-zinc-200"
                />
                <Button 
                  type="submit" 
                  size="icon" 
                  className="h-10 w-10 shrink-0 rounded-xl bg-zinc-950 hover:bg-zinc-800"
                >
                  <Send className="h-4 w-4" />
                </Button>
              </form>
            </div>
          </>
        ) : (
          /* Empty Chat placeholder */
          <div className="flex-1 flex flex-col items-center justify-center p-8 text-center text-zinc-400 space-y-3">
            <div className="p-4 bg-zinc-50 border rounded-full flex items-center justify-center">
              <Compass className="h-10 w-10 text-zinc-300" />
            </div>
            <h3 className="font-bold text-lg text-zinc-800">Your Secure Chat Lobby</h3>
            <p className="text-sm text-zinc-500 max-w-sm">
              Select an active conversation on the left, or connect with a peer in reciprocal matches to open a secure channel!
            </p>
          </div>
        )}
      </div>

    </div>
  );
};

export default ChatPage;
