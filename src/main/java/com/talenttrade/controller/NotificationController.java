package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.NotificationResponseDTO;
import com.talenttrade.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user in-app notifications")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves all notifications for the authenticated user, sorted by creation date descending, with pagination support.")
    public ResponseEntity<ApiResponse<Page<NotificationResponseDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<NotificationResponseDTO> response = notificationService.getNotifications(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Notifications retrieved successfully"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Retrieves the count of unread notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread notifications count retrieved successfully"));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by its ID. Restricted to the owner of the notification.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        notificationService.markAsRead(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read successfully"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all active notifications for the current authenticated user as read.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Removes a specific notification by its ID. Restricted to the owner of the notification.")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        notificationService.deleteNotification(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
