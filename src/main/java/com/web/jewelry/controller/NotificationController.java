package com.web.jewelry.controller;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/notifications")
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerNotifications(@PathVariable Long customerId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<NotificationResponse> notifications = notificationService.getCustomerNotifications(customerId, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", notifications));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse> getStaffNotifications(@PathVariable Long staffId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<NotificationResponse> notifications = notificationService.getStaffNotifications(staffId, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", notifications));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse> getManagerNotifications(@PathVariable Long managerId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<NotificationResponse> notifications = notificationService.getManagerNotifications(managerId, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", notifications));
    }

    @PostMapping("/send-to-customer")
    public ResponseEntity<ApiResponse> sendNotificationToSpecificCustomer(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToSpecificCustomer(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/send-to-all-customer")
    public ResponseEntity<ApiResponse> sendNotificationToAllCustomer(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToAllCustomer(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/send-to-staff")
    public ResponseEntity<ApiResponse> sendNotificationToSpecificStaff(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToSpecificStaff(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/send-to-all-staff")
    public ResponseEntity<ApiResponse> sendNotificationToAllStaff(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToAllStaff(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/send-to-manager")
    public ResponseEntity<ApiResponse> sendNotificationToSpecificManager(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToSpecificManager(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/send-to-all-manager")
    public ResponseEntity<ApiResponse> sendNotificationToAllManager(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToAllManager(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/mark-as-read-all/customer/{customerId}")
    public ResponseEntity<ApiResponse> markAsReadAllForUser(@PathVariable Long customerId) {
        notificationService.markAsReadAllForUser(customerId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/mark-as-read-all/staff/{staffId}")
    public ResponseEntity<ApiResponse> markAsReadAllForStaff(@PathVariable Long staffId) {
        notificationService.markAsReadAllForStaff(staffId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/mark-as-read-all/manager/{managerId}")
    public ResponseEntity<ApiResponse> markAsReadAllForManager(@PathVariable Long managerId) {
        notificationService.markAsReadAllForManager(managerId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete-all/customer/{customerId}")
    public ResponseEntity<ApiResponse> deleteAllNotification(@PathVariable Long customerId) {
        notificationService.deleteAllNotification(customerId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete-all/staff/{staffId}")
    public ResponseEntity<ApiResponse> deleteAllNotificationByStaff(@PathVariable Long staffId) {
        notificationService.deleteAllNotificationByStaff(staffId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete-all/manager/{managerId}")
    public ResponseEntity<ApiResponse> deleteAllNotificationByManager(@PathVariable Long managerId) {
        notificationService.deleteAllNotificationByManager(managerId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
