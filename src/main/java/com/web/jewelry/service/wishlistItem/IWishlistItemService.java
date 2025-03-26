package com.web.jewelry.service.wishlistItem;

import com.web.jewelry.dto.request.WishlistItemRequest;
import com.web.jewelry.dto.response.WishlistItemResponse;
import com.web.jewelry.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IWishlistItemService {
    WishlistItem addWishlistItem(WishlistItemRequest wishlistItemRequest);
    void deleteWishlistItem(Long id);
    Page<WishlistItem> getCustomerWishlistItems(Pageable pageable);
    WishlistItemResponse convertToResponse(WishlistItem wishlistItem);
    Page<WishlistItemResponse> convertToResponse(Page<WishlistItem> wishlistItemPage);
}
