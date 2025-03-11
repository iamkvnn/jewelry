package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class AddressRequest {
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String district;
    private String village;
    private String address;
}
