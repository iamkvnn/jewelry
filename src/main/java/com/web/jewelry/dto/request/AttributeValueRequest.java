package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class AttributeValueRequest {
    private Long id;
    private Long attributeId;
    private String name;
    private String value;
}
