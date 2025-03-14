package com.web.jewelry.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.jewelry.dto.response.*;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.OrderItemRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.address.IAddressService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.productSize.IProductSizeService;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final IProductSizeService productSizeService;
    private final IAddressService addressService;
    private final ICartService cartService;
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
    public Order placeOrder(Long customerId) {
        Cart cart = cartService.getCartByCustomerId(customerId);
        Address address = addressService.getCustomerDefaultAddress(customerId);
        Order order = Order.builder()
                .customer(cart.getCustomer())
                .shippingAddress(address)
                .shippingMethod(EShippingMethod.STANDARD)
                .status(EOrderStatus.CHECKOUT)
                .paymentMethod(EPaymentMethod.COD)
                .isReviewed(false)
                .orderDate(LocalDateTime.now())
                .build();
        List<OrderItem> orderItems = createOrderItems(order, cart);
        Long total = orderItems.stream()
                .map(orderItem -> orderItem.getProductSize().getPrice() * orderItem.getQuantity())
                .reduce(0L, Long::sum);
        order.setTotalProductPrice(total);
        Long shippingFee = getEstimateShippingFee(address.getDistrict(), address.getProvince(), EShippingMethod.STANDARD);
        order.setShippingFee(shippingFee);
        order.setTotalPrice(total + shippingFee);
        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cartService.getCheckedItem(cart.getId()).stream()
                .filter(item -> !item.isInCheckout())
                .map(CItem -> {
                    cartService.setCheckout(CItem.getId());
                    productSizeService.decreaseStock(CItem.getProductSize().getId(), CItem.getQuantity());
                    productSizeService.increaseSold(CItem.getProductSize().getId(), CItem.getQuantity());
                    return OrderItem.builder()
                            .order(order)
                            .productSize(CItem.getProductSize())
                            .quantity(CItem.getQuantity())
                            .cartItem(CItem)
                            .build();
                })
                .toList();
    }

    @Override
    public Order updateOrderInfo(Long orderId, Long customerId, String note, EPaymentMethod paymentMethod, EShippingMethod shippingMethod) {
        Order order = getOrder(orderId);
        if (order.getStatus() != EOrderStatus.CHECKOUT) {
            throw new BadRequestException("Cannot update order that is not in checkout status");
        }
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        order.setNote(note);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : order.getPaymentMethod());
        if(shippingMethod != null && !order.getShippingMethod().equals(shippingMethod)) {
            order.setShippingMethod(shippingMethod);
            order.setShippingFee(getEstimateShippingFee(order.getShippingAddress().getDistrict(), order.getShippingAddress().getProvince(), shippingMethod));
            order.setTotalPrice(order.getTotalProductPrice() + order.getShippingFee());
        }
        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public void completeOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.setStatus(EOrderStatus.PENDING);
        order.getOrderItems().forEach(orderItem -> {
            cartService.completeCheckout(orderItem.getCartItem().getId());
            orderItem.setCartItem(null);
            orderItemRepository.save(orderItem);
        });
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void cancelCheckout(Long customerId, Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != EOrderStatus.CHECKOUT) {
            throw new BadRequestException("Cannot cancel order that is not in checkout status");
        }
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        order.getOrderItems().forEach(orderItem -> {
            productSizeService.increaseStock(orderItem.getProductSize().getId(), orderItem.getQuantity());
            productSizeService.decreaseSold(orderItem.getProductSize().getId(), orderItem.getQuantity());
            cartService.cancelCheckout(orderItem.getCartItem().getId());
        });
        orderRepository.delete(order);
    }

    @Override
    public Page<Order> getOrders(Long page, Long size) {
        return orderRepository.findAll(PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public Page<Order> getCustomerOrders(Long customerId, Long page, Long size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @Override
    public Page<Order> getMyOrders() {
        return null;
    }

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
        OrderResponse response = modelMapper.map(order, OrderResponse.class);
        Set<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemResponse item = modelMapper.map(orderItem, OrderItemResponse.class);
                    ProductSize productSize = orderItem.getProductSize();
                    item.setProduct(modelMapper.map(productSize.getProduct(), ProductResponse.class));
                    item.setTotalPrice(productSize.getPrice() * orderItem.getQuantity());
                    return item;
                })
                .collect(Collectors.toSet());
        response.setOrderItems(orderItems);
        return response;
    }

    @Override
    public Page<OrderResponse> convertToResponse(Page<Order> orders) {
        return orders.map(this::convertToResponse);
    }

}
