package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.model.Cart;
import com.web.jewelry.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-cart")
    public ResponseEntity<CartResponse> getMyCart() {
        Cart cart = cartService.getMyCart();
        return ResponseEntity.ok(cartService.convertToCartResponse(cart));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam Long productSizeId, @RequestParam Long quantity) {
        cartService.addItemToCart(productSizeId, quantity);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@RequestParam Long productSizeId) {
        cartService.removeItemFromCart(productSizeId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@RequestParam Long productSizeId, @RequestParam Long quantity) {
        cartService.updateItemQuantity(productSizeId, quantity);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/change-size")
    public ResponseEntity<ApiResponse> changeSize(@RequestParam Long oldProductSize, @RequestParam Long newProductSize) {
        cartService.changeSize(oldProductSize, newProductSize);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearMyCart() {
        cartService.clearMyCart();
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
