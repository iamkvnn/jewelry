package com.web.jewelry.dto.response;

import com.web.jewelry.enums.ENotificationStatus;
import com.web.jewelry.enums.ENotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private ENotificationStatus status;
    private LocalDateTime sentAt;
    private ENotificationType type;
}
