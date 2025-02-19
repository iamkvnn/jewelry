package com.web.jewelry.dto.request;

import com.web.jewelry.dto.response.CategoryResponse;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private CategoryRequest parent;
}
