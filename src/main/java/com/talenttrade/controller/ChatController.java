package com.talenttrade.controller;

import com.talenttrade.dto.ChatMessageRequestDTO;
import com.talenttrade.dto.ChatMessageResponseDTO;
import com.talenttrade.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequestDTO request, Principal principal) {
        if (principal == null) {
            log.warn("Attempt to send WebSocket message without authentication");
            return;
        }
        String senderEmail = principal.getName();
        log.info("WebSocket: Message received from {} to receiver ID: {}", senderEmail, request.getReceiverId());

        ChatMessageResponseDTO response = chatService.saveMessage(senderEmail, request);

        // Form conversation ID: minUserId_maxUserId
        Long id1 = response.getSenderId();
        Long id2 = response.getReceiverId();
        String conversationId = id1 < id2 ? id1 + "_" + id2 : id2 + "_" + id1;

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, response);
        log.info("WebSocket: Broadcasted message ID {} to topic /topic/chat/{}", response.getId(), conversationId);
    }
}
