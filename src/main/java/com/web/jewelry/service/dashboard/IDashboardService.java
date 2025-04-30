package com.web.jewelry.service.dashboard;

import com.web.jewelry.dto.response.MonthlyCategoryRevenueResponse;
import com.web.jewelry.dto.response.MonthlyRevenueResponse;
import com.web.jewelry.dto.response.TopSellingProductResponse;

import java.util.List;

public interface IDashboardService {
    Long getTotalCustomers();
    Long getTotalOrders(int month, int year);
    Long getTotalProducts();
    Long getTotalCategories();
    Long getTotalCollections();
    Long getRevenue(int month, int year);
    List<MonthlyCategoryRevenueResponse> getRevenueByCategory(int year);
    List<TopSellingProductResponse> getTopSellingProducts(int month, int year, int limit);
    List<MonthlyRevenueResponse> getMonthlyRevenue(int year);
    Long getTotalNewCustomers(int month, int year);
    Long getTotalReturnOrders(int month, int year);
}
