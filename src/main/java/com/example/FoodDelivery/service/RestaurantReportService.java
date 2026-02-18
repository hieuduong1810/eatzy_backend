package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.*;
import com.example.FoodDelivery.domain.res.report.*;
import com.example.FoodDelivery.repository.*;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestaurantReportService {

        private final OrderRepository orderRepository;
        private final ReviewRepository reviewRepository;
        private final RestaurantService restaurantService;
        private final DishRepository dishRepository;
        private final OrderEarningsSummaryRepository orderEarningsSummaryRepository;
        private final RestaurantRepository restaurantRepository;
        private final UserService userService;

        public RestaurantReportService(OrderRepository orderRepository,
                        ReviewRepository reviewRepository,
                        RestaurantService restaurantService,
                        DishRepository dishRepository,
                        OrderEarningsSummaryRepository orderEarningsSummaryRepository,
                        RestaurantRepository restaurantRepository,
                        UserService userService) {
                this.orderRepository = orderRepository;
                this.reviewRepository = reviewRepository;
                this.restaurantService = restaurantService;
                this.dishRepository = dishRepository;
                this.orderEarningsSummaryRepository = orderEarningsSummaryRepository;
                this.restaurantRepository = restaurantRepository;
                this.userService = userService;
        }

        /**
         * Get the restaurant owned by the currently logged-in user
         */
        public Restaurant getCurrentUserRestaurant() throws IdInvalidException {
                String email = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

                User currentUser = userService.handleGetUserByUsername(email);
                if (currentUser == null) {
                        throw new IdInvalidException("User not found");
                }

                Optional<Restaurant> restaurant = restaurantRepository.findByOwnerId(currentUser.getId());
                if (restaurant.isEmpty()) {
                        throw new IdInvalidException("No restaurant found for current user");
                }

                return restaurant.get();
        }

        /**
         * Get full dashboard report for a restaurant
         */
        public FullReportDTO getFullReport(Long restaurantId, Instant startDate, Instant endDate)
                        throws IdInvalidException {
                Restaurant restaurant = validateRestaurant(restaurantId);

                List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                                restaurantId, startDate, endDate);

                // Calculate totals
                BigDecimal totalRevenue = orders.stream()
                                .filter(o -> "DELIVERED".equals(o.getOrderStatus()))
                                .map(o -> o.getSubtotal() != null ? o.getSubtotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                int totalOrders = orders.size();
                int completedOrders = (int) orders.stream()
                                .filter(o -> "DELIVERED".equals(o.getOrderStatus()))
                                .count();
                int cancelledOrders = (int) orders.stream()
                                .filter(o -> "CANCELLED".equals(o.getOrderStatus())
                                                || "REJECTED".equals(o.getOrderStatus()))
                                .count();

                BigDecimal cancelRate = totalOrders > 0
                                ? BigDecimal.valueOf(cancelledOrders).multiply(BigDecimal.valueOf(100))
                                                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                BigDecimal averageOrderValue = totalOrders > 0
                                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Calculate net revenue from order earnings summaries
                BigDecimal netRevenue = orders.stream()
                                .map(o -> orderEarningsSummaryRepository.findByOrderId(o.getId()))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(s -> s.getRestaurantNetEarning() != null ? s.getRestaurantNetEarning()
                                                : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Get review stats
                BigDecimal averageRating = restaurant.getAverageRating() != null ? restaurant.getAverageRating()
                                : BigDecimal.ZERO;
                int totalReviews = getTotalReviewCount(restaurant);

                // Get top performing dish
                String topPerformingDish = getTopPerformingDishName(restaurantId, orders);

                // Generate revenue chart
                List<RevenueReportItemDTO> revenueChart = getRevenueReport(restaurantId, startDate, endDate);

                // Generate order status breakdown
                List<OrderStatusBreakdownDTO> orderStatusBreakdown = generateOrderStatusBreakdown(orders);

                return FullReportDTO.builder()
                                .totalRevenue(totalRevenue)
                                .netRevenue(netRevenue)
                                .totalOrders(totalOrders)
                                .completedOrders(completedOrders)
                                .cancelledOrders(cancelledOrders)
                                .cancelRate(cancelRate)
                                .averageOrderValue(averageOrderValue)
                                .averageRating(averageRating)
                                .totalReviews(totalReviews)
                                .topPerformingDish(topPerformingDish)
                                .revenueChart(revenueChart)
                                .orderStatusBreakdown(orderStatusBreakdown)
                                .build();
        }

        /**
         * Get revenue breakdown by day
         */
        public List<RevenueReportItemDTO> getRevenueReport(Long restaurantId, Instant startDate, Instant endDate)
                        throws IdInvalidException {
                validateRestaurant(restaurantId);

                List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                                restaurantId, startDate, endDate);

                // Group orders by date
                Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                                .filter(o -> o.getCreatedAt() != null)
                                .collect(Collectors.groupingBy(
                                                o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()));

                // Get earnings summaries for all orders
                Map<Long, OrderEarningsSummary> earningsByOrderId = orders.stream()
                                .map(o -> orderEarningsSummaryRepository.findByOrderId(o.getId()))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toMap(s -> s.getOrder().getId(), s -> s, (a, b) -> a));

                List<RevenueReportItemDTO> result = new ArrayList<>();

                // Generate data for each date in range
                LocalDate start = startDate.atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate end = endDate.atZone(ZoneId.systemDefault()).toLocalDate();

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                        List<Order> dayOrders = ordersByDate.getOrDefault(date, Collections.emptyList());

                        BigDecimal foodRevenue = BigDecimal.ZERO;
                        BigDecimal deliveryFee = BigDecimal.ZERO;
                        BigDecimal discountAmount = BigDecimal.ZERO;
                        BigDecimal commissionAmount = BigDecimal.ZERO;
                        BigDecimal netRevenue = BigDecimal.ZERO;

                        for (Order order : dayOrders) {
                                // Only count revenue for delivered orders
                                if ("DELIVERED".equals(order.getOrderStatus())) {
                                        foodRevenue = foodRevenue.add(
                                                        order.getSubtotal() != null ? order.getSubtotal()
                                                                        : BigDecimal.ZERO);
                                        deliveryFee = deliveryFee
                                                        .add(order.getDeliveryFee() != null ? order.getDeliveryFee()
                                                                        : BigDecimal.ZERO);
                                        discountAmount = discountAmount
                                                        .add(order.getDiscountAmount() != null
                                                                        ? order.getDiscountAmount()
                                                                        : BigDecimal.ZERO);

                                        OrderEarningsSummary summary = earningsByOrderId.get(order.getId());
                                        if (summary != null) {
                                                commissionAmount = commissionAmount.add(
                                                                summary.getRestaurantCommissionAmount() != null
                                                                                ? summary.getRestaurantCommissionAmount()
                                                                                : BigDecimal.ZERO);
                                                netRevenue = netRevenue
                                                                .add(summary.getRestaurantNetEarning() != null
                                                                                ? summary.getRestaurantNetEarning()
                                                                                : BigDecimal.ZERO);
                                        }
                                }
                        }

                        result.add(RevenueReportItemDTO.builder()
                                        .date(date)
                                        .foodRevenue(foodRevenue)
                                        .deliveryFee(deliveryFee)
                                        .discountAmount(discountAmount)
                                        .commissionAmount(commissionAmount)
                                        .netRevenue(netRevenue)
                                        .totalOrders(dayOrders.size())
                                        .build());
                }

                return result;
        }

        /**
         * Get orders report with details
         */
        public List<OrderReportItemDTO> getOrdersReport(Long restaurantId, Instant startDate, Instant endDate)
                        throws IdInvalidException {
                validateRestaurant(restaurantId);

                List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                                restaurantId, startDate, endDate);

                return orders.stream().map(this::convertToOrderReportItem).collect(Collectors.toList());
        }

        /**
         * Get menu analytics for a restaurant
         */
        public MenuSummaryDTO getMenuAnalytics(Long restaurantId) throws IdInvalidException {
                Restaurant restaurant = validateRestaurant(restaurantId);

                List<Dish> dishes = dishRepository.findByRestaurantId(restaurantId);

                int totalDishes = dishes.size();
                int activeDishes = (int) dishes.stream()
                                .filter(d -> d.getAvailabilityQuantity() > 0)
                                .count();
                int outOfStockDishes = totalDishes - activeDishes;

                // Get all orders for this restaurant to calculate dish stats
                List<Order> orders = orderRepository.findByRestaurantId(restaurantId);

                // Calculate dish analytics
                List<MenuAnalyticsItemDTO> dishAnalytics = dishes.stream()
                                .map(dish -> calculateDishAnalytics(dish, orders))
                                .collect(Collectors.toList());

                // Sort by total revenue
                dishAnalytics.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));

                // Get top 5 and improvement dishes (the rest)
                List<MenuAnalyticsItemDTO> topSellingDishes = dishAnalytics.stream()
                                .limit(5)
                                .collect(Collectors.toList());

                List<MenuAnalyticsItemDTO> lowPerformingDishes = dishAnalytics.stream()
                                .skip(5)
                                .collect(Collectors.toList());

                // Calculate category breakdown
                List<CategoryAnalyticsItemDTO> categoryBreakdown = calculateCategoryBreakdown(dishes, dishAnalytics);

                return MenuSummaryDTO.builder()
                                .totalDishes(totalDishes)
                                .activeDishes(activeDishes)
                                .outOfStockDishes(outOfStockDishes)
                                .topSellingDishes(topSellingDishes)
                                .lowPerformingDishes(lowPerformingDishes)
                                .categoryBreakdown(categoryBreakdown)
                                .build();
        }

        /**
         * Get review summary for a restaurant
         */
        public ReviewSummaryDTO getReviewSummary(Long restaurantId) throws IdInvalidException {
                Restaurant restaurant = validateRestaurant(restaurantId);

                String targetName = restaurant.getName();

                List<Review> reviews = reviewRepository.findByReviewTargetAndTargetNameOrderByCreatedAtDesc(
                                "RESTAURANT", targetName);

                int totalReviews = reviews.size();
                BigDecimal averageRating = restaurant.getAverageRating() != null
                                ? restaurant.getAverageRating()
                                : BigDecimal.ZERO;

                // Get rating distribution
                RatingDistributionDTO ratingDistribution = RatingDistributionDTO.builder()
                                .oneStar(restaurant.getOneStarCount() != null ? restaurant.getOneStarCount() : 0)
                                .twoStar(restaurant.getTwoStarCount() != null ? restaurant.getTwoStarCount() : 0)
                                .threeStar(restaurant.getThreeStarCount() != null ? restaurant.getThreeStarCount() : 0)
                                .fourStar(restaurant.getFourStarCount() != null ? restaurant.getFourStarCount() : 0)
                                .fiveStar(restaurant.getFiveStarCount() != null ? restaurant.getFiveStarCount() : 0)
                                .build();

                // Get recent reviews (limit 20)
                List<ReviewReportItemDTO> recentReviews = reviews.stream()
                                .limit(20)
                                .map(this::convertToReviewReportItem)
                                .collect(Collectors.toList());

                // Calculate response rate
                Long repliedCount = reviewRepository.countByReviewTargetAndTargetNameAndReplyIsNotNull(
                                "RESTAURANT", targetName);
                BigDecimal responseRate = totalReviews > 0
                                ? BigDecimal.valueOf(repliedCount).multiply(BigDecimal.valueOf(100))
                                                .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Average response time (simplified - just return a placeholder)
                BigDecimal averageResponseTime = BigDecimal.valueOf(45);

                return ReviewSummaryDTO.builder()
                                .averageRating(averageRating)
                                .totalReviews(totalReviews)
                                .ratingDistribution(ratingDistribution)
                                .recentReviews(recentReviews)
                                .responseRate(responseRate)
                                .averageResponseTime(averageResponseTime)
                                .build();
        }

        // ==================== HELPER METHODS ====================

        private Restaurant validateRestaurant(Long restaurantId) throws IdInvalidException {
                Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
                if (restaurant == null) {
                        throw new IdInvalidException("Restaurant not found with id: " + restaurantId);
                }
                return restaurant;
        }

        private int getTotalReviewCount(Restaurant restaurant) {
                int count = 0;
                if (restaurant.getOneStarCount() != null)
                        count += restaurant.getOneStarCount();
                if (restaurant.getTwoStarCount() != null)
                        count += restaurant.getTwoStarCount();
                if (restaurant.getThreeStarCount() != null)
                        count += restaurant.getThreeStarCount();
                if (restaurant.getFourStarCount() != null)
                        count += restaurant.getFourStarCount();
                if (restaurant.getFiveStarCount() != null)
                        count += restaurant.getFiveStarCount();
                return count;
        }

        private String getTopPerformingDishName(Long restaurantId, List<Order> orders) {
                Map<Long, Integer> dishOrderCount = new HashMap<>();

                for (Order order : orders) {
                        if (order.getOrderItems() != null) {
                                for (OrderItem item : order.getOrderItems()) {
                                        if (item.getDish() != null) {
                                                Long dishId = item.getDish().getId();
                                                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                                                dishOrderCount.merge(dishId, quantity, Integer::sum);
                                        }
                                }
                        }
                }

                if (dishOrderCount.isEmpty()) {
                        return "N/A";
                }

                Long topDishId = dishOrderCount.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(null);

                if (topDishId != null) {
                        return dishRepository.findById(topDishId)
                                        .map(Dish::getName)
                                        .orElse("N/A");
                }

                return "N/A";
        }

        private List<OrderStatusBreakdownDTO> generateOrderStatusBreakdown(List<Order> orders) {
                Map<String, Long> statusCounts = orders.stream()
                                .filter(o -> o.getOrderStatus() != null)
                                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

                int total = orders.size();

                return statusCounts.entrySet().stream()
                                .map(entry -> OrderStatusBreakdownDTO.builder()
                                                .status(entry.getKey())
                                                .count(entry.getValue().intValue())
                                                .percent(total > 0
                                                                ? BigDecimal.valueOf(entry.getValue() * 100.0 / total)
                                                                                .setScale(2, RoundingMode.HALF_UP)
                                                                : BigDecimal.ZERO)
                                                .build())
                                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                                .collect(Collectors.toList());
        }

        private OrderReportItemDTO convertToOrderReportItem(Order order) {
                int itemsCount = 0;
                if (order.getOrderItems() != null) {
                        itemsCount = order.getOrderItems().stream()
                                        .mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 1)
                                        .sum();
                }

                String customerName = "N/A";
                String customerPhone = "N/A";
                if (order.getCustomer() != null) {
                        customerName = order.getCustomer().getName() != null ? order.getCustomer().getName() : "N/A";
                        customerPhone = order.getCustomer().getPhoneNumber() != null
                                        ? order.getCustomer().getPhoneNumber()
                                        : "N/A";
                }

                return OrderReportItemDTO.builder()
                                .id(order.getId())
                                .orderCode("EZ" + order.getId())
                                .customerName(customerName)
                                .customerPhone(customerPhone)
                                .orderTime(order.getCreatedAt())
                                .deliveredTime(order.getDeliveredAt())
                                .status(order.getOrderStatus())
                                .paymentMethod(order.getPaymentMethod())
                                .paymentStatus(order.getPaymentStatus())
                                .subtotal(order.getSubtotal())
                                .deliveryFee(order.getDeliveryFee())
                                .discountAmount(order.getDiscountAmount())
                                .totalAmount(order.getTotalAmount())
                                .itemsCount(itemsCount)
                                .cancellationReason(order.getCancellationReason())
                                .build();
        }

        private MenuAnalyticsItemDTO calculateDishAnalytics(Dish dish, List<Order> orders) {
                int totalOrdered = 0;
                BigDecimal totalRevenue = BigDecimal.ZERO;

                for (Order order : orders) {
                        if (order.getOrderItems() != null) {
                                for (OrderItem item : order.getOrderItems()) {
                                        if (item.getDish() != null && item.getDish().getId().equals(dish.getId())) {
                                                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                                                totalOrdered += quantity;

                                                BigDecimal price = item.getPriceAtPurchase() != null
                                                                ? item.getPriceAtPurchase()
                                                                : dish.getPrice();
                                                if (price != null) {
                                                        totalRevenue = totalRevenue.add(
                                                                        price.multiply(BigDecimal.valueOf(quantity)));
                                                }
                                        }
                                }
                        }
                }

                String categoryName = dish.getCategory() != null ? dish.getCategory().getName() : "Uncategorized";

                return MenuAnalyticsItemDTO.builder()
                                .dishId(dish.getId())
                                .dishName(dish.getName())
                                .categoryName(categoryName)
                                .imageUrl(dish.getImageUrl())
                                .price(dish.getPrice())
                                .totalOrdered(totalOrdered)
                                .totalRevenue(totalRevenue)
                                .averageRating(BigDecimal.ZERO) // No dish-level rating in current schema
                                .reviewCount(0)
                                .trend("stable")
                                .trendPercent(BigDecimal.ZERO)
                                .build();
        }

        private List<CategoryAnalyticsItemDTO> calculateCategoryBreakdown(List<Dish> dishes,
                        List<MenuAnalyticsItemDTO> dishAnalytics) {
                Map<Long, CategoryAnalyticsItemDTO> categoryMap = new HashMap<>();

                BigDecimal totalRevenue = dishAnalytics.stream()
                                .map(MenuAnalyticsItemDTO::getTotalRevenue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                for (Dish dish : dishes) {
                        Long categoryId = dish.getCategory() != null ? dish.getCategory().getId() : 0L;
                        String categoryName = dish.getCategory() != null ? dish.getCategory().getName()
                                        : "Uncategorized";

                        MenuAnalyticsItemDTO analytics = dishAnalytics.stream()
                                        .filter(a -> a.getDishId().equals(dish.getId()))
                                        .findFirst()
                                        .orElse(null);

                        if (analytics != null) {
                                CategoryAnalyticsItemDTO existing = categoryMap.get(categoryId);
                                if (existing == null) {
                                        existing = CategoryAnalyticsItemDTO.builder()
                                                        .categoryId(categoryId)
                                                        .categoryName(categoryName)
                                                        .totalDishes(0)
                                                        .totalOrdered(0)
                                                        .totalRevenue(BigDecimal.ZERO)
                                                        .percentOfTotal(BigDecimal.ZERO)
                                                        .build();
                                        categoryMap.put(categoryId, existing);
                                }

                                existing.setTotalDishes(existing.getTotalDishes() + 1);
                                existing.setTotalOrdered(existing.getTotalOrdered() + analytics.getTotalOrdered());
                                existing.setTotalRevenue(existing.getTotalRevenue().add(analytics.getTotalRevenue()));
                        }
                }

                // Calculate percentages
                return categoryMap.values().stream()
                                .peek(c -> {
                                        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                                                c.setPercentOfTotal(c.getTotalRevenue()
                                                                .multiply(BigDecimal.valueOf(100))
                                                                .divide(totalRevenue, 2, RoundingMode.HALF_UP));
                                        }
                                })
                                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                                .collect(Collectors.toList());
        }

        private ReviewReportItemDTO convertToReviewReportItem(Review review) {
                List<String> dishNames = new ArrayList<>();
                String orderCode = "N/A";
                Long orderId = null;

                if (review.getOrder() != null) {
                        orderId = review.getOrder().getId();
                        orderCode = "EZ" + orderId;

                        if (review.getOrder().getOrderItems() != null) {
                                dishNames = review.getOrder().getOrderItems().stream()
                                                .filter(i -> i.getDish() != null)
                                                .map(i -> i.getDish().getName())
                                                .collect(Collectors.toList());
                        }
                }

                String customerName = review.getCustomer() != null && review.getCustomer().getName() != null
                                ? review.getCustomer().getName()
                                : "N/A";

                return ReviewReportItemDTO.builder()
                                .id(review.getId())
                                .orderId(orderId)
                                .orderCode(orderCode)
                                .customerName(customerName)
                                .rating(review.getRating())
                                .comment(review.getComment())
                                .reply(review.getReply())
                                .dishNames(dishNames)
                                .createdAt(review.getCreatedAt())
                                .build();
        }
}
