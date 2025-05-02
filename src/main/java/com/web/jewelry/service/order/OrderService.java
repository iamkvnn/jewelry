package com.web.jewelry.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.ReturnItemRequest;
import com.web.jewelry.dto.request.ReturnOrderRequest;
import com.web.jewelry.dto.response.*;
import com.web.jewelry.enums.*;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.CustomerRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.repository.ReturnItemRepository;
import com.web.jewelry.service.address.IAddressService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.payment.CODPaymentService;
import com.web.jewelry.service.payment.MomoPaymentService;
import com.web.jewelry.service.payment.VNPayPaymentService;
import com.web.jewelry.service.productSize.IProductSizeService;
import com.web.jewelry.service.user.IUserService;
import com.web.jewelry.service.voucher.IVoucherService;
import com.web.jewelry.utils.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
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
    private final ReturnItemRepository returnItemRepository;
    private final IVoucherService voucherService;
    private final IProductSizeService productSizeService;
    private final IAddressService addressService;
    private final ICartService cartService;
    private final IUserService userService;
    private final INotificationService notificationService;
    private final CODPaymentService codPaymentService;
    private final MomoPaymentService momoPaymentService;
    private final VNPayPaymentService vnPayPaymentService;
    private final CustomerRepository customerRepository;
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
        List<Voucher> vouchers = validateVouchers(order.getCustomer().getId(), orderRequest);
        validateOrder(orderRequest, orderItems, vouchers);
        order.setOrderItems(orderItems);
        order.setVouchers(vouchers.stream()
                .map(voucher -> OrderVoucher.builder()
                        .order(order)
                        .voucher(voucher)
                        .customerId(order.getCustomer().getId())
                        .build())
                .toList());
        Order newOrder = orderRepository.save(order);
        if (newOrder.getPaymentMethod() == EPaymentMethod.COD) {
            newOrder.setCodPayment(codPaymentService.createPayment(order));
        }
        else if (newOrder.getPaymentMethod() == EPaymentMethod.MOMO) {
            newOrder.setMomoPayment(momoPaymentService.createPayment(order));
        } else if (newOrder.getPaymentMethod() == EPaymentMethod.VN_PAY) {
            newOrder.setVnPayPayment(vnPayPaymentService.createPayment(order));
        }
        return newOrder;
    }

    private List<Voucher> validateVouchers(Long customerId, OrderRequest orderRequest) {
        List<Voucher> vouchers = voucherService.validateVouchers(orderRequest);
        vouchers.forEach(voucher -> {
                        Long used = voucherService.countUsedByVoucherCodeAndCustomerId(voucher.getCode(), customerId);
                        if (used >= voucher.getLimitUsePerCustomer()) {
                            throw new BadRequestException("Voucher " + voucher.getCode() + " has been used up");
                        }
                        voucherService.decreaseVoucherQuantity(voucher.getId());
                });
        return vouchers;
    }

    private void validateOrder(OrderRequest orderRequest, List<OrderItem> orderItems, List<Voucher> vouchers) {
        Long total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(0L, Long::sum);
        if (!total.equals(orderRequest.getTotalProductPrice())) {
            throw new BadRequestException("Total product price is not correct");
        }
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
    }

    private Order initializeOrder(OrderRequest orderRequest, Cart cart, Address address) {
        LocalDateTime orderDate = LocalDateTime.now();
        String orderCode = OrderIdGenerator.getInstance().generateCode(orderDate);
        while (orderRepository.existsById(orderCode)) {
            orderCode = OrderIdGenerator.getInstance().generateCode(orderDate);
        }
        return Order.builder()
                .id(orderCode)
                .shippingAddress(address)
                .shippingMethod(orderRequest.getShippingMethod())
                .paymentMethod(orderRequest.getPaymentMethod())
                .status(EOrderStatus.PENDING)
                .note(orderRequest.getNote())
                .orderDate(orderDate)
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
                    productSizeMap.put(sizeId, size);
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
        productSizeService.updateStockAndSold(productSizeMap.values().stream().toList());
        cartService.removeItemsFromCart(productSizeIds);
        return items;
    }

    @Override
    public Page<Order> getOrders(EOrderStatus status, Long page, Long size) {
        if (status == null) {
            return orderRepository.findAll(PageRequest.of(page.intValue() - 1, size.intValue()));
        } else {
            return orderRepository.findByStatus(status, PageRequest.of(page.intValue() - 1, size.intValue()));
        }
    }

    @Override
    public Order getOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userService.getCurrentUser();
        if (!Objects.equals(order.getCustomer().getId(), user.getId()) && !user.getRole().equals(EUserRole.STAFF)) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order;
    }

    @Override
    public Page<Order> getMyOrders(EOrderStatus status, Long page, Long size) {
        Long customerId = userService.getCurrentUser().getId();
        if (status == null) {
            return orderRepository.findByCustomerId(customerId, PageRequest.of(page.intValue() - 1, size.intValue()));
        } else {
            return orderRepository.findByCustomerIdAndStatus(customerId, status, PageRequest.of(page.intValue() - 1, size.intValue()));
        }
    }

    @Override
    public Order updateOrderStatus(String orderId, EOrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        if (status.equals(EOrderStatus.COMPLETED)){
            Customer customer = order.getCustomer();
            customer.setTotalSpent(customer.getTotalSpent() + order.getTotalPrice());
            customer.setMembershipRank(calcRank(customer.getTotalSpent()));
            customerRepository.save(customer);
        }
        return orderRepository.save(order);
    }

    private EMembershiprank calcRank(Long totalSpent) {
        if (totalSpent < 7000000) {
            return EMembershiprank.MEMBER;
        } else if (totalSpent < 15000000) {
            return EMembershiprank.SILVER;
        } else if (totalSpent < 30000000) {
            return EMembershiprank.GOLD;
        } else if (totalSpent < 50000000) {
            return EMembershiprank.PLATINUM;
        } else {
            return EMembershiprank.DIAMOND;
        }
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

    @Transactional
    @Override
    public void returnOrderItem(ReturnOrderRequest request) {
        Order order = getOrder(request.getOrderId());
        if (order.getStatus() != EOrderStatus.DELIVERED) {
            throw new BadRequestException("Order is not delivered yet");
        }
        List<ReturnItemRequest> returnItems = request.getItems();
        if (returnItems == null || returnItems.isEmpty()) {
            throw new BadRequestException("Return items cannot be empty");
        }
        Set<Long> itemIds = returnItems.stream()
                .map(ReturnItemRequest::getItemId)
                .collect(Collectors.toSet());
        if (itemIds.size() != returnItems.size()) {
            throw new BadRequestException("Return items cannot be duplicated");
        }

        Map<Long, OrderItem> orderItemMap = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        returnItems.forEach(returnItem -> {
                    OrderItem orderItem = orderItemMap.get(returnItem.getItemId());
                    if (orderItem == null) {
                        throw new ResourceNotFoundException("Item with ID " + returnItem.getItemId() + " not found in this order");
                    }

                    if (orderItem.getQuantity() < returnItem.getQuantity()) {
                        throw new BadRequestException("Return quantity for item " + orderItem.getId() + " cannot be greater than order quantity");
                    }
                    returnItemRepository.save(ReturnItem.builder()
                            .quantity(returnItem.getQuantity())
                            .reason(returnItem.getReturnReason())
                            .description(returnItem.getDescription())
                            .orderItem(orderItem)
                            .build());
                });
        order.setStatus(EOrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);
        notificationService.sendNotificationToAllStaff(NotificationRequest.builder()
                .title("Có yêu cầu trả hàng mới!")
                .content("Khách hàng " + order.getCustomer().getFullName() + " đã gửi một yêu cầu trả hàng với đơn hàng " + order.getId() +
                        ".\n Vui lòng kiểm tra lại đơn hàng và xác nhận yêu cầu trả hàng.")
                .build());
        notificationService.sendNotificationToAllManager(NotificationRequest.builder()
                .title("Có yêu cầu trả hàng mới!")
                .content("Khách hàng " + order.getCustomer().getFullName() + " đã gửi một yêu cầu trả hàng với đơn hàng " + order.getId())
                .build());
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
        PaymentResponse paymentResponse = switch (order.getPaymentMethod()) {
            case COD -> modelMapper.map(order.getCodPayment(), PaymentResponse.class);
            case MOMO -> modelMapper.map(order.getMomoPayment(), PaymentResponse.class);
            case VN_PAY -> modelMapper.map(order.getVnPayPayment(), PaymentResponse.class);
        };
        order.setCodPayment(null);
        order.setMomoPayment(null);
        order.setVnPayPayment(null);
        OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class);
        orderResponse.setPayment(paymentResponse);
        return orderResponse;
    }

    @Override
    public Page<OrderResponse> convertToResponse(Page<Order> orders) {
        return orders.map(this::convertToResponse);
    }
}
