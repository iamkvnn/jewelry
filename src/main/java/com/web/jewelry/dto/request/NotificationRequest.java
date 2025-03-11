package com.web.jewelry.dto.request;

import com.web.jewelry.enums.ENotificationType;
import lombok.Data;

import java.util.Set;

@Data
public class NotificationRequest {
    private String title;
    private String content;
    private ENotificationType type;
    private Set<Long> customerIds;
    private Set<Long> staffIds;
    private Set<Long> managerIds;
}
