package com.web.jewelry.service.cart;

import com.web.jewelry.dto.response.CartItemResponse;
import com.web.jewelry.dto.response.CartResponse;
import com.web.jewelry.enums.EProductStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.CartItemRepository;
import com.web.jewelry.repository.CartRepository;
import com.web.jewelry.service.product.IProductService;
import com.web.jewelry.service.productSize.IProductSizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService implements ICartService{
    private final IProductSizeService productSizeService;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ModelMapper modelMapper;

    @Override
    public Cart getCart(Long id) {
        return unCheckAllItems(id);
    }

    private Cart getCartById(Long id) {
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
        Cart cart = getCartById(cartId);
        ProductSize size = productSizeService.getProductSize(productSizeId);
        if (size.getProduct().getStatus().equals(EProductStatus.NOT_AVAILABLE)) {
            throw new ResourceNotFoundException("Product is not available");
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
                    .quantity(quantity)
                    .isChecked(false)
                    .isInCheckout(false)
                    .addedAt(LocalDateTime.now())
                    .build());
        cart.addToCart(CItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productSizeId) {
        Cart cart = getCartById(cartId);
        CartItem cartItem = getCartItem(cartId, productSizeId);
        cart.removeFromCart(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productSizeId, Long quantity) {
        Cart cart = getCartById(cartId);
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
        Cart cart = getCartById(cartId);
        CartItem oldCartItem = getCartItem(cartId, oldProductSizeId);
        ProductSize size = productSizeService.getProductSize(newProductSizeId);
        if (oldProductSizeId.equals(newProductSizeId)) {
            throw new AlreadyExistException("Product size is already in your cart");
        }
        if (!oldCartItem.getProductSize().getProduct().getId().equals(size.getProduct().getId())) {
            throw new ResourceNotFoundException("Product size not found for this product");
        }
        if (size.getProduct().getStatus().equals(EProductStatus.NOT_AVAILABLE)) {
            throw new ResourceNotFoundException("Product is not available");
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
                    .quantity(1L)
                    .isChecked(false)
                    .isInCheckout(false)
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
        return cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    @Override
    public Cart checkItem(Long cartId, Long productSizeId) {
        CartItem cartItem = getCartItem(cartId, productSizeId);
        if (cartItem.isChecked() || cartItem.isInCheckout()) {
            throw new AlreadyExistException("Item is already checked");
        }
        cartItem.setChecked(true);
        Cart cart = cartItem.getCart();
        cart.setTotalPrice(cart.getTotalPrice() + cartItem.getProductSize().getPrice() * cartItem.getQuantity());
        cartItemRepository.save(cartItem);
        return cartRepository.save(cart);
    }

    @Override
    public Cart unCheckItem(Long cartId, Long productSizeId) {
        CartItem cartItem = getCartItem(cartId, productSizeId);
        if (!cartItem.isChecked() || cartItem.isInCheckout()) {
            throw new AlreadyExistException("Item is already unchecked");
        }
        cartItem.setChecked(false);
        Cart cart = cartItem.getCart();
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductSize().getPrice() * cartItem.getQuantity());
        cartItemRepository.save(cartItem);
        return cartRepository.save(cart);
    }

    @Override
    public Cart checkAllItems(Long cartId) {
        Cart cart = getCartById(cartId);
        cart.setTotalPrice(cart.getCartItems().stream()
                .filter(cartItem -> !cartItem.isChecked() && !cartItem.isInCheckout())
                .map(cartItem -> {
                    cartItem.setChecked(true);
                    return cartItem.getProductSize().getPrice() * cartItem.getQuantity();
                })
                .reduce(0L, Long::sum));
        return cartRepository.save(cart);
    }

    @Override
    public Cart unCheckAllItems(Long cartId) {
        Cart cart = getCartById(cartId);
        cart.setTotalPrice(0L);
        cart.getCartItems().forEach(cartItem -> cartItem.setChecked(false));
        return cartRepository.save(cart);
    }

    @Override
    public List<CartItem> getCheckedItem(Long cartId) {
        List<CartItem> checked = cartItemRepository.findAllByCartIdAndIsChecked(cartId, true);
        if (checked.isEmpty()) {
            throw new ResourceNotFoundException("No item checked");
        }
        return checked;
    }

    @Override
    public void setCheckout(Long cartItemId) {
        CartItem cartItem = getCartItemById(cartItemId);
        Cart cart = cartItem.getCart();
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductSize().getPrice() * cartItem.getQuantity());
        cartItem.setInCheckout(true);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void cancelCheckout(Long cartItemId) {
        CartItem cartItem = getCartItemById(cartItemId);
        Cart cart = cartItem.getCart();
        cart.setTotalPrice(cart.getTotalPrice() + cartItem.getProductSize().getPrice() * cartItem.getQuantity());
        cartItem.setInCheckout(false);
        cartItem.setOrderItem(null);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void completeCheckout(Long cartItemId) {
        CartItem cartItem = getCartItemById(cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Override
    public CartResponse convertToCartResponse(Cart cart) {
        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        cartResponse.setCartItems(cart.getCartItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toSet()));
        return cartResponse;
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = modelMapper.map(cartItem, CartItemResponse.class);
        ProductSize size = cartItem.getProductSize();
        cartItemResponse.setProduct(productService.convertToProductResponse(size.getProduct()));
        cartItemResponse.setTotalPrice(size.getPrice() * cartItem.getQuantity());
        return cartItemResponse;
    }
}
