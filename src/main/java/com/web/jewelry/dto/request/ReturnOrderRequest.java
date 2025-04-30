package com.web.jewelry.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReturnOrderRequest {
    private String orderId;
    private List<ReturnItemRequest> items;
}
