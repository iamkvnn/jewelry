package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class FeatureValueResponse {
    private Long id;
    private Long featureId;
    private String name;
    private String value;
}
