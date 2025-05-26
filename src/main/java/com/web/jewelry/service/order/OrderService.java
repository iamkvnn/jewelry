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
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.order.orderHandlerChain.*;
import com.web.jewelry.service.order.orderState.*;
import com.web.jewelry.service.payment.PaymentServiceFactory;
import com.web.jewelry.service.user.IUserService;
import jakarta.annotation.PostConstruct;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderInitializeHandler orderInitializeHandler;
    private final OrderItemCreationHandler orderItemCreationHandler;
    private final VoucherValidationHandler voucherValidationHandler;
    private final OrderValidationHandler orderValidationHandler;
    private final OrderCompletionHandler orderCompletionHandler;
    private final ReturnItemRepository returnItemRepository;
    private final IUserService userService;
    private final INotificationService notificationService;
    private final PaymentServiceFactory paymentServiceFactory;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        orderInitializeHandler.setNext(orderItemCreationHandler);
        orderItemCreationHandler.setNext(voucherValidationHandler);
        voucherValidationHandler.setNext(orderValidationHandler);
        orderValidationHandler.setNext(orderCompletionHandler);
    }

    @Value("${ghtk.url.fee}")
    private String url;
    @Value("${ghtk.Token}")
    private String token;
    @Value("${ghtk.X-Client-Source}")
    private String clientSource;

    @Transactional
    @Override
    public Order placeOrder(OrderRequest orderRequest) {
        OrderHandlerContext context = new OrderHandlerContext();
        context.setOrderRequest(orderRequest);
        Order order = orderInitializeHandler.process(context);
        if (order.getPaymentMethod() == EPaymentMethod.COD) {
            order.setCodPayment((CODPayment) paymentServiceFactory.getPaymentService(EPaymentMethod.COD).createPayment(order));
        }
        else if (order.getPaymentMethod() == EPaymentMethod.MOMO) {
            order.setMomoPayment((MomoPayment) paymentServiceFactory.getPaymentService(EPaymentMethod.MOMO).createPayment(order));
        } else if (order.getPaymentMethod() == EPaymentMethod.VN_PAY) {
            order.setVnPayPayment((VNPayPayment) paymentServiceFactory.getPaymentService(EPaymentMethod.VN_PAY).createPayment(order));
        }
        return order;
    }

    @Override
    public Page<Order> getOrders(EOrderStatus status, Long page, Long size, String query) {
        return orderRepository.findByQuery(status, query, PageRequest.of(page.intValue() - 1, size.intValue()));
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

    @Transactional
    @Override
    public Order updateOrderStatus(String orderId, EOrderStatus status) {
        Order order = getOrder(orderId);
        OrderStateContext context = new OrderStateContext(order);
        switch (status) {
            case CONFIRMED:
                context.confirmOrder();
                break;
            case SHIPPING:
                context.shipOrder();
                break;
            case DELIVERED:
                context.deliverOrder();
                break;
            case COMPLETED:
                context.completeOrder();
                Customer customer = order.getCustomer();
                customer.setTotalSpent(customer.getTotalSpent() + order.getTotalPrice());
                customer.setMembershipRank(calcRank(customer.getTotalSpent()));
                customerRepository.save(customer);
                break;
            case CANCELLED:
                context.cancelOrder();
                break;
            case RETURN_REQUESTED:
                context.returnOrder();
                break;
            case RETURNED:
                context.acceptReturn();
                break;
            case RETURN_REJECTED:
                context.rejectReturn();
                break;
            default:
                throw new BadRequestException("Unknown order status: " + status);
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
