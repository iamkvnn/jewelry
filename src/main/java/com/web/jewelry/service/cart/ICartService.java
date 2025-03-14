package com.web.jewelry.service.cart;

import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.model.Cart;
import com.web.jewelry.model.CartItem;
import com.web.jewelry.model.Customer;

import java.util.List;

public interface ICartService {
    Cart getCart(Long id);
    Cart getMyCart();
    void clearCart(Long id);
    void clearMyCart();
    void initializeNewCart(Customer customer);
    Cart getCartByCustomerId(Long id);
    public void addItemToCart(Long cartId, Long productSizeId, Long quantity);
    void removeItemFromCart(Long cartId, Long productSizeId);
    void updateItemQuantity(Long cartId, Long productSizeId, Long quantity);
    void changeSize(Long cartId, Long oldProductSize, Long newProductSize);
    CartItem getCartItem(Long cartId, Long productSizeId);
    public CartItem getCartItemById(Long cartItemId);
    Cart checkItem(Long cartId, Long productSizeId);
    Cart unCheckItem(Long cartId, Long productSizeId);
    Cart checkAllItems(Long cartId);
    Cart unCheckAllItems(Long cartId);
    void setCheckout(Long cartItemId);
    void cancelCheckout(Long cartItemId);
    void completeCheckout(Long cartItemId);
    List<CartItem> getCheckedItem(Long cartId);
    CartResponse convertToCartResponse(Cart cart);
}
