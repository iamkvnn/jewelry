package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/notifications")
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getNotifications(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(page, size);
        return ResponseEntity.ok(new ApiResponse("200", "Success", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadNotificationCount() {
        Long unreadCount = notificationService.countUnreadNotification();
        return ResponseEntity.ok(new ApiResponse("200", "Success", unreadCount));
    }

    @PutMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/mark-as-read-all")
    public ResponseEntity<ApiResponse> markAsReadAllForUser() {
        notificationService.markAsReadAll();
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse> deleteAllNotification() {
        notificationService.deleteAllNotification();
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
