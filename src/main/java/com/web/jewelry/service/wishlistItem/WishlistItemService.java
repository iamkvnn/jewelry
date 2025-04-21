package com.web.jewelry.service.wishlistItem;

import com.web.jewelry.dto.request.WishlistItemRequest;
import com.web.jewelry.dto.response.WishlistItemResponse;
import com.web.jewelry.exception.AlreadyExistException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class WishlistItemService implements IWishlistItemService {
    private final WishlistItemRepository wishlistItemRepository;
    private final IUserService userService;
    private final IProductService productService;
    private final ModelMapper modelMapper;

    @Override
    public WishlistItem addWishlistItem(WishlistItemRequest wishlistItemRequest) {
        Customer customer = (Customer) userService.getCurrentUser();
        if(wishlistItemRepository.findByCustomerIdAndProductId(customer.getId(), wishlistItemRequest.getProduct().getId()).isPresent()) {
            throw new AlreadyExistException("WishlistItem already exists");
        }
        Product product = productService.getProductById(wishlistItemRequest.getProduct().getId());
        return wishlistItemRepository.save(WishlistItem.builder()
                .customer(customer)
                .product(product)
                .addedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    @Override
    public void deleteWishlistItem(Long id) {
        Long customerId = userService.getCurrentUser().getId();
        wishlistItemRepository.deleteByProductIdAndCustomerId(id, customerId);
    }

    @Override
    public Page<WishlistItem> getCustomerWishlistItems(Pageable pageable) {
        User customer = userService.getCurrentUser();
        return wishlistItemRepository.findAllByCustomerId(customer.getId(), pageable);
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
