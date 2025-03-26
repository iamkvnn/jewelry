package com.web.jewelry.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.response.*;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.address.IAddressService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.productSize.IProductSizeService;
import com.web.jewelry.service.user.IUserService;
import com.web.jewelry.service.voucher.IVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final IVoucherService voucherService;
    private final IProductSizeService productSizeService;
    private final IAddressService addressService;
    private final ICartService cartService;
    private final IUserService userService;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Value("${ghtk.url.fee}")
    private String url;
    @Value("${ghtk.Token}")
    private String token;
    @Value("${ghtk.X-Client-Source}")
    private String clientSource;

    @Transactional
    @Override
    public Order placeOrder(OrderRequest orderRequest) {
        Cart cart = cartService.getMyCart();
        Address address = addressService.getAddressById(orderRequest.getShippingAddress().getId());
        Order order = initializeOrder(orderRequest, cart, address);
        List<OrderItem> orderItems = createOrderItems(order, cart, orderRequest);
        Long total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(0L, Long::sum);
        if (!total.equals(orderRequest.getTotalProductPrice())) {
            throw new BadRequestException("Total product price is not correct");
        }
        List<Voucher> vouchers = voucherService.validateVouchers(orderRequest);
        vouchers.forEach(voucher -> {
            if (voucher.getType() == EVoucherType.FREESHIP) {
                Long discount = voucher.getDiscountRate() * orderRequest.getShippingFee() > voucher.getApplyLimit() ?
                        voucher.getApplyLimit() : voucher.getDiscountRate() * orderRequest.getShippingFee();
                if (!discount.equals(orderRequest.getFreeShipDiscount())) {
                    throw new BadRequestException("Free ship discount is not correct");
                }
            } else if (voucher.getType() == EVoucherType.PROMOTION) {
                Long discount = voucher.getDiscountRate() * orderRequest.getTotalProductPrice() > voucher.getApplyLimit() ?
                        voucher.getApplyLimit() : voucher.getDiscountRate() * orderRequest.getTotalProductPrice();
                if (!discount.equals(orderRequest.getPromotionDiscount())) {
                    throw new BadRequestException("Promotion discount is not correct");
                }
            }
        });
        if (!orderRequest.getTotalPrice().equals(total + orderRequest.getShippingFee() - orderRequest.getFreeShipDiscount() - orderRequest.getPromotionDiscount())) {
            throw new BadRequestException("Total price is not correct");
        }
        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    private Order initializeOrder(OrderRequest orderRequest, Cart cart, Address address) {
        return Order.builder()
                .shippingAddress(address)
                .shippingMethod(orderRequest.getShippingMethod())
                .paymentMethod(orderRequest.getPaymentMethod())
                .status(EOrderStatus.PENDING)
                .note(orderRequest.getNote())
                .orderDate(LocalDateTime.now())
                .customer(cart.getCustomer())
                .isReviewed(false)
                .totalProductPrice(orderRequest.getTotalProductPrice())
                .shippingFee(orderRequest.getShippingFee())
                .freeShipDiscount(orderRequest.getFreeShipDiscount())
                .promotionDiscount(orderRequest.getPromotionDiscount())
                .totalPrice(orderRequest.getTotalPrice())
                .build();
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart, OrderRequest request) {
        List<Long> productSizeIds = request.getCartItems().stream()
                .map(cartItem -> cartItem.getProductSize().getId())
                .toList();
        Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProductSize().getId(),
                        item -> item
                ));
        if (!cartItemMap.keySet().containsAll(productSizeIds)) {
            throw new ResourceNotFoundException("Product not found in your cart");
        }
        List<ProductSize> productSizes = productSizeService.getProductSizesByIds(productSizeIds);
        Map<Long, ProductSize> productSizeMap = productSizes.stream()
                .collect(Collectors.toMap(ProductSize::getId, size -> size));

        List<OrderItem> items = request.getCartItems().stream()
                .map(item -> {
                    Long sizeId = item.getProductSize().getId();
                    CartItem cartItem = cartItemMap.get(sizeId);
                    ProductSize size = productSizeMap.get(sizeId);
                    if (size.getStock() < cartItem.getQuantity()) {
                        throw new ResourceNotFoundException("Not enough stock for product: " + size.getProduct().getTitle() + " - Size: " + size.getSize());
                    }
                    size.setStock(size.getStock() - cartItem.getQuantity());
                    size.setSold(size.getSold() + cartItem.getQuantity());
                    return OrderItem.builder()
                            .order(order)
                            .productSize(size)
                            .product(size.getProduct())
                            .price(size.getPrice())
                            .discountPrice(size.getDiscountPrice())
                            .totalPrice(size.getDiscountPrice() * cartItem.getQuantity())
                            .quantity(cartItem.getQuantity())
                            .build();
                })
                .toList();
        productSizeService.updateStockAndSold(productSizes);
        cartService.removeItemsFromCart(productSizeIds);
        return items;
    }

    @Override
    public Page<Order> getOrders(Long page, Long size) {
        return orderRepository.findAll(PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @PostAuthorize("returnObject.customer.email == authentication.name")
    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public Page<Order> getMyOrders(Long page, Long size) {
        Long customerId = userService.getCurrentUser().getId();
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @PostAuthorize("returnObject.customer.email == authentication.name")
    @Override
    public Order updateOrderStatus(Long orderId, EOrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Long getEstimateShippingFee(String district, String province, EShippingMethod method) {
        Map<String, Object> requestBody = getBody(district, province, method);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Token", token);
        headers.add("X-Client-Source", clientSource);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.path("fee").path("fee").asLong();
        } catch (Exception e) {
            throw new BadRequestException("Cannot get shipping fee");
        }
    }

    private Map<String, Object> getBody(String district, String province, EShippingMethod method) {
        String transport = switch (method) {
            case STANDARD -> "road";
            case EXPRESS -> "fly";
        };
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pick_province", "TP. Hồ Chí Minh");
        requestBody.put("pick_district", "Thành phố Thủ Đức");
        requestBody.put("province", province);
        requestBody.put("district", district);
        requestBody.put("weight", 150);
        requestBody.put("transport", transport);
        requestBody.put("deliver_option", "none");
        return requestBody;
    }

    @Override
    public OrderResponse convertToResponse(Order order) {
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public Page<OrderResponse> convertToResponse(Page<Order> orders) {
        return orders.map(this::convertToResponse);
    }
}
