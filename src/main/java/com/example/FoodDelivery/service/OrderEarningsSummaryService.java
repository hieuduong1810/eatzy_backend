package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.OrderEarningsSummary;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.OrderEarningsSummaryRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class OrderEarningsSummaryService {
    private final OrderEarningsSummaryRepository orderEarningsSummaryRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final SystemConfigurationService systemConfigurationService;

    public OrderEarningsSummaryService(OrderEarningsSummaryRepository orderEarningsSummaryRepository,
            OrderService orderService,
            UserService userService,
            RestaurantService restaurantService,
            SystemConfigurationService systemConfigurationService) {
        this.orderEarningsSummaryRepository = orderEarningsSummaryRepository;
        this.orderService = orderService;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.systemConfigurationService = systemConfigurationService;
    }

    public OrderEarningsSummary getOrderEarningsSummaryById(Long id) {
        Optional<OrderEarningsSummary> summaryOpt = this.orderEarningsSummaryRepository.findById(id);
        return summaryOpt.orElse(null);
    }

    public OrderEarningsSummary getOrderEarningsSummaryByOrderId(Long orderId) {
        Optional<OrderEarningsSummary> summaryOpt = this.orderEarningsSummaryRepository.findByOrderId(orderId);
        return summaryOpt.orElse(null);
    }

    public List<OrderEarningsSummary> getOrderEarningsSummariesByDriverId(Long driverId) {
        return this.orderEarningsSummaryRepository.findByDriverId(driverId);
    }

    public List<OrderEarningsSummary> getOrderEarningsSummariesByRestaurantId(Long restaurantId) {
        return this.orderEarningsSummaryRepository.findByRestaurantId(restaurantId);
    }

    public List<OrderEarningsSummary> getOrderEarningsSummariesByDateRange(Instant startDate, Instant endDate) {
        return this.orderEarningsSummaryRepository.findByRecordedAtBetween(startDate, endDate);
    }

    public BigDecimal getTotalDriverEarnings(Long driverId) {
        BigDecimal total = this.orderEarningsSummaryRepository.sumDriverEarnings(driverId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRestaurantEarnings(Long restaurantId) {
        BigDecimal total = this.orderEarningsSummaryRepository.sumRestaurantEarnings(restaurantId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPlatformEarnings(Instant startDate, Instant endDate) {
        BigDecimal total = this.orderEarningsSummaryRepository.sumPlatformEarnings(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public OrderEarningsSummary createOrderEarningsSummary(OrderEarningsSummary orderEarningsSummary)
            throws IdInvalidException {
        // check order exists
        if (orderEarningsSummary.getOrder() != null) {
            Order order = this.orderService.getOrderById(orderEarningsSummary.getOrder().getId());
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + orderEarningsSummary.getOrder().getId());
            }

            // check if summary already exists for this order
            if (this.getOrderEarningsSummaryByOrderId(order.getId()) != null) {
                throw new IdInvalidException("Earnings summary already exists for order id: " + order.getId());
            }

            orderEarningsSummary.setOrder(order);
        } else {
            throw new IdInvalidException("Order is required");
        }

        // check driver exists
        if (orderEarningsSummary.getDriver() != null) {
            User driver = this.userService.getUserById(orderEarningsSummary.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + orderEarningsSummary.getDriver().getId());
            }
            orderEarningsSummary.setDriver(driver);
        }

        // check restaurant exists
        if (orderEarningsSummary.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService
                    .getRestaurantById(orderEarningsSummary.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException(
                        "Restaurant not found with id: " + orderEarningsSummary.getRestaurant().getId());
            }
            orderEarningsSummary.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        orderEarningsSummary.setRecordedAt(Instant.now());
        return orderEarningsSummaryRepository.save(orderEarningsSummary);
    }

    @Transactional
    public OrderEarningsSummary createOrderEarningsSummaryFromOrder(Long orderId) throws IdInvalidException {
        Order order = this.orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // check if summary already exists
        if (this.getOrderEarningsSummaryByOrderId(orderId) != null) {
            throw new IdInvalidException("Earnings summary already exists for order id: " + orderId);
        }

        // get restaurant commission rate from restaurant or system config
        BigDecimal restaurantCommissionRate = order.getRestaurant().getCommissionRate();
        if (restaurantCommissionRate == null) {
            // get from system configuration
            SystemConfiguration restaurantConfig = systemConfigurationService
                    .getSystemConfigurationByKey("RESTAURANT_COMMISSION_RATE");
            if (restaurantConfig != null && restaurantConfig.getConfigValue() != null) {
                restaurantCommissionRate = new BigDecimal(restaurantConfig.getConfigValue());
            } else {
                restaurantCommissionRate = new BigDecimal("15.00"); // default 15%
            }
        }

        // get driver commission rate from system configuration
        BigDecimal driverCommissionRate;
        SystemConfiguration driverConfig = systemConfigurationService
                .getSystemConfigurationByKey("DRIVER_COMMISSION_RATE");
        if (driverConfig != null && driverConfig.getConfigValue() != null) {
            driverCommissionRate = new BigDecimal(driverConfig.getConfigValue());
        } else {
            driverCommissionRate = new BigDecimal("80.00"); // default 80%
        }

        // calculate earnings
        BigDecimal orderSubtotal = order.getSubtotal();
        BigDecimal deliveryFee = order.getDeliveryFee();

        // restaurant commission
        BigDecimal restaurantCommissionAmount = orderSubtotal
                .multiply(restaurantCommissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal restaurantNetEarning = orderSubtotal.subtract(restaurantCommissionAmount);

        // driver earnings
        BigDecimal driverCommissionAmount = deliveryFee
                .multiply(driverCommissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal driverNetEarning = deliveryFee.subtract(driverCommissionAmount);

        // platform earnings
        BigDecimal platformTotalEarning = restaurantCommissionAmount
                .add(driverCommissionAmount);

        OrderEarningsSummary summary = OrderEarningsSummary.builder()
                .order(order)
                .driver(order.getDriver())
                .restaurant(order.getRestaurant())
                .orderSubtotal(orderSubtotal)
                .deliveryFee(deliveryFee)
                .restaurantCommissionRate(restaurantCommissionRate)
                .restaurantCommissionAmount(restaurantCommissionAmount)
                .restaurantNetEarning(restaurantNetEarning)
                .driverCommissionRate(driverCommissionRate)
                .driverCommissionAmount(driverCommissionAmount)
                .driverNetEarning(driverNetEarning)
                .platformTotalEarning(platformTotalEarning)
                .recordedAt(Instant.now())
                .build();

        return orderEarningsSummaryRepository.save(summary);
    }

    @Transactional
    public OrderEarningsSummary updateOrderEarningsSummary(OrderEarningsSummary orderEarningsSummary)
            throws IdInvalidException {
        // check id
        OrderEarningsSummary currentSummary = getOrderEarningsSummaryById(orderEarningsSummary.getId());
        if (currentSummary == null) {
            throw new IdInvalidException("Order earnings summary not found with id: " + orderEarningsSummary.getId());
        }

        // update fields
        if (orderEarningsSummary.getOrderSubtotal() != null) {
            currentSummary.setOrderSubtotal(orderEarningsSummary.getOrderSubtotal());
        }
        if (orderEarningsSummary.getDeliveryFee() != null) {
            currentSummary.setDeliveryFee(orderEarningsSummary.getDeliveryFee());
        }
        if (orderEarningsSummary.getRestaurantCommissionRate() != null) {
            currentSummary.setRestaurantCommissionRate(orderEarningsSummary.getRestaurantCommissionRate());
        }
        if (orderEarningsSummary.getRestaurantCommissionAmount() != null) {
            currentSummary.setRestaurantCommissionAmount(orderEarningsSummary.getRestaurantCommissionAmount());
        }
        if (orderEarningsSummary.getRestaurantNetEarning() != null) {
            currentSummary.setRestaurantNetEarning(orderEarningsSummary.getRestaurantNetEarning());
        }
        if (orderEarningsSummary.getDriverCommissionRate() != null) {
            currentSummary.setDriverCommissionRate(orderEarningsSummary.getDriverCommissionRate());
        }
        if (orderEarningsSummary.getDriverCommissionAmount() != null) {
            currentSummary.setDriverCommissionAmount(orderEarningsSummary.getDriverCommissionAmount());
        }
        if (orderEarningsSummary.getDriverNetEarning() != null) {
            currentSummary.setDriverNetEarning(orderEarningsSummary.getDriverNetEarning());
        }
        if (orderEarningsSummary.getPlatformTotalEarning() != null) {
            currentSummary.setPlatformTotalEarning(orderEarningsSummary.getPlatformTotalEarning());
        }

        return orderEarningsSummaryRepository.save(currentSummary);
    }

    public ResultPaginationDTO getAllOrderEarningsSummaries(Specification<OrderEarningsSummary> spec,
            Pageable pageable) {
        Page<OrderEarningsSummary> page = this.orderEarningsSummaryRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteOrderEarningsSummary(Long id) {
        this.orderEarningsSummaryRepository.deleteById(id);
    }
}
