package com.web.jewelry.controller;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import com.web.jewelry.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse> getAllProducts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<ProductResponse> product = productService.getAllProducts(PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/add")
    ResponseEntity<ApiResponse> addProduct(@RequestBody ProductRequest request) {
        ProductResponse product = productService.addProduct(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

    @PutMapping("/product/{id}")
    ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

    @DeleteMapping("/product/{id}")
    ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", productService.convertToProductResponse(product)));
    }

    @GetMapping("/category/{id}")
    ResponseEntity<ApiResponse> getProductsByCategory(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<ProductResponse> responses = productService.getProductsByCategory(id, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @GetMapping("/search-and-filter")
    ResponseEntity<ApiResponse> getFilterAndSearchProducts(@RequestParam(required = false) String title, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Long minPrice,
                                                     @RequestParam(required = false) Long maxPrice, @RequestParam(required = false) List<String> productSizes,
                                                     @RequestParam(required = false) String material, @RequestParam(required = false) String dir,
                                                     @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size){
        Page<ProductResponse> products = productService.getSearchAndFilterProducts(title, categories, material, minPrice,maxPrice, productSizes, dir, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", products));

    }
}
