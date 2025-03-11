package com.web.jewelry.service.notification;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.NotificationResponse;
import com.web.jewelry.enums.ENotificationStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.NotificationRepository;
import com.web.jewelry.repository.UserNotificationRepository;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public void sendNotificationToSpecificCustomer(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        request.getCustomerIds().forEach(customerId -> {
            userNotificationRepository.save(UserNotification.builder()
                    .customer((Customer) userService.getCustomerById(customerId))
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build());
        });
    }

    @Transactional
    @Override
    public void sendNotificationToAllCustomer(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllCustomers(Pageable.unpaged()).forEach(customer -> {
            userNotificationRepository.save(UserNotification.builder()
                    .customer(customer)
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build());
        });
    }

    @Transactional
    @Override
    public void sendNotificationToSpecificStaff(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        request.getStaffIds().forEach(staffId -> {
            UserNotification userNotification = UserNotification.builder()
                    .staff((Staff) userService.getStaffById(staffId))
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build();
            userNotificationRepository.save(userNotification);
        });
    }

    @Transactional
    @Override
    public void sendNotificationToAllStaff(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllStaff(Pageable.unpaged()).forEach(staff -> {
            userNotificationRepository.save(UserNotification.builder()
                    .staff(staff)
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build());
        });
    }

    @Transactional
    @Override
    public void sendNotificationToSpecificManager(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        request.getManagerIds().forEach(managerId -> {
            UserNotification userNotification = UserNotification.builder()
                    .manager((Manager) userService.getManagerById(managerId))
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build();
            userNotificationRepository.save(userNotification);
        });
    }

    @Transactional
    @Override
    public void sendNotificationToAllManager(NotificationRequest request) {
        Notification notification = notificationRepository.save(createBaseNotification(request));
        userService.getAllManagers(Pageable.unpaged()).forEach(manager -> {
            userNotificationRepository.save(UserNotification.builder()
                    .manager(manager)
                    .notification(notification)
                    .status(ENotificationStatus.UNREAD)
                    .build());
        });
    }

    private Notification createBaseNotification(NotificationRequest request) {
        return Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<NotificationResponse> getCustomerNotifications(Long customerId, Pageable pageable) {
        return userNotificationRepository.findAllByCustomerId(customerId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public Page<NotificationResponse> getStaffNotifications(Long staffId, Pageable pageable) {
        return userNotificationRepository.findAllByStaffId(staffId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public Page<NotificationResponse> getManagerNotifications(Long managerId, Pageable pageable) {
        return userNotificationRepository.findAllByManagerId(managerId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public void markAsRead(Long notificationId) {
        userNotificationRepository.findById(notificationId).ifPresentOrElse(userNotification -> {
            userNotification.setStatus(ENotificationStatus.READ);
            userNotificationRepository.save(userNotification);
        }, () -> {
            throw new ResourceNotFoundException("Notification not found");
        });
    }

    @Override
    public void markAsReadAllForUser(Long customerId) {
        userNotificationRepository.findAllByCustomerId(customerId, Pageable.unpaged())
                .forEach(userNotification -> {
                    userNotification.setStatus(ENotificationStatus.READ);
                    userNotificationRepository.save(userNotification);
                });
    }

    @Override
    public void markAsReadAllForStaff(Long staffId) {
        userNotificationRepository.findAllByStaffId(staffId, Pageable.unpaged())
                .forEach(userNotification -> {
                    userNotification.setStatus(ENotificationStatus.READ);
                    userNotificationRepository.save(userNotification);
                });
    }

    @Override
    public void markAsReadAllForManager(Long managerId) {
        userNotificationRepository.findAllByManagerId(managerId, Pageable.unpaged())
                .forEach(userNotification -> {
                    userNotification.setStatus(ENotificationStatus.READ);
                    userNotificationRepository.save(userNotification);
                });
    }

    @Override
    public void deleteNotification(Long notificationId) {
        userNotificationRepository.findById(notificationId).ifPresentOrElse(userNotificationRepository::delete, () -> {
            throw new ResourceNotFoundException("Notification not found");
        });
    }

    @Override
    public void deleteAllNotification(Long customerId) {
        userNotificationRepository.deleteAll(userNotificationRepository.findAllByCustomerId(customerId, Pageable.unpaged()));
    }

    @Override
    public void deleteAllNotificationByStaff(Long staffId) {
        userNotificationRepository.deleteAll(userNotificationRepository.findAllByStaffId(staffId, Pageable.unpaged()));
    }

    @Override
    public void deleteAllNotificationByManager(Long managerId) {
        userNotificationRepository.deleteAll(userNotificationRepository.findAllByManagerId(managerId, Pageable.unpaged()));
    }

    @Override
    public NotificationResponse convertToResponse(UserNotification userNotification) {
        NotificationResponse response = modelMapper.map(userNotification, NotificationResponse.class);
        response.setType(userNotification.getNotification().getType());
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
