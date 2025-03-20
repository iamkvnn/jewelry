package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private CategoryRequest parent;
}
