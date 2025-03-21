package com.web.jewelry.controller;

import com.web.jewelry.dto.request.WishlistItemRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.WishlistItemResponse;
import com.web.jewelry.model.WishlistItem;
import com.web.jewelry.service.WishlistItem.IWishlistItemService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("${api.prefix}/wishlist")
@RestController
public class WishlistItemController {
    private final IWishlistItemService wishlistItemService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getWishlistItemById(@PathVariable Long id) {
        WishlistItem wishlistItem = wishlistItemService.getWishlistItemById(id);
        WishlistItemResponse wishlistItemResponse = wishlistItemService.convertToResponse(wishlistItem);
        return ResponseEntity.ok(new ApiResponse("200", "Success", wishlistItemResponse));
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse> getWishlist(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<WishlistItem> wishlistItems = wishlistItemService.getCustomerWishlistItems(PageRequest.of(page - 1, size));
        Page<WishlistItemResponse> wishlistItemResponse = wishlistItemService.convertToResponse(wishlistItems);
        return ResponseEntity.ok(new ApiResponse("200", "Success", wishlistItemResponse));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteWishlistItem(@PathVariable Long id) {
        wishlistItemService.deleteWishlistItem(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addWishlistItem(@RequestParam Long customerId, @RequestBody WishlistItemRequest wishlistItemRequest) {
        WishlistItem wishlistItem = wishlistItemService.addWishlistItem(customerId, wishlistItemRequest);
        WishlistItemResponse wishlistItemResponse = wishlistItemService.convertToResponse(wishlistItem);
        return ResponseEntity.ok(new ApiResponse("200", "Success", wishlistItemResponse));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateWishlistItem(@PathVariable Long id, @RequestBody WishlistItemRequest wishlistItemRequest) {
        WishlistItem wishlistItem = wishlistItemService.updateWishlistItem(id, wishlistItemRequest);
        WishlistItemResponse wishlistItemResponse = wishlistItemService.convertToResponse(wishlistItem);
        return ResponseEntity.ok(new ApiResponse("200", "Success", wishlistItemResponse));
    }
}
