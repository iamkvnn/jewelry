package com.web.jewelry.service.category;

import com.web.jewelry.dto.request.CategoryRequest;
import com.web.jewelry.dto.response.CategoryResponse;
import com.web.jewelry.model.Category;

import java.util.List;


public interface ICategoryService {
    List<CategoryResponse> getCategoriesByParentId(Long parentId);
    Category getCategoryById(Long id);
    List<CategoryResponse> getCategoryByName(String name);
    List<CategoryResponse> getAllCategories();
    CategoryResponse addCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategoryById(Long id);
    CategoryResponse convertToResponse(Category category);
    boolean existsByName(String name);
}
