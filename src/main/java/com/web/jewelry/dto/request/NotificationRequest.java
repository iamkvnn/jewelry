package com.web.jewelry.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class NotificationRequest {
    private String title;
    private String content;
    private Set<Long> customerIds;
    private Set<Long> staffIds;
    private Set<Long> managerIds;
    private boolean isEmail;
}
