package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryResponse parent;
}
