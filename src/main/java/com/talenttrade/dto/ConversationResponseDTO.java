package com.talenttrade.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDTO {
    private UserResponse otherUser;
    private ChatMessageResponseDTO lastMessage;
    private long unreadCount;
}
