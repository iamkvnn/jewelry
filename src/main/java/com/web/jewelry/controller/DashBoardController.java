package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.service.dashboard.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/dashboard")
public class DashBoardController {
    private final IDashboardService dashboardService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getTotalRevenue(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getRevenue(month, year))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/total-orders")
    public ResponseEntity<ApiResponse> getTotalOrders(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getTotalOrders(month, year))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/new-customers")
    public ResponseEntity<ApiResponse> getTotalNewCustomers(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getTotalNewCustomers(month, year))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/monthly-revenue")
    public ResponseEntity<ApiResponse> getMonthlyRevenue(@RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getMonthlyRevenue(year))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/top-selling-products")
    public ResponseEntity<ApiResponse> getTopSellingProducts(@RequestParam int month, @RequestParam int year, @RequestParam(required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getTopSellingProducts(month, year, limit))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/latest-orders")
    public  ResponseEntity<ApiResponse> getLatestOrders(@RequestParam int month, @RequestParam int year, @RequestParam(required = false, defaultValue = "5" ) int limit) {
        return ResponseEntity.ok(new ApiResponse("200", "Success", dashboardService.getLatestOrders(month, year, limit)));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse> getRevenueByCategory(@RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getRevenueByCategory(year))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/product-sold-by-category")
    public ResponseEntity<ApiResponse> getProductSoldByCategory(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(new ApiResponse("200", "Success", dashboardService.getProductSoldByCategory(month, year)));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/total-return-orders")
    public ResponseEntity<ApiResponse> getTotalReturnOrders(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse("200", "Success", dashboardService.getTotalReturnOrders(month, year))
        );
    }
}
