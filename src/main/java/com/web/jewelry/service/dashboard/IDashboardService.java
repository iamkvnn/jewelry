package com.web.jewelry.service.dashboard;

import com.web.jewelry.dto.response.*;

import java.util.List;

public interface IDashboardService {
    Long getTotalOrders(int month, int year);
    Long getRevenue(int month, int year);
    List<MonthlyCategoryRevenueResponse> getRevenueByCategory(int year);
    List<ProductSoldByCategoryResponse> getProductSoldByCategory(int month, int year);
    List<TopSellingProductResponse> getTopSellingProducts(int month, int year, int limit);
    List<MonthlyRevenueResponse> getMonthlyRevenue(int year);
    List<LatestOrderResponse> getLatestOrders(int month, int year, int limit);
    Long getTotalNewCustomers(int month, int year);
    Long getTotalReturnOrders(int month, int year);
}
