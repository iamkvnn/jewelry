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
import com.web.jewelry.service.user.IUserService;
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
    private final IUserService userService;
    private final ProductSizeObservable productSizeObservable;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        productSizeObservable.addObserver(this);
    }

    @Override
    public Cart getMyCart() {
        Long customerId = userService.getCurrentUser().getId();
        return cartRepository.findByCustomerId(customerId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    @Transactional
    @Override
    public void clearMyCart() {
        Cart cart = getMyCart();
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public void initializeNewCart(Customer customer) {
        cartRepository.save(Cart.builder()
                .customer(customer)
                .build());
    }

    @Override
    public void addItemToCart(Long productSizeId, Long quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Cart cart = getMyCart();
        ProductSize size = productSizeService.getProductSize(productSizeId);
        if (!size.getProduct().getStatus().equals(EProductStatus.IN_STOCK) || size.isDeleted()) {
            throw new ResourceNotFoundException("Product not found or out of stock");
        }
        if (size.getStock() < quantity) {
            throw new ResourceNotFoundException("Not enough inventory");
        }
        CartItem CItem = cartItemRepository.findByCartIdAndProductSizeId(cart.getId(), productSizeId)
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
    public void removeItemFromCart(Long productSizeId) {
        Cart cart = getMyCart();
        CartItem cartItem = getCartItem(cart.getId(), productSizeId);
        cart.removeFromCart(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemsFromCart(List<Long> productSizeIds) {
        Long cartId = getMyCart().getId();
        cartItemRepository.deleteAllByCartIdAndProductSizeIdIn(cartId, productSizeIds);
    }

    @Override
    public void updateItemQuantity(Long productSizeId, Long quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Cart cart = getMyCart();
        CartItem cartItem = getCartItem(cart.getId(), productSizeId);
        ProductSize size = cartItem.getProductSize();
        if (size.getStock() < quantity) {
            throw new ResourceNotFoundException("Not enough inventory");
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public void changeSize(Long oldProductSizeId, Long newProductSizeId) {
        Cart cart = getMyCart();
        CartItem oldCartItem = getCartItem(cart.getId(), oldProductSizeId);
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
        CartItem newCartItem = cartItemRepository.findByCartIdAndProductSizeId(cart.getId(), newProductSizeId)
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
    public CartItem getCartItemById(Long cartItemId) {
        Long cartId = getMyCart().getId();
        return cartItemRepository.findByIdAndCartId(cartItemId, cartId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    @Override
    public CartResponse convertToCartResponse(Cart cart) {
        return modelMapper.map(cart, CartResponse.class);
    }

    @Override
    public void onProductSizeDeleted(Long productSizeId) {
        cartItemRepository.deleteAllByByProductSizeId(productSizeId);
    }
}
