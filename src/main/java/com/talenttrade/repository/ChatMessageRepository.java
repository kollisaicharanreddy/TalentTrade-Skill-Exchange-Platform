package com.talenttrade.repository;

import com.talenttrade.entity.ChatMessage;
import com.talenttrade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)")
    Page<ChatMessage> findChatHistory(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN (SELECT m.receiver.id FROM ChatMessage m WHERE m.sender = :user) OR u.id IN (SELECT m.sender.id FROM ChatMessage m WHERE m.receiver = :user)")
    Page<User> findConversationsByUser(@Param("user") User user, Pageable pageable);

    @Query(value = "SELECT * FROM chat_messages WHERE (sender_id = :u1 AND receiver_id = :u2) OR (sender_id = :u2 AND receiver_id = :u1) ORDER BY sent_at DESC LIMIT 1", nativeQuery = true)
    Optional<ChatMessage> findLastMessage(@Param("u1") Long u1, @Param("u2") Long u2);

    long countBySenderAndReceiverAndIsReadFalse(User sender, User receiver);
}
