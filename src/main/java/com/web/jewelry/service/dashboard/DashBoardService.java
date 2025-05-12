package com.web.jewelry.service.dashboard;

import com.web.jewelry.dto.response.*;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.OrderItem;
import com.web.jewelry.repository.*;
import com.web.jewelry.service.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DashBoardService implements IDashboardService{
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final AddressService addressService;

    @Override
    public Long getTotalOrders(int month, int year) {
        return orderRepository.countByOrderDateBetween(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year, month + 1, 1, 0, 0)
        );
    }

    @Override
    public Long getRevenue(int month, int year) {
        return orderRepository.sumTotalPriceByOrderDateBetween(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year, month + 1, 1, 0, 0)
        );
    }

    @Override
    public List<MonthlyCategoryRevenueResponse> getRevenueByCategory(int year) {
        List<Order> orders = orderRepository.findByOrderDateBetween(
                LocalDateTime.of(year, 1, 1, 0, 0),
                LocalDateTime.of(year + 1, 1, 1, 0, 0)
        );

        Map<Long, Map<Long, CategoryRevenueItemResponse>> categoryRevenueMap = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            categoryRevenueMap.put((long) month, categoryRepository.findAllWithoutParent().stream()
                    .map(category -> CategoryRevenueItemResponse.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .revenue(0L)
                            .build())
                    .collect(Collectors.toMap(CategoryRevenueItemResponse::getCategoryId, category -> category)));
        }

        orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .forEach(orderItem -> {
                    Long month = (long) orderItem.getOrder().getOrderDate().getMonthValue();
                    Long categoryId = orderItem.getProduct().getCategory().getParent().getId();
                    Long totalPrice = orderItem.getTotalPrice();
                    categoryRevenueMap.get(month).get(categoryId).setRevenue(
                            categoryRevenueMap.get(month).get(categoryId).getRevenue() + totalPrice);
                });

        return categoryRevenueMap.entrySet().stream()
                .map(entry -> MonthlyCategoryRevenueResponse.builder()
                        .month(entry.getKey().intValue())
                        .categoryRevenueItems(entry.getValue().values().stream().toList())
                        .build())
                .sorted(Comparator.comparing(MonthlyCategoryRevenueResponse::getMonth))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductSoldByCategoryResponse> getProductSoldByCategory(int month, int year) {
        List<Order> orders = orderRepository.findByOrderDateBetween(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year + 1, month+1, 1, 0, 0)
        );

        Map<Long, ProductSoldByCategoryResponse> categoryProductMap = new HashMap<>();
        categoryProductMap.putAll(categoryRepository.findAllWithoutParent().stream()
                .map(category -> ProductSoldByCategoryResponse.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .totalSales(0L)
                        .build())
                .collect(Collectors.toMap(
                        ProductSoldByCategoryResponse::getCategoryId,
                        category -> category
                ))
        );
        orders.stream().flatMap(order -> order.getOrderItems().stream())
                .forEach(orderItem -> {
                    Long quantity = orderItem.getQuantity();
                    Long categoryId = orderItem.getProduct().getCategory().getParent().getId();
                    categoryProductMap.get(categoryId).setTotalSales(categoryProductMap.get(categoryId).getTotalSales() + quantity);
                });
        return categoryProductMap.values().stream().toList();
    }

    @Override
    public List<TopSellingProductResponse> getTopSellingProducts(int month, int year, int limit) {
        return orderRepository.findByOrderDateBetween(
                    LocalDateTime.of(year, month, 1, 0, 0),
                    LocalDateTime.of(year, month + 1, 1, 0, 0)
                ).stream()
                    .flatMap(order -> order.getOrderItems().stream())
                    .collect(Collectors.groupingBy(
                                OrderItem::getProduct,
                                Collectors.summingLong(OrderItem::getQuantity)
                    ))
                    .entrySet().stream()
                    .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                    .map(entry -> TopSellingProductResponse.builder()
                            .productId(entry.getKey().getId())
                            .productTitle(entry.getKey().getTitle())
                            .productImage(entry.getKey().getImages().getFirst().getUrl())
                            .totalQuantity(entry.getValue())
                            .build())
                    .limit(limit)
                    .toList();
    }

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue(int year) {
        List<Order> orders = orderRepository.findByOrderDateBetween(
                LocalDateTime.of(year, 1, 1, 0, 0),
                LocalDateTime.of(year + 1, 1, 1, 0, 0)
        );
        Map<Long, MonthlyRevenueResponse> monthlyRevenueMap = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenueMap.put((long) month, MonthlyRevenueResponse.builder()
                    .month(month)
                    .revenue(0L)
                    .build());
        }
        orders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderDate().getMonthValue()))
                .forEach((month, orderList) -> {
                    Long totalRevenue = orderList.stream()
                            .mapToLong(Order::getTotalPrice)
                            .sum();
                    monthlyRevenueMap.get(Long.valueOf(month)).setRevenue(totalRevenue);
                });
        return monthlyRevenueMap.values().stream()
                .sorted(Comparator.comparing(MonthlyRevenueResponse::getMonth))
                .toList();
    }

    @Override
    public List<LatestOrderResponse> getLatestOrders(int month, int year, int limit) {
        List<Order> orders = orderRepository.findByOrderDateBetween(
                LocalDateTime.of(year, 1, 1, 0, 0),
                LocalDateTime.of(year + 1, 1, 1, 0, 0)
        ).stream().sorted(Comparator.comparing(Order::getOrderDate)).limit(limit).toList();
        return  orders.stream().map(order -> LatestOrderResponse.builder()
                .id(order.getId())
                .totalProductPrice(order.getTotalProductPrice())
                .shippingAddress(addressService.convertToResponse(order.getShippingAddress()))
                .shippingMethod(order.getShippingMethod())
                .shippingFee(order.getShippingFee())
                .totalPrice(order.getTotalPrice())
                .freeShipDiscount(order.getFreeShipDiscount())
                .promotionDiscount(order.getPromotionDiscount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .note(order.getNote())
                .orderDate(order.getOrderDate())
                .isReviewed(order.isReviewed())
                .build()
                ).limit(limit).toList();
    }

    @Override
    public Long getTotalNewCustomers(int month, int year) {
        return customerRepository.countByCreatedAtBetween(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year, month + 1, 1, 0, 0)
        );
    }

    @Override
    public Long getTotalReturnOrders(int month, int year) {
        return orderRepository.countByOrderDateBetweenAndReturned(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year, month + 1, 1, 0, 0)
        );
    }
}
