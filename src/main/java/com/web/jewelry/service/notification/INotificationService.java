package com.web.jewelry.service.notification;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
    void sendNotificationToSpecificCustomer(NotificationRequest request);
    void sendNotificationToAllCustomer(NotificationRequest request);
    void sendNotificationToSpecificStaff(NotificationRequest request);
    void sendNotificationToAllStaff(NotificationRequest request);
    void sendNotificationToSpecificManager(NotificationRequest request);
    void sendNotificationToAllManager(NotificationRequest request);
    Page<NotificationResponse> getCustomerNotifications(Long customerId, Pageable pageable);
    Page<NotificationResponse> getStaffNotifications(Long staffId, Pageable pageable);
    Page<NotificationResponse> getManagerNotifications(Long managerId, Pageable pageable);
    void markAsRead(Long notificationId);
    void markAsReadAllForUser(Long customerId);
    void markAsReadAllForStaff(Long staffId);
    void markAsReadAllForManager(Long managerId);
    void deleteNotification(Long notificationId);
    void deleteAllNotification(Long customerId);
    void deleteAllNotificationByStaff(Long staffId);
    void deleteAllNotificationByManager(Long managerId);
    NotificationResponse convertToResponse(UserNotification userNotification);
    Page<NotificationResponse> convertToResponse(Page<UserNotification> userNotifications);
}
