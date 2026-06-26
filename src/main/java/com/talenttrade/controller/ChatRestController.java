package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.ChatMessageResponseDTO;
import com.talenttrade.dto.ConversationResponseDTO;
import com.talenttrade.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Endpoints for managing real-time chat history and conversations")
@SecurityRequirement(name = "BearerAuth")
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get chat history", description = "Fetches a paginated list of messages between the authenticated user and another user.")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponseDTO>>> getChatHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        
        Page<ChatMessageResponseDTO> history = chatService.getChatHistory(email, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history, "Chat history retrieved successfully"));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get active conversations", description = "Fetches a paginated list of distinct active chat conversations for the authenticated user.")
    public ResponseEntity<ApiResponse<Page<ConversationResponseDTO>>> getConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        // Since we query conversations, sort by default ID/lastMessage status inside service, or apply custom sorting here
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));

        Page<ConversationResponseDTO> conversations = chatService.getConversations(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations, "Conversations retrieved successfully"));
    }

    @PutMapping("/read/{messageId}")
    @Operation(summary = "Mark message as read", description = "Marks a specific chat message as read. Only permitted for the receiver.")
    public ResponseEntity<ApiResponse<ChatMessageResponseDTO>> markAsRead(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ChatMessageResponseDTO updated = chatService.markAsRead(messageId, email);
        return ResponseEntity.ok(ApiResponse.success(updated, "Message marked as read"));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete chat message", description = "Deletes a specific chat message. Only permitted for the sender.")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        chatService.deleteMessage(messageId, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }
}
