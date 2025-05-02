package com.web.jewelry.service.cart;

import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.model.Cart;
import com.web.jewelry.model.CartItem;
import com.web.jewelry.model.Customer;

import java.util.List;

public interface ICartService {
    Cart getMyCart();
    void clearMyCart();
    void initializeNewCart(Customer customer);
    void addItemToCart(Long productSizeId, Long quantity);
    void removeItemFromCart(Long productSizeId);
    void removeItemsFromCart(List<Long> productSizeIds);
    void updateItemQuantity(Long productSizeId, Long quantity);
    void changeSize(Long oldProductSize, Long newProductSize);
    CartItem getCartItem(Long cartId, Long productSizeId);
    CartItem getCartItemById(Long cartItemId);
    CartResponse convertToCartResponse(Cart cart);
}
