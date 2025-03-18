package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.model.Cart;
import com.web.jewelry.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long id) {
        Cart cart = cartService.getCart(id);
        CartResponse cartResponse = cartService.convertToCartResponse(cart);
        return ResponseEntity.ok(new ApiResponse("200", "Success", cartResponse));
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<ApiResponse> getCustomerCart(@PathVariable Long id) {
        Cart cart = cartService.getCartByCustomerId(id);
        CartResponse cartResponse = cartService.convertToCartResponse(cart);
        return ResponseEntity.ok(new ApiResponse("200", "Success", cartResponse));
    }

    @PostMapping("/{cartId}/add")
    public ResponseEntity<ApiResponse> addItemToCart(@PathVariable Long cartId, @RequestParam Long productSizeId, @RequestParam Long quantity) {
        cartService.addItemToCart(cartId, productSizeId, quantity);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/{cartId}/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long cartId, @RequestParam Long productSizeId) {
        cartService.removeItemFromCart(cartId, productSizeId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/{cartId}/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long cartId, @RequestParam Long productSizeId, @RequestParam Long quantity) {
        cartService.updateItemQuantity(cartId, productSizeId, quantity);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/{cartId}/change-size")
    public ResponseEntity<ApiResponse> changeSize(@PathVariable Long cartId, @RequestParam Long oldProductSize, @RequestParam Long newProductSize) {
        cartService.changeSize(cartId, oldProductSize, newProductSize);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long id) {
        cartService.clearCart(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
