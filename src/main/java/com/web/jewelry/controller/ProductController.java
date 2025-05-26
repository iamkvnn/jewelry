package com.web.jewelry.controller;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import com.web.jewelry.service.product.CacheProductService;
import com.web.jewelry.service.product.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    public ProductController(CacheProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/add")
    ResponseEntity<ApiResponse> addProduct(@RequestBody ProductRequest request) {
        ProductResponse product = productService.addProduct(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/product/{id}")
    ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

    @PreAuthorize("hasRole('MANAGER')")
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
        Page<ProductResponse> responses = productService.getProductsByCategory(id, page, size);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @GetMapping("/search-and-filter")
    ResponseEntity<ApiResponse> getFilterAndSearchProducts(@RequestParam(required = false) String title, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Long minPrice,
                                                     @RequestParam(required = false) Long maxPrice, @RequestParam(required = false) List<String> productSizes,
                                                     @RequestParam(required = false) String material, @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "30") int size){
        Page<ProductResponse> products = productService.getSearchAndFilterProducts(title, categories, material, minPrice, maxPrice, productSizes, page, size);
        return ResponseEntity.ok(new ApiResponse("200", "Success", products));
    }
}
