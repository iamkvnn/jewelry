package com.web.jewelry.service.cart;

import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.enums.EProductStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.CartItemRepository;
import com.web.jewelry.repository.CartRepository;
import com.web.jewelry.service.observer.ProductSizeListener;
import com.web.jewelry.service.observer.ProductSizeObservable;
import com.web.jewelry.service.productSize.IProductSizeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService implements ICartService, ProductSizeListener {
    private final IProductSizeService productSizeService;
    private final ProductSizeObservable productSizeObservable;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        productSizeObservable.addObserver(this);
    }

    @Override
    public Cart getCart(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    @Override
    public Cart getMyCart() {
        return null;
    }

    @Transactional
    @Override
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        cart.setTotalPrice(0L);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public void clearMyCart() {

    }

    @Override
    public void initializeNewCart(Customer customer) {
        cartRepository.save(Cart.builder()
                .customer(customer)
                .totalPrice(0L)
                .build());
    }

    @Override
    public Cart getCartByCustomerId(Long id) {
        return cartRepository.findByCustomerId(id).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    @Override
    public void addItemToCart(Long cartId, Long productSizeId, Long quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Cart cart = getCart(cartId);
        ProductSize size = productSizeService.getProductSize(productSizeId);
        if (!size.getProduct().getStatus().equals(EProductStatus.IN_STOCK) || size.isDeleted()) {
            throw new ResourceNotFoundException("Product not found or out of stock");
        }
        if (size.getStock() < quantity) {
            throw new ResourceNotFoundException("Not enough inventory");
        }
        CartItem CItem = cartItemRepository.findByCartIdAndProductSizeId(cartId, productSizeId)
                .map(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    return cartItem;
                })
                .orElse(CartItem.builder()
                    .cart(cart)
                    .productSize(size)
                    .product(size.getProduct())
                    .quantity(quantity)
                    .addedAt(LocalDateTime.now())
                    .build());
        cart.addToCart(CItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productSizeId) {
        Cart cart = getCart(cartId);
        CartItem cartItem = getCartItem(cartId, productSizeId);
        cart.removeFromCart(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemsFromCart(Long cartId, List<Long> productSizeIds) {
        cartItemRepository.deleteAllByCartIdAndProductSizeIdIn(cartId, productSizeIds);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productSizeId, Long quantity) {
        Cart cart = getCart(cartId);
        CartItem cartItem = getCartItem(cartId, productSizeId);
        ProductSize size = cartItem.getProductSize();
        if (size.getStock() < quantity) {
            throw new ResourceNotFoundException("Not enough inventory");
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public void changeSize(Long cartId, Long oldProductSizeId, Long newProductSizeId) {
        Cart cart = getCart(cartId);
        CartItem oldCartItem = getCartItem(cartId, oldProductSizeId);
        ProductSize size = productSizeService.getProductSize(newProductSizeId);
        if (oldProductSizeId.equals(newProductSizeId)) {
            throw new AlreadyExistException("Product size is already in your cart");
        }
        if (!oldCartItem.getProductSize().getProduct().getId().equals(size.getProduct().getId())) {
            throw new ResourceNotFoundException("Product size not found for this product");
        }
        if (!size.getProduct().getStatus().equals(EProductStatus.IN_STOCK) || size.isDeleted()) {
            throw new ResourceNotFoundException("Product is not available or out of stock");
        }
        if (size.getStock() < 1) {
            throw new ResourceNotFoundException("Not enough inventory");
        }
        CartItem newCartItem = cartItemRepository.findByCartIdAndProductSizeId(cartId, newProductSizeId)
                .map(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    return cartItem;
                })
                .orElse(CartItem.builder()
                    .cart(cart)
                    .productSize(size)
                    .product(size.getProduct())
                    .quantity(1L)
                    .addedAt(LocalDateTime.now())
                    .build());
        cart.removeFromCart(oldCartItem);
        cart.addToCart(newCartItem);
        cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productSizeId) {
        return cartItemRepository.findByCartIdAndProductSizeId(cartId, productSizeId).orElseThrow(() -> new ResourceNotFoundException("Product not found in your cart"));
    }

    @Override
    public CartItem getCartItemById(Long cartId, Long cartItemId) {
        return cartItemRepository.findByIdAndCartId(cartItemId, cartId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    @Override
    public CartResponse convertToCartResponse(Cart cart) {
        return modelMapper.map(cart, CartResponse.class);
    }

    @Override
    public void onProductSizeChange(Long productSizeId) {
        cartItemRepository.deleteAllByByProductSizeId(productSizeId);
    }
}
