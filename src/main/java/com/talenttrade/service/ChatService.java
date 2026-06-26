package com.talenttrade.service;

import com.talenttrade.dto.ChatMessageRequestDTO;
import com.talenttrade.dto.ChatMessageResponseDTO;
import com.talenttrade.dto.ConversationResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.ChatMessage;
import com.talenttrade.entity.RequestStatus;
import com.talenttrade.entity.SessionStatus;
import com.talenttrade.entity.User;
import com.talenttrade.exception.InvalidRequestException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.ChatMessageRepository;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.SessionRepository;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public ChatMessageResponseDTO saveMessage(String senderEmail, ChatMessageRequestDTO requestDTO) {
        log.info("Saving new message from {} to user ID: {}", senderEmail, requestDTO.getReceiverId());

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found: " + senderEmail));

        User receiver = userRepository.findById(requestDTO.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with ID: " + requestDTO.getReceiverId()));

        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidRequestException("You cannot send messages to yourself");
        }

        // Validate that they have an ACCEPTED request or active session
        if (!canChat(sender, receiver)) {
            log.warn("Chat permission denied between user ID: {} and user ID: {}", sender.getId(), receiver.getId());
            throw new UnauthorizedException("You are not allowed to chat with this user. Requires an accepted exchange request or active session.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .message(requestDTO.getMessage())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        log.info("Chat message saved successfully with ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponseDTO> getChatHistory(String email, Long otherUserId, Pageable pageable) {
        log.debug("Fetching chat history between user: {} and user ID: {}", email, otherUserId);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + otherUserId));

        // Validate permission to view history
        if (!canChat(currentUser, otherUser)) {
            throw new UnauthorizedException("You do not have permission to view chat history with this user.");
        }

        return chatMessageRepository.findChatHistory(currentUser, otherUser, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponseDTO> getConversations(String email, Pageable pageable) {
        log.debug("Fetching active conversations for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Page<User> distinctUsers = chatMessageRepository.findConversationsByUser(user, pageable);

        return distinctUsers.map(otherUser -> {
            ChatMessage lastMessage = chatMessageRepository.findLastMessage(user.getId(), otherUser.getId()).orElse(null);
            long unreadCount = chatMessageRepository.countBySenderAndReceiverAndIsReadFalse(otherUser, user);

            return ConversationResponseDTO.builder()
                    .otherUser(mapToUserResponse(otherUser))
                    .lastMessage(lastMessage != null ? mapToResponseDTO(lastMessage) : null)
                    .unreadCount(unreadCount)
                    .build();
        });
    }

    @Transactional
    public ChatMessageResponseDTO markAsRead(Long messageId, String email) {
        log.info("Marking message ID: {} as read by user: {}", messageId, email);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        if (!message.getReceiver().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to mark message ID: {} as read by user: {}", messageId, email);
            throw new UnauthorizedException("You can only mark received messages as read");
        }

        message.setRead(true);
        ChatMessage updated = chatMessageRepository.save(message);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public void deleteMessage(Long messageId, String email) {
        log.info("Deleting message ID: {} by user: {}", messageId, email);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        if (!message.getSender().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to delete message ID: {} by user: {}", messageId, email);
            throw new UnauthorizedException("You can only delete messages sent by you");
        }

        chatMessageRepository.delete(message);
        log.info("Message ID: {} deleted successfully", messageId);
    }

    public boolean canChat(User user1, User user2) {
        if (user1.getId().equals(user2.getId())) {
            return false;
        }

        // 1. Check if there's an ACCEPTED exchange request in either direction
        boolean requestAccepted = exchangeRequestRepository.existsBySenderAndReceiverAndStatus(user1, user2, RequestStatus.ACCEPTED)
                || exchangeRequestRepository.existsBySenderAndReceiverAndStatus(user2, user1, RequestStatus.ACCEPTED);
        if (requestAccepted) {
            return true;
        }

        // 2. Check if there's a SCHEDULED session in either direction
        return sessionRepository.existsByMentorAndLearnerAndStatus(user1, user2, SessionStatus.SCHEDULED)
                || sessionRepository.existsByMentorAndLearnerAndStatus(user2, user1, SessionStatus.SCHEDULED);
    }

    private ChatMessageResponseDTO mapToResponseDTO(ChatMessage message) {
        return ChatMessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getFullName())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
