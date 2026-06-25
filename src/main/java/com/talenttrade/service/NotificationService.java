package com.talenttrade.service;

import com.talenttrade.dto.NotificationResponseDTO;
import com.talenttrade.entity.Notification;
import com.talenttrade.entity.NotificationType;
import com.talenttrade.entity.User;
import com.talenttrade.exception.NotificationNotFoundException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User user, String title, String message, NotificationType type) {
        log.info("Creating notification of type {} for user ID: {}", type, user.getId());
        
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.debug("Notification created successfully for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotifications(String email, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", email);
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(email, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    public void markAsRead(Long id, String email) {
        log.info("Marking notification ID: {} as read by user: {}", id, email);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to mark notification ID: {} as read by user: {}", id, email);
            throw new UnauthorizedException("You are not authorized to access this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        log.info("Marking all notifications as read for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        notificationRepository.markAllAsRead(user.getEmail());
    }

    @Transactional
    public void deleteNotification(Long id, String email) {
        log.info("Deleting notification ID: {} by user: {}", id, email);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to delete notification ID: {} by user: {}", id, email);
            throw new UnauthorizedException("You are not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Notification ID: {} deleted successfully", id);
    }

    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
