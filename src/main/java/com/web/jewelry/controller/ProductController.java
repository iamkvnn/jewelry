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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse> getAllProducts() {
        List<ProductResponse> product = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }

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

    @GetMapping("/search")
    ResponseEntity<ApiResponse> searchProduct(@RequestParam String title, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<ProductResponse> product = productService.findByTitleContaining(title, PageRequest.of(page - 1, size));
        return ResponseEntity.ok(new ApiResponse("200", "Success", product));
    }
}
