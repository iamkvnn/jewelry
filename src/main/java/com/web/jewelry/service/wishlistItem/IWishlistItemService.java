package com.web.jewelry.service.WishlistItem;

import com.web.jewelry.dto.request.WishlistItemRequest;
import com.web.jewelry.dto.response.WishlistItemResponse;
import com.web.jewelry.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IWishlistItemService {
    WishlistItem updateWishlistItem(Long id, WishlistItemRequest wishlistItemRequest);
    WishlistItem addWishlistItem(Long userId, WishlistItemRequest wishlistItemRequest);
    void deleteWishlistItem(Long id);
    Page<WishlistItem> getCustomerWishlistItems(Pageable pageable);
    WishlistItem getWishlistItemById(Long id);
    WishlistItemResponse convertToResponse(WishlistItem wishlistItem);
    Page<WishlistItemResponse> convertToResponse(Page<WishlistItem> wishlistItemPage);
}
