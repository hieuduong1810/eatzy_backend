package com.example.FoodDelivery.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.FoodDelivery.domain.res.report.*;
import com.example.FoodDelivery.service.RestaurantReportService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/restaurants/reports")
public class RestaurantReportController {

    private final RestaurantReportService restaurantReportService;

    public RestaurantReportController(RestaurantReportService restaurantReportService) {
        this.restaurantReportService = restaurantReportService;
    }

    /**
     * Get full dashboard report for a restaurant
     * 
     * @param startDate    Start date (ISO 8601 format)
     * @param endDate      End date (ISO 8601 format)
     * @return Full report with revenue, orders, and review stats
     */
    @GetMapping("/full")
    @ApiMessage("Get full dashboard report for restaurant")
    public ResponseEntity<FullReportDTO> getFullReport(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) throws IdInvalidException {
        Long restaurantId = restaurantReportService.getCurrentUserRestaurant().getId();
        FullReportDTO report = restaurantReportService.getFullReport(restaurantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get revenue breakdown by day
     * 
     * @param startDate    Start date (ISO 8601 format)
     * @param endDate      End date (ISO 8601 format)
     * @return List of daily revenue reports
     */
    @GetMapping("/revenue")
    @ApiMessage("Get revenue report for restaurant")
    public ResponseEntity<List<RevenueReportItemDTO>> getRevenueReport(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) throws IdInvalidException {
        Long restaurantId = restaurantReportService.getCurrentUserRestaurant().getId();
        List<RevenueReportItemDTO> report = restaurantReportService.getRevenueReport(restaurantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get orders report with details
     * 
     * @param startDate    Start date (ISO 8601 format)
     * @param endDate      End date (ISO 8601 format)
     * @return List of order details
     */
    @GetMapping("/orders")
    @ApiMessage("Get orders report for restaurant")
    public ResponseEntity<List<OrderReportItemDTO>> getOrdersReport(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) throws IdInvalidException {
        Long restaurantId = restaurantReportService.getCurrentUserRestaurant().getId();
        List<OrderReportItemDTO> report = restaurantReportService.getOrdersReport(restaurantId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get menu analytics for a restaurant
     * 
     * @return Menu summary with top/low performing dishes and category breakdown
     */
    @GetMapping("/menu")
    @ApiMessage("Get menu analytics for restaurant")
    public ResponseEntity<MenuSummaryDTO> getMenuAnalytics() throws IdInvalidException {
        Long restaurantId = restaurantReportService.getCurrentUserRestaurant().getId();
        MenuSummaryDTO summary = restaurantReportService.getMenuAnalytics(restaurantId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get review summary for a restaurant
     * 
     * @return Review summary with rating distribution and recent reviews
     */
    @GetMapping("/reviews")
    @ApiMessage("Get review summary for restaurant")
    public ResponseEntity<ReviewSummaryDTO> getReviewSummary() throws IdInvalidException {
        Long restaurantId = restaurantReportService.getCurrentUserRestaurant().getId();
        ReviewSummaryDTO summary = restaurantReportService.getReviewSummary(restaurantId);
        return ResponseEntity.ok(summary);
    }
}
