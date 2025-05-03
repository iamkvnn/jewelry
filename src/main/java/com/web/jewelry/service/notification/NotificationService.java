package com.web.jewelry.service.notification;

import com.web.jewelry.dto.request.EmailRequest;
import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.enums.ENotificationStatus;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.NotificationRepository;
import com.web.jewelry.repository.UserNotificationRepository;
import com.web.jewelry.service.email.EmailQueueService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final IUserService userService;
    private final EmailQueueService emailQueueService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public void sendNotificationToSpecificCustomer(NotificationRequest request) {
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            return;
        }
        Notification notification = notificationRepository.save(createBaseNotification(request));
        request.getCustomerIds().forEach(customerId -> {
            Customer customer = (Customer) userService.getCustomerById(customerId);
            userNotificationRepository.save(UserNotification.builder()
                    .customer(customer)
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build());
            if (request.isEmail()) {
                emailQueueService.enqueue(new EmailRequest(
                        customer.getEmail(),
                        notification.getTitle(),
                        notification.getContent()
                ));
            }
        });
    }

    @Transactional
    @Override
    public void sendNotificationToAllCustomer(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllCustomers(Pageable.unpaged()).forEach(customer -> userNotificationRepository.save(UserNotification.builder()
                .customer(customer)
                .notification(notification)
                .status(ENotificationStatus.UNREAD)
                .build()));
    }

    @Transactional
    @Override
    public void sendNotificationToAllStaff(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllStaff(Pageable.unpaged()).forEach(staff -> userNotificationRepository.save(UserNotification.builder()
                .staff(staff)
                .notification(notification)
                .status(ENotificationStatus.UNREAD)
                .build()));
    }

    @Transactional
    @Override
    public void sendNotificationToAllManager(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllManagers(Pageable.unpaged()).forEach(manager -> userNotificationRepository.save(UserNotification.builder()
                .manager(manager)
                .notification(notification)
                .status(ENotificationStatus.UNREAD)
                .build()));
    }

    private Notification createBaseNotification(NotificationRequest request) {
        return Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<NotificationResponse> getNotifications(int page, int size) {
        User user = userService.getCurrentUser();
        return switch (user.getRole()) {
            case CUSTOMER -> convertToResponse(userNotificationRepository.findAllByCustomerId(user.getId(),
                    PageRequest.of(page - 1, size, Sort.by("status").descending().and(Sort.by("notification.sentAt").descending()))));
            case STAFF -> convertToResponse(userNotificationRepository.findAllByStaffId(user.getId(),
                    PageRequest.of(page - 1, size, Sort.by("status").descending().and(Sort.by("notification.sentAt").descending()))));
            case MANAGER -> convertToResponse(userNotificationRepository.findAllByManagerId(user.getId(),
                    PageRequest.of(page - 1, size, Sort.by("status").descending().and(Sort.by("notification.sentAt").descending()))));
        };
    }

    @Override
    public Long countUnreadNotification(){
        User user = userService.getCurrentUser();
        return switch (user.getRole()) {
            case CUSTOMER -> userNotificationRepository.countUnreadByCustomer(user.getId());
            case STAFF -> userNotificationRepository.countUnreadByStaff(user.getId());
            case MANAGER -> userNotificationRepository.countUnreadByManager(user.getId());
        };
    }

    @Override
    public void markAsRead(Long notificationId) {
        User user = userService.getCurrentUser();
        userNotificationRepository.findById(notificationId).ifPresentOrElse(userNotification -> {
            if ((userNotification.getCustomer() == null && user.getRole() == EUserRole.CUSTOMER) ||
                (userNotification.getStaff() == null && user.getRole() == EUserRole.STAFF) ||
                (userNotification.getManager() == null && user.getRole() == EUserRole.MANAGER)) {
                throw new ResourceNotFoundException("Notification not found");
            }
            userNotification.setStatus(ENotificationStatus.READ);
            userNotificationRepository.save(userNotification);
        }, () -> {
            throw new ResourceNotFoundException("Notification not found");
        });
    }

    @Override
    public void markAsReadAll() {
        User user = userService.getCurrentUser();
        switch (user.getRole()) {
            case CUSTOMER -> userNotificationRepository.findUnreadByCustomerId(user.getId()).forEach(userNotification -> {
                userNotification.setStatus(ENotificationStatus.READ);
                userNotificationRepository.save(userNotification);
            });
            case STAFF -> userNotificationRepository.findUnreadByStaffId(user.getId()).forEach(userNotification -> {
                userNotification.setStatus(ENotificationStatus.READ);
                userNotificationRepository.save(userNotification);
            });
            case MANAGER -> userNotificationRepository.findUnreadByManagerId(user.getId()).forEach(userNotification -> {
                userNotification.setStatus(ENotificationStatus.READ);
                userNotificationRepository.save(userNotification);
            });
        }
    }

    @Override
    public void deleteNotification(Long notificationId) {
        User user = userService.getCurrentUser();
        userNotificationRepository.findById(notificationId).ifPresentOrElse(userNotification -> {
            if ((userNotification.getCustomer() == null && user.getRole() == EUserRole.CUSTOMER) ||
                (userNotification.getStaff() == null && user.getRole() == EUserRole.STAFF) ||
                (userNotification.getManager() == null && user.getRole() == EUserRole.MANAGER)) {
                throw new ResourceNotFoundException("Notification not found");
            }
            userNotificationRepository.delete(userNotification);
        }, () -> {
            throw new ResourceNotFoundException("Notification not found");
        });
    }

    @Transactional
    @Override
    public void deleteAllNotification() {
        User user = userService.getCurrentUser();
        switch (user.getRole()) {
            case CUSTOMER -> userNotificationRepository.deleteAllByCustomerId(user.getId());
            case STAFF -> userNotificationRepository.deleteAllByStaffId(user.getId());
            case MANAGER -> userNotificationRepository.deleteAllByManagerId(user.getId());
        }
    }

    @Override
    public NotificationResponse convertToResponse(UserNotification userNotification) {
        NotificationResponse response = modelMapper.map(userNotification, NotificationResponse.class);
        response.setTitle(userNotification.getNotification().getTitle());
        response.setContent(userNotification.getNotification().getContent());
        response.setSentAt(userNotification.getNotification().getSentAt());
        return response;
    }

    @Override
    public Page<NotificationResponse> convertToResponse(Page<UserNotification> userNotifications) {
        return userNotifications.map(this::convertToResponse);
    }
}
