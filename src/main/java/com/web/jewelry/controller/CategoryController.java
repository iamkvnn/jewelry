package com.web.jewelry.controller;

import com.web.jewelry.dto.request.CategoryRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.CategoryResponse;
import com.web.jewelry.model.Category;
import com.web.jewelry.service.category.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse("200", "Success", categories));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.addCategory(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", category));
    }

    @GetMapping("category/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id){
        Category category = categoryService.getCategoryById(id);
        CategoryResponse response = categoryService.convertToResponse(category);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("category")
    public ResponseEntity<ApiResponse> getCategoryByName(@RequestParam String name){
        CategoryResponse category = categoryService.getCategoryByName(name);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", category));
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategoryById(id);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", category));
    }
}
