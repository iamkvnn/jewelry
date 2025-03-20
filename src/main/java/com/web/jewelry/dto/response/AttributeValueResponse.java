package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class AttributeValueResponse {
    private Long attributeId;
    private String name;
    private String value;
}
