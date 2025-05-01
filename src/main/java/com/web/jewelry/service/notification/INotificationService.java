package com.web.jewelry.service.notification;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.model.UserNotification;
import org.springframework.data.domain.Page;

public interface INotificationService {
    void sendNotificationToSpecificCustomer(NotificationRequest request);
    void sendNotificationToAllCustomer(NotificationRequest request);
    void sendNotificationToAllStaff(NotificationRequest request);
    void sendNotificationToAllManager(NotificationRequest request);
    Page<NotificationResponse> getNotifications(int page, int size);

    Long countUnreadNotification();

    void markAsRead(Long notificationId);
    void markAsReadAll();
    void deleteNotification(Long notificationId);
    void deleteAllNotification();
    NotificationResponse convertToResponse(UserNotification userNotification);
    Page<NotificationResponse> convertToResponse(Page<UserNotification> userNotifications);
}
