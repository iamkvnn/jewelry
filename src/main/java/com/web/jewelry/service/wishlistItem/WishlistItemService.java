package com.web.jewelry.service.WishlistItem;

import com.web.jewelry.dto.request.WishlistItemRequest;
import com.web.jewelry.dto.response.WishlistItemResponse;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.User;
import com.web.jewelry.model.WishlistItem;
import com.web.jewelry.repository.WishlistItemRepository;
import com.web.jewelry.service.product.IProductService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class WishlistItemService implements IWishlistItemService{
    private final WishlistItemRepository wishlistItemRepository;
    private final IUserService userService;
    private final IProductService productService;
    private final ModelMapper modelMapper;

    @Override
    public WishlistItem updateWishlistItem(Long id, WishlistItemRequest wishlistItemRequest) {
        return wishlistItemRepository.findById(id).map(
                wishlistItem -> {
                    Product product = productService.getProductById(wishlistItemRequest.getProduct().getId());
                    wishlistItem.setProduct(product);
                    return wishlistItemRepository.save(wishlistItem);
                }).orElseThrow(() -> new ResourceNotFoundException("WishlistItem not found"));
    }

    @Override
    public WishlistItem addWishlistItem(Long userId, WishlistItemRequest wishlistItemRequest) {
        Customer customer = (Customer) userService.getCustomerById(userId);
        if (customer != null) {
            if(wishlistItemRepository.findByCustomerIdAndProductId(userId, wishlistItemRequest.getProduct().getId()).isPresent()) {
                throw new RuntimeException("WishlistItem already exists");
            }
            else{
                Product product =  productService.getProductById(wishlistItemRequest.getProduct().getId());
                return wishlistItemRepository.save(WishlistItem.builder()
                        .customer(customer)
                        .product(product)
                        .addedAt(LocalDateTime.now())
                        .build());
            }
        }
        else {
            throw new BadRequestException("Customer not found");
        }
    }

    @Override
    public void deleteWishlistItem(Long id) {
        wishlistItemRepository.deleteById(id);
    }

    @Override
    public Page<WishlistItem> getCustomerWishlistItems(Pageable pageable) {
        // lấy id user trong token
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User customer = userService.getCustomerByUsername(username);

        if(customer != null) {
            System.out.println("DEBUG: " + customer.getId().toString());
            return wishlistItemRepository.findAllByCustomerId(customer.getId(), pageable);
        }
        throw new BadRequestException("Customer not found");
    }

    @Override
    public WishlistItem getWishlistItemById(Long id) {
        return wishlistItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem not found"));
    }

    @Override
    public WishlistItemResponse convertToResponse(WishlistItem wishlistItem) {
        return modelMapper.map(wishlistItem, WishlistItemResponse.class);
    }

    @Override
    public Page<WishlistItemResponse> convertToResponse(Page<WishlistItem> wishlistItemPage) {
        return wishlistItemPage.map(this::convertToResponse);
    }
}
