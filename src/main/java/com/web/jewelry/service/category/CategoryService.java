package com.web.jewelry.service.category;

import com.web.jewelry.dto.request.CategoryRequest;
import com.web.jewelry.dto.response.CategoryResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Category;
import com.web.jewelry.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CategoryResponse> getCategoriesByParentId(Long parentId) {
        return categoryRepository.findAllByParentId(parentId)
                .stream().map(this::convertToResponse).toList();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    public List<CategoryResponse> getCategoryByName(String name) {
        return categoryRepository.findByNameContaining(name).stream()
                .map(this::convertToResponse).toList();
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public CategoryResponse addCategory(CategoryRequest request) {
        if (existsByName(request.getName())) {
            throw new ResourceNotFoundException("Category already exists please check again");
        }
        Category parent = request.getParent() != null ? getCategoryById(request.getParent().getId()) : null;
        return convertToResponse(categoryRepository.save(Category.builder()
                .name(request.getName())
                .parent(parent)
                .createdAt(LocalDateTime.now())
                .build()
        ));
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest category) {
        Category oldCategory = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (existsByName(category.getName()) && !oldCategory.getName().equals(category.getName())) {
            throw new ResourceNotFoundException("Category already exists please check again");
        }
        oldCategory.setName(category.getName());
        oldCategory.setParent(category.getParent() != null ? getCategoryById(category.getParent().getId()) : null);
        oldCategory.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(categoryRepository.save(oldCategory));
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id).ifPresentOrElse(categoryRepository::delete,
                () -> {throw new ResourceNotFoundException("Category not found");
                });
    }

    @Override
    public CategoryResponse convertToResponse(Category category) {
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
