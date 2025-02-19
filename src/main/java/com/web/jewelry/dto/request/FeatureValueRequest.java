package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class FeatureValueRequest {
    private Long id;
    private Long featureId;
    private String name;
    private String value;
}
