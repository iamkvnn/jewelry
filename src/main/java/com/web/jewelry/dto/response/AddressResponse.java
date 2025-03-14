package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String district;
    private String village;
    private String address;
    private boolean isDefault;
}
