package com.example.FoodDelivery.service;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.OrderEarningsSummary;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Voucher;
import com.example.FoodDelivery.domain.req.ReqOrderDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.repository.OrderEarningsSummaryRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderEarningsSummaryService orderEarningsSummaryService;

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final VoucherService voucherService;
    private final DishService dishService;
    private final MenuOptionRepository menuOptionRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final WebSocketService webSocketService;
    private final SystemConfigurationService systemConfigurationService;
    private final MapboxService mapboxService;
    private final DriverProfileService driverProfileService;
    private final RedisGeoService redisGeoService;
    private final RedisRejectionService redisRejectionService;
    private final OrderEarningsSummaryRepository orderEarningsSummaryRepository;

    public OrderService(OrderRepository orderRepository, UserService userService,
            RestaurantService restaurantService, VoucherService voucherService, DishService dishService,
            MenuOptionRepository menuOptionRepository, @Lazy OrderEarningsSummaryService orderEarningsSummaryService,
            DriverProfileRepository driverProfileRepository,
            PaymentService paymentService,
            VNPayService vnPayService,
            WebSocketService webSocketService,
            SystemConfigurationService systemConfigurationService,
            MapboxService mapboxService,
            @Lazy DriverProfileService driverProfileService,
            RedisGeoService redisGeoService,
            RedisRejectionService redisRejectionService,
            OrderEarningsSummaryRepository orderEarningsSummaryRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.voucherService = voucherService;
        this.dishService = dishService;
        this.menuOptionRepository = menuOptionRepository;
        this.orderEarningsSummaryService = orderEarningsSummaryService;
        this.driverProfileRepository = driverProfileRepository;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.webSocketService = webSocketService;
        this.systemConfigurationService = systemConfigurationService;
        this.mapboxService = mapboxService;
        this.driverProfileService = driverProfileService;
        this.redisGeoService = redisGeoService;
        this.redisRejectionService = redisRejectionService;
        this.orderEarningsSummaryRepository = orderEarningsSummaryRepository;
    }

    private ResOrderDTO convertToResOrderDTO(Order order) {
        ResOrderDTO dto = new ResOrderDTO();
        dto.setId(order.getId());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setDeliveryLatitude(order.getDeliveryLatitude());
        dto.setDeliveryLongitude(order.getDeliveryLongitude());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setSubtotal(order.getSubtotal());

        // Format delivery fee to 2 decimal places
        dto.setDeliveryFee(order.getDeliveryFee() != null
                ? order.getDeliveryFee().setScale(2, java.math.RoundingMode.HALF_UP)
                : null);

        dto.setDiscountAmount(order.getDiscountAmount());

        // Format total amount to 2 decimal places
        dto.setTotalAmount(order.getTotalAmount() != null
                ? order.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP)
                : null);

        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPreparingAt(order.getPreparingAt());
        dto.setDeliveredAt(order.getDeliveredAt());

        // Calculate total trip duration in minutes (from createdAt to deliveredAt)
        if (order.getCreatedAt() != null && order.getDeliveredAt() != null) {
            Duration duration = Duration.between(order.getCreatedAt(), order.getDeliveredAt());
            dto.setTotalTripDuration(duration.toMinutes());
        }

        // Convert customer
        if (order.getCustomer() != null) {
            ResOrderDTO.Customer customer = new ResOrderDTO.Customer();
            customer.setId(order.getCustomer().getId());
            customer.setName(order.getCustomer().getName());
            customer.setPhoneNumber(order.getCustomer().getPhoneNumber());
            dto.setCustomer(customer);
        }

        // Convert restaurant
        if (order.getRestaurant() != null) {
            ResOrderDTO.Restaurant restaurant = new ResOrderDTO.Restaurant();
            restaurant.setId(order.getRestaurant().getId());
            restaurant.setName(order.getRestaurant().getName());
            restaurant.setAddress(order.getRestaurant().getAddress());
            dto.setRestaurant(restaurant);
        }

        BigDecimal distance = mapboxService.getDrivingDistance(
                order.getRestaurant().getLatitude(),
                order.getRestaurant().getLongitude(),
                order.getDeliveryLatitude(),
                order.getDeliveryLongitude());
        // Get distance from order entity and format to 2 decimal places
        dto.setDistance(distance != null
                ? distance.setScale(2, java.math.RoundingMode.HALF_UP)
                : null);

        // Convert driver
        if (order.getDriver() != null) {
            ResOrderDTO.Driver driver = new ResOrderDTO.Driver();
            driver.setId(order.getDriver().getId());
            driver.setName(order.getDriver().getName());
            driver.setPhoneNumber(order.getDriver().getPhoneNumber());

            // Get driver profile for additional information
            Optional<DriverProfile> driverProfileOpt = driverProfileRepository.findByUserId(order.getDriver().getId());
            if (driverProfileOpt.isPresent()) {
                DriverProfile driverProfile = driverProfileOpt.get();
                driver.setVehicleType(driverProfile.getVehicleType());
                driver.setAverageRating(
                        driverProfile.getAverageRating() != null ? driverProfile.getAverageRating().toString() : null);
                driver.setCompletedTrips(
                        driverProfile.getCompletedTrips() != null ? driverProfile.getCompletedTrips().toString()
                                : null);
                driver.setVehicleLicensePlate(driverProfile.getVehicleLicensePlate());
                driver.setVehicleDetails(driverProfile.getVehicleDetails());
            }

            dto.setDriver(driver);
        }

        // Convert voucher
        if (order.getVoucher() != null) {
            ResOrderDTO.Voucher voucher = new ResOrderDTO.Voucher();
            voucher.setId(order.getVoucher().getId());
            voucher.setCode(order.getVoucher().getCode());
            dto.setVoucher(voucher);
        }

        // Convert order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<ResOrderItemDTO> orderItemDtos = order.getOrderItems().stream()
                    .map(orderItem -> {
                        ResOrderItemDTO itemDto = new ResOrderItemDTO();
                        itemDto.setId(orderItem.getId());
                        itemDto.setQuantity(orderItem.getQuantity());
                        itemDto.setPriceAtPurchase(orderItem.getPriceAtPurchase());

                        // Convert dish
                        if (orderItem.getDish() != null) {
                            ResOrderItemDTO.Dish dish = new ResOrderItemDTO.Dish();
                            dish.setId(orderItem.getDish().getId());
                            dish.setName(orderItem.getDish().getName());
                            dish.setPrice(orderItem.getDish().getPrice());
                            itemDto.setDish(dish);
                        }

                        // Convert order item options
                        if (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty()) {
                            List<ResOrderItemOptionDTO> optionDtos = orderItem.getOrderItemOptions().stream()
                                    .map(option -> {
                                        ResOrderItemOptionDTO optionDto = new ResOrderItemOptionDTO();
                                        optionDto.setId(option.getId());

                                        // Convert menu option
                                        if (option.getMenuOption() != null) {
                                            ResOrderItemOptionDTO.MenuOption menuOption = new ResOrderItemOptionDTO.MenuOption();
                                            menuOption.setId(option.getMenuOption().getId());
                                            menuOption.setName(option.getMenuOption().getName());
                                            menuOption.setPriceAdjustment(option.getMenuOption().getPriceAdjustment());
                                            optionDto.setMenuOption(menuOption);
                                        }

                                        return optionDto;
                                    })
                                    .collect(Collectors.toList());
                            itemDto.setOrderItemOptions(optionDtos);
                        }

                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setOrderItems(orderItemDtos);
        }

        // Get earnings summary information
        Optional<OrderEarningsSummary> earningsSummaryOpt = orderEarningsSummaryRepository.findByOrderId(order.getId());
        if (earningsSummaryOpt.isPresent()) {
            OrderEarningsSummary earningsSummary = earningsSummaryOpt.get();
            dto.setRestaurantCommissionAmount(earningsSummary.getRestaurantCommissionAmount());
            dto.setRestaurantNetEarning(earningsSummary.getRestaurantNetEarning());
            dto.setDriverCommissionAmount(earningsSummary.getDriverCommissionAmount());
            dto.setDriverNetEarning(earningsSummary.getDriverNetEarning());
        }

        return dto;
    }

    /**
     * Helper method to calculate delivery fee based on real driving distance
     */
    private BigDecimal calculateDeliveryFee(Restaurant restaurant, BigDecimal deliveryLatitude,
            BigDecimal deliveryLongitude) throws IdInvalidException {
        if (restaurant == null || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new IdInvalidException("Restaurant location is required to calculate delivery fee");
        }

        if (deliveryLatitude == null || deliveryLongitude == null) {
            throw new IdInvalidException("Delivery location is required to calculate delivery fee");
        }

        // Get configuration values
        BigDecimal baseFee = new BigDecimal("15000"); // Default 15,000 VND
        BigDecimal baseDistance = new BigDecimal("3"); // Default 3 km
        BigDecimal perKmFee = new BigDecimal("5000"); // Default 5,000 VND per km

        try {
            SystemConfiguration baseFeeConfig = systemConfigurationService
                    .getSystemConfigurationByKey("DELIVERY_BASE_FEE");
            if (baseFeeConfig != null && baseFeeConfig.getConfigValue() != null
                    && !baseFeeConfig.getConfigValue().isEmpty()) {
                baseFee = new BigDecimal(baseFeeConfig.getConfigValue());
            }

            SystemConfiguration baseDistanceConfig = systemConfigurationService
                    .getSystemConfigurationByKey("DELIVERY_BASE_DISTANCE");
            if (baseDistanceConfig != null && baseDistanceConfig.getConfigValue() != null
                    && !baseDistanceConfig.getConfigValue().isEmpty()) {
                baseDistance = new BigDecimal(baseDistanceConfig.getConfigValue());
            }

            SystemConfiguration perKmFeeConfig = systemConfigurationService
                    .getSystemConfigurationByKey("DELIVERY_PER_KM_FEE");
            if (perKmFeeConfig != null && perKmFeeConfig.getConfigValue() != null
                    && !perKmFeeConfig.getConfigValue().isEmpty()) {
                perKmFee = new BigDecimal(perKmFeeConfig.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Failed to get delivery fee configuration, using defaults", e);
        }

        // Get real driving distance using Mapbox API
        BigDecimal distance = mapboxService.getDrivingDistance(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                deliveryLatitude,
                deliveryLongitude);

        if (distance == null) {
            log.warn("Failed to get driving distance from Mapbox, using base fee");
            return baseFee;
        }

        log.info("Delivery distance: {} km, Base distance: {} km, Base fee: {} VND, Per km fee: {} VND",
                distance, baseDistance, baseFee, perKmFee);

        // Calculate fee: if distance <= baseDistance, return baseFee
        // Otherwise: baseFee + (distance - baseDistance) * perKmFee
        if (distance.compareTo(baseDistance) <= 0) {
            return baseFee;
        } else {
            BigDecimal extraDistance = distance.subtract(baseDistance);
            BigDecimal extraFee = extraDistance.multiply(perKmFee);
            BigDecimal totalFee = baseFee.add(extraFee);
            log.info("Calculated delivery fee: {} VND (base: {} + extra: {} for {} km)",
                    totalFee, baseFee, extraFee, extraDistance);
            return totalFee;
        }
    }

    /**
     * Helper method to calculate voucher discount amount
     */
    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotal, BigDecimal deliveryFee) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }

        // Check if order meets minimum order value requirement
        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            log.warn("Order subtotal {} does not meet minimum order value {} for voucher {}",
                    subtotal, voucher.getMinOrderValue(), voucher.getCode());
            return BigDecimal.ZERO;
        }

        // Check if voucher is still valid (not expired)
        Instant now = Instant.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            log.warn("Voucher {} has not started yet", voucher.getCode());
            return BigDecimal.ZERO;
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            log.warn("Voucher {} has expired", voucher.getCode());
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = BigDecimal.ZERO;

        // Calculate discount based on type
        if ("PERCENTAGE".equals(voucher.getDiscountType())) {
            // Percentage discount: subtotal * (discountValue / 100)
            discountAmount = subtotal
                    .multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            log.info("Calculated percentage discount: {} ({}% of {})",
                    discountAmount, voucher.getDiscountValue(), subtotal);

            // Check if discount exceeds maxDiscountAmount for percentage type
            if (voucher.getMaxDiscountAmount() != null
                    && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
                log.info("Discount amount capped at maxDiscountAmount: {}", voucher.getMaxDiscountAmount());
            }
        } else if ("FIXED".equals(voucher.getDiscountType())) {
            // Fixed discount: use discountValue directly
            discountAmount = voucher.getDiscountValue();
            log.info("Applied fixed discount: {}", discountAmount);
        } else if ("FREESHIP".equals(voucher.getDiscountType())) {
            // Free shipping: discount equals delivery fee
            discountAmount = deliveryFee;
            log.info("Applied free shipping discount: {}", discountAmount);
        }

        // Make sure discount doesn't exceed subtotal
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
            log.warn("Discount amount {} exceeds subtotal {}, capping at subtotal",
                    discountAmount, subtotal);
        }

        return discountAmount;
    }

    /**
     * Helper method to find the closest available driver using Mapbox API for real
     * driving distance
     */
    private DriverProfile findClosestDriverWithMapbox(List<DriverProfile> candidates, Restaurant restaurant) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        // Calculate real driving distance for each candidate using Mapbox API
        DriverProfile closestDriver = null;
        BigDecimal shortestDistance = null;

        for (DriverProfile driver : candidates) {
            if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                continue;
            }

            BigDecimal drivingDistance = mapboxService.getDrivingDistance(
                    driver.getCurrentLatitude(),
                    driver.getCurrentLongitude(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude());

            // If Mapbox API fails for this driver, skip them
            if (drivingDistance == null) {
                log.warn("Failed to get driving distance from Mapbox for driver {}", driver.getUser().getId());
                continue;
            }

            // Check if this is the closest driver so far
            if (shortestDistance == null || drivingDistance.compareTo(shortestDistance) < 0) {
                shortestDistance = drivingDistance;
                closestDriver = driver;
            }
        }

        return closestDriver;
    }

    public Order getOrderById(Long id) {
        Optional<Order> orderOpt = this.orderRepository.findById(id);
        return orderOpt.orElse(null);
    }

    public ResOrderDTO getOrderDTOById(Long id) {
        Order order = getOrderById(id);
        if (order == null) {
            return null;
        }
        return convertToResOrderDTO(order);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return this.orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return this.orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> getOrdersByDriverId(Long driverId) {
        return this.orderRepository.findByDriverId(driverId);
    }

    public List<Order> getOrdersByStatus(String orderStatus) {
        return this.orderRepository.findByOrderStatus(orderStatus);
    }

    public List<Order> getOrdersByCustomerIdAndStatus(Long customerId, String orderStatus) {
        return this.orderRepository.findByCustomerIdAndOrderStatus(customerId, orderStatus);
    }

    public List<ResOrderDTO> getOrdersDTOByRestaurantId(Long restaurantId) {
        List<Order> orders = this.orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return orders.stream()
                .map(this::convertToResOrderDTO)
                .collect(Collectors.toList());
    }

    public List<ResOrderDTO> getOrdersDTOByRestaurantIdAndStatus(Long restaurantId, String orderStatus) {
        List<Order> orders = this.orderRepository.findByRestaurantIdAndOrderStatus(restaurantId, orderStatus);
        return orders.stream()
                .map(this::convertToResOrderDTO)
                .collect(Collectors.toList());
    }

    public List<ResOrderDTO> getOrdersDTOByCustomerId(Long customerId) {
        List<Order> orders = this.orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream()
                .map(this::convertToResOrderDTO)
                .collect(Collectors.toList());
    }

    public List<ResOrderDTO> getOrdersDTOByDriverId(Long driverId) {
        List<Order> orders = this.orderRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        return orders.stream()
                .map(this::convertToResOrderDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResOrderDTO createOrderFromReqDTO(ReqOrderDTO reqOrderDTO, String clientIp, String baseUrl)
            throws IdInvalidException {
        // Create Order entity
        Order order = new Order();

        // Validate and set customer
        if (reqOrderDTO.getCustomer() == null || reqOrderDTO.getCustomer().getId() == null) {
            throw new IdInvalidException("Customer is required");
        }
        User customer = this.userService.getUserById(reqOrderDTO.getCustomer().getId());
        if (customer == null) {
            throw new IdInvalidException("Customer not found with id: " + reqOrderDTO.getCustomer().getId());
        }
        order.setCustomer(customer);

        // Validate and set restaurant
        if (reqOrderDTO.getRestaurant() == null || reqOrderDTO.getRestaurant().getId() == null) {
            throw new IdInvalidException("Restaurant is required");
        }
        Restaurant restaurant = this.restaurantService.getRestaurantById(reqOrderDTO.getRestaurant().getId());
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + reqOrderDTO.getRestaurant().getId());
        }
        order.setRestaurant(restaurant);

        // Set driver if provided
        if (reqOrderDTO.getDriver() != null && reqOrderDTO.getDriver().getId() != null) {
            User driver = this.userService.getUserById(reqOrderDTO.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + reqOrderDTO.getDriver().getId());
            }
            order.setDriver(driver);
        }

        // Set voucher if provided
        if (reqOrderDTO.getVoucher() != null && reqOrderDTO.getVoucher().getId() != null) {
            Voucher voucher = this.voucherService.getVoucherById(reqOrderDTO.getVoucher().getId());

            if (voucher == null) {
                throw new IdInvalidException("Voucher not found with id: " + reqOrderDTO.getVoucher().getId());
            }
            order.setVoucher(voucher);
        }

        // Set order fields
        order.setOrderStatus(reqOrderDTO.getOrderStatus() != null ? reqOrderDTO.getOrderStatus() : "PENDING");
        order.setDeliveryAddress(reqOrderDTO.getDeliveryAddress());
        order.setDeliveryLatitude(reqOrderDTO.getDeliveryLatitude() != null
                ? BigDecimal.valueOf(reqOrderDTO.getDeliveryLatitude())
                : null);
        order.setDeliveryLongitude(reqOrderDTO.getDeliveryLongitude() != null
                ? BigDecimal.valueOf(reqOrderDTO.getDeliveryLongitude())
                : null);
        order.setSpecialInstructions(reqOrderDTO.getSpecialInstructions());
        order.setSubtotal(reqOrderDTO.getSubtotal());
        // Delivery fee will be calculated based on distance and system configuration
        order.setPaymentMethod(reqOrderDTO.getPaymentMethod());
        order.setPaymentStatus(reqOrderDTO.getPaymentStatus() != null ? reqOrderDTO.getPaymentStatus() : "UNPAID");
        order.setCreatedAt(Instant.now());

        // Save order first
        Order savedOrder = orderRepository.save(order);

        // Create order items with options
        BigDecimal subtotal = BigDecimal.ZERO;

        if (reqOrderDTO.getOrderItems() != null && !reqOrderDTO.getOrderItems().isEmpty()) {
            List<OrderItem> orderItems = new ArrayList<>();

            for (ReqOrderDTO.OrderItem reqItem : reqOrderDTO.getOrderItems()) {
                // Validate dish
                if (reqItem.getDish() == null || reqItem.getDish().getId() == null) {
                    throw new IdInvalidException("Dish is required for order item");
                }
                Dish dish = this.dishService.getDishById(reqItem.getDish().getId());
                if (dish == null) {
                    throw new IdInvalidException("Dish not found with id: " + reqItem.getDish().getId());
                }

                // Validate quantity
                if (reqItem.getQuantity() == null || reqItem.getQuantity() <= 0) {
                    throw new IdInvalidException("Quantity must be greater than 0");
                }

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setDish(dish);
                orderItem.setQuantity(reqItem.getQuantity());

                // Calculate price: dish price * quantity
                BigDecimal itemPrice = dish.getPrice().multiply(new BigDecimal(reqItem.getQuantity()));

                // Process order item options
                if (reqItem.getOrderItemOptions() != null && !reqItem.getOrderItemOptions().isEmpty()) {
                    List<OrderItemOption> itemOptions = new ArrayList<>();

                    for (ReqOrderDTO.OrderItem.OrderItemOption reqOption : reqItem.getOrderItemOptions()) {
                        if (reqOption.getMenuOption() == null || reqOption.getMenuOption().getId() == null) {
                            throw new IdInvalidException("Menu option is required");
                        }

                        MenuOption menuOption = this.menuOptionRepository.findById(reqOption.getMenuOption().getId())
                                .orElseThrow(() -> new IdInvalidException(
                                        "Menu option not found with id: " + reqOption.getMenuOption().getId()));

                        // Create order item option
                        OrderItemOption itemOption = new OrderItemOption();
                        itemOption.setOrderItem(orderItem);
                        itemOption.setMenuOption(menuOption);
                        itemOption.setOptionName(menuOption.getName());
                        itemOption.setPriceAtPurchase(menuOption.getPriceAdjustment());

                        itemOptions.add(itemOption);

                        // Add option price * quantity to item price
                        itemPrice = itemPrice.add(
                                menuOption.getPriceAdjustment().multiply(new BigDecimal(reqItem.getQuantity())));
                    }

                    orderItem.setOrderItemOptions(itemOptions);
                }

                // Set final price for order item
                orderItem.setPriceAtPurchase(itemPrice);

                // Add to subtotal
                subtotal = subtotal.add(itemPrice);

                orderItems.add(orderItem);
            }

            savedOrder.setOrderItems(orderItems);
        }

        // Calculate delivery fee based on real driving distance
        savedOrder.setSubtotal(subtotal);
        BigDecimal deliveryFee = calculateDeliveryFee(
                restaurant,
                savedOrder.getDeliveryLatitude(),
                savedOrder.getDeliveryLongitude());
        savedOrder.setDeliveryFee(deliveryFee);

        // Calculate voucher discount (including free shipping)
        BigDecimal discountAmount = calculateVoucherDiscount(savedOrder.getVoucher(), subtotal, deliveryFee);
        savedOrder.setDiscountAmount(discountAmount);

        // Calculate total amount: subtotal + deliveryFee - discountAmount
        BigDecimal totalAmount = subtotal.add(deliveryFee).subtract(discountAmount);
        savedOrder.setTotalAmount(totalAmount);

        log.info("Order {} - Subtotal: {}, Delivery Fee: {}, Discount: {}, Total: {}",
                savedOrder.getId(), subtotal, deliveryFee, discountAmount, totalAmount);

        savedOrder = orderRepository.save(savedOrder);

        // Convert to DTO for response and WebSocket notification
        ResOrderDTO orderDTO = convertToResOrderDTO(savedOrder);

        // Notify restaurant about new order via WebSocket
        webSocketService.notifyRestaurantNewOrder(restaurant.getId(), orderDTO);

        // Process payment based on payment method
        if ("WALLET".equals(savedOrder.getPaymentMethod())) {
            // Process wallet payment
            Map<String, Object> paymentResult = paymentService.processWalletPayment(savedOrder);
            if (!(Boolean) paymentResult.get("success")) {
                // Rollback order creation if payment fails
                orderRepository.delete(savedOrder);
                throw new IdInvalidException((String) paymentResult.get("message"));
            }
            savedOrder.setPaymentStatus("PAID");
            savedOrder = orderRepository.save(savedOrder);
            orderDTO.setPaymentStatus("PAID");
        } else if ("VNPAY".equals(savedOrder.getPaymentMethod())) {
            // Generate VNPAY payment URL
            try {
                String paymentUrl = vnPayService.createPaymentUrl(savedOrder, clientIp, baseUrl);
                orderDTO.setVnpayPaymentUrl(paymentUrl);
                log.info("VNPAY payment URL generated for order {} with client IP {} and base URL {}: {}",
                        savedOrder.getId(), clientIp, baseUrl, paymentUrl);
            } catch (Exception e) {
                log.error("Failed to generate VNPAY payment URL: {}", e.getMessage());
                throw new IdInvalidException("Failed to generate payment URL: " + e.getMessage());
            }
            // VNPAY payment will be updated after callback
            savedOrder.setPaymentStatus("UNPAID");
        }

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO updateOrder(Order order) throws IdInvalidException {
        // check id
        Order currentOrder = getOrderById(order.getId());
        if (currentOrder == null) {
            throw new IdInvalidException("Order not found with id: " + order.getId());
        }

        // update fields
        if (order.getOrderStatus() != null) {
            currentOrder.setOrderStatus(order.getOrderStatus());

            // set deliveredAt when status is DELIVERED
            if ("DELIVERED".equals(order.getOrderStatus()) && currentOrder.getDeliveredAt() == null) {
                currentOrder.setDeliveredAt(Instant.now());
            }
        }

        if (order.getDriver() != null) {
            User driver = this.userService.getUserById(order.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + order.getDriver().getId());
            }
            currentOrder.setDriver(driver);
        }

        if (order.getDeliveryAddress() != null) {
            currentOrder.setDeliveryAddress(order.getDeliveryAddress());
        }
        if (order.getDeliveryLatitude() != null) {
            currentOrder.setDeliveryLatitude(order.getDeliveryLatitude());
        }
        if (order.getDeliveryLongitude() != null) {
            currentOrder.setDeliveryLongitude(order.getDeliveryLongitude());
        }
        if (order.getSpecialInstructions() != null) {
            currentOrder.setSpecialInstructions(order.getSpecialInstructions());
        }
        if (order.getSubtotal() != null) {
            currentOrder.setSubtotal(order.getSubtotal());
        }
        if (order.getDeliveryFee() != null) {
            currentOrder.setDeliveryFee(order.getDeliveryFee());
        }
        if (order.getTotalAmount() != null) {
            currentOrder.setTotalAmount(order.getTotalAmount());
        }
        if (order.getPaymentMethod() != null) {
            currentOrder.setPaymentMethod(order.getPaymentMethod());
        }
        if (order.getPaymentStatus() != null) {
            currentOrder.setPaymentStatus(order.getPaymentStatus());
        }
        if (order.getCancellationReason() != null) {
            currentOrder.setCancellationReason(order.getCancellationReason());
        }

        currentOrder = orderRepository.save(currentOrder);
        return convertToResOrderDTO(currentOrder);
    }

    @Transactional
    public ResOrderDTO assignDriver(Long orderId) throws IdInvalidException {

        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get restaurant location
        Restaurant restaurant = order.getRestaurant();
        if (restaurant == null || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new IdInvalidException("Restaurant location is required to find drivers");
        }

        // Get search radius from system configuration (default 10 km if not set)
        BigDecimal radiusKm = new BigDecimal("10.0");
        try {
            SystemConfiguration radiusConfig = systemConfigurationService
                    .getSystemConfigurationByKey("DRIVER_SEARCH_RADIUS_KM");
            if (radiusConfig != null && radiusConfig.getConfigValue() != null
                    && !radiusConfig.getConfigValue().isEmpty()) {
                radiusKm = new BigDecimal(radiusConfig.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Failed to get DRIVER_SEARCH_RADIUS_KM config, using default 10 km", e);
        }

        log.info("🔍 Step 1: Searching drivers using Redis GEO within {} km of restaurant (lat: {}, lng: {})",
                radiusKm, restaurant.getLatitude(), restaurant.getLongitude());

        // STEP 1: Use Redis GEO to find nearby drivers (fast spatial search with
        // Geohash)
        var geoResults = redisGeoService.findNearbyDrivers(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                radiusKm.doubleValue(),
                50 // Get top 50 closest drivers
        );

        if (geoResults == null || geoResults.getContent().isEmpty()) {
            throw new IdInvalidException("No drivers found within " + radiusKm + " km radius");
        }

        // Extract driver IDs from Redis GEO results
        List<Long> nearbyDriverIds = geoResults.getContent().stream()
                .map(result -> {
                    try {
                        return Long.parseLong(result.getContent().getName().toString());
                    } catch (Exception e) {
                        log.error("Failed to parse driver ID: {}", result.getContent().getName());
                        return null;
                    }
                })
                .filter(id -> id != null)
                .collect(Collectors.toList());

        log.info("📍 Found {} drivers in Redis GEO within radius: {}", nearbyDriverIds.size(), nearbyDriverIds);

        // STEP 2: Query SQL to validate business rules (COD limit, wallet balance,
        // status)
        List<DriverProfile> candidateDrivers;
        if ("COD".equals(order.getPaymentMethod())) {
            log.info("💰 Step 2: Validating COD limit >= {} for {} drivers",
                    order.getTotalAmount(), nearbyDriverIds.size());
            candidateDrivers = driverProfileRepository.findByUserIdsWithCodLimit(
                    nearbyDriverIds,
                    order.getTotalAmount());
        } else {
            log.info("💳 Step 2: Validating online payment readiness for {} drivers", nearbyDriverIds.size());
            candidateDrivers = driverProfileRepository.findByUserIds(nearbyDriverIds);
        }

        if (candidateDrivers.isEmpty()) {
            throw new IdInvalidException("No qualified drivers found (failed business rules validation)");
        }

        log.info("✅ {} drivers passed validation", candidateDrivers.size());

        // STEP 3: Find the closest driver using Mapbox API for real driving distance
        log.info("🚗 Step 3: Calculating real driving distances using Mapbox API");
        DriverProfile closestDriver = findClosestDriverWithMapbox(candidateDrivers, restaurant);
        if (closestDriver == null) {
            throw new IdInvalidException(
                    "Failed to calculate driving distance to available drivers");
        }

        User driver = this.userService.getUserById(closestDriver.getUser().getId());
        if (driver == null) {
            throw new IdInvalidException("Driver user not found");
        }

        log.info("🎯 Assigned driver {} (ID: {}) to order {}", driver.getName(), driver.getId(), orderId);

        order.setDriver(driver);
        order = orderRepository.save(order);

        // Convert to DTO for response
        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify driver about order assignment via WebSocket
        webSocketService.notifyDriverOrderAssigned(order.getDriver().getId(), orderDTO);

        return orderDTO;
    }

    // CUSTOMER ACTIONS
    @Transactional
    public ResOrderDTO cancelOrder(Long orderId, String cancellationReason) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only reject orders with PENDING status. Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus("REJECTED");
        order.setCancellationReason(cancellationReason);

        // If payment was already made (WALLET or VNPAY), process refund
        if ("PAID".equals(order.getPaymentStatus()) &&
                ("WALLET".equals(order.getPaymentMethod()) || "VNPAY".equals(order.getPaymentMethod()))) {
            paymentService.processRefund(order);
            order.setPaymentStatus("REFUNDED");
        }

        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify customer about order rejection
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Your order has been rejected by the restaurant");

        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    // RESTAURANT ACTIONS
    @Transactional
    public ResOrderDTO markOrderAsReady(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (!"PREPARING".equals(order.getOrderStatus()) && !"DRIVER_ASSIGNED".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only mark orders as READY from PREPARING or ACCEPTED status. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus("READY");
        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify driver and customer that order is ready for pickup
        if (order.getDriver() != null) {
            webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                    orderDTO, "Your order is ready for pickup");
        }
        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO acceptOrder(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only accept orders with PENDING status. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus("PREPARING");
        order.setPreparingAt(Instant.now());
        order = orderRepository.save(order);

        // Notify customer about order acceptance
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                convertToResOrderDTO(order), "Your order has been accepted and is being prepared");

        assignDriver(orderId);

        return convertToResOrderDTO(order);
    }

    // DRIVER ACTIONS
    @Transactional
    public ResOrderDTO acceptOrderByDriver(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        // Check if order has this driver assigned
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new IdInvalidException("This order is not assigned to you");
        }

        if ("COD".equals(order.getPaymentMethod())) {
            Map<String, Object> paymentResult = paymentService.processCODPaymentOnDelivery(order);
            if (!(Boolean) paymentResult.get("success")) {
                // Rollback order creation if payment fails
                orderRepository.delete(order);
                throw new IdInvalidException((String) paymentResult.get("message"));
            }
        }

        // Update status to DRIVER_ASSIGNED
        order.setOrderStatus("DRIVER_ASSIGNED");
        order = orderRepository.save(order);

        // Update driver profile status to UNAVAILABLE
        try {
            driverProfileService.updateDriverProfileStatusByUserId(driver.getId(), "UNAVAILABLE");
            log.info("Updated driver {} profile status to UNAVAILABLE", driver.getId());
        } catch (Exception e) {
            log.error("Failed to update driver profile status: {}", e.getMessage());
        }

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify customer and restaurant about driver acceptance
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Driver has accepted your order");
        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO rejectOrderByDriver(Long orderId, String rejectionReason)
            throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        // Check if order has this driver assigned
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new IdInvalidException("This order is not assigned to you");
        }

        // Save rejection to Redis using RedisRejectionService
        redisRejectionService.addRejectedDriver(orderId, driver.getId());

        log.info("💾 Saved driver {} rejection for order {} to Redis (reason: {})",
                driver.getId(), orderId, rejectionReason);

        // Get list of all rejected driver IDs for this order from Redis
        List<Long> rejectedDriverIds = redisRejectionService.getRejectedDriverIds(orderId);

        // Get restaurant location
        Restaurant restaurant = order.getRestaurant();
        if (restaurant == null || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new IdInvalidException("Restaurant location is required to find drivers");
        }

        // Get search radius
        BigDecimal radiusKm = new BigDecimal("10.0");
        try {
            SystemConfiguration radiusConfig = systemConfigurationService
                    .getSystemConfigurationByKey("DRIVER_SEARCH_RADIUS_KM");
            if (radiusConfig != null && radiusConfig.getConfigValue() != null
                    && !radiusConfig.getConfigValue().isEmpty()) {
                radiusKm = new BigDecimal(radiusConfig.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Failed to get DRIVER_SEARCH_RADIUS_KM config, using default 10 km", e);
        }

        log.info("🔍 Searching for alternative drivers using Redis GEO (excluding {} rejected drivers)",
                rejectedDriverIds.size());

        // STEP 1: Use Redis GEO to find nearby drivers
        var geoResults = redisGeoService.findNearbyDrivers(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                radiusKm.doubleValue(),
                100 // Get more drivers since some may be rejected
        );

        if (geoResults == null || geoResults.getContent().isEmpty()) {
            order.setDriver(null);
            log.warn("No alternative drivers found in Redis GEO");
            order = orderRepository.save(order);
            ResOrderDTO orderDTO = convertToResOrderDTO(order);
            webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                    orderDTO, "Looking for another driver for your order");
            return orderDTO;
        }

        // Extract driver IDs and exclude rejected ones
        List<Long> nearbyDriverIds = geoResults.getContent().stream()
                .map(result -> {
                    try {
                        return Long.parseLong(result.getContent().getName().toString());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(id -> id != null && !rejectedDriverIds.contains(id))
                .collect(Collectors.toList());

        log.info("📍 Found {} available drivers (after excluding rejected)", nearbyDriverIds.size());

        if (nearbyDriverIds.isEmpty()) {
            order.setDriver(null);
            order = orderRepository.save(order);
            ResOrderDTO orderDTO = convertToResOrderDTO(order);
            webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                    orderDTO, "Looking for another driver for your order");
            return orderDTO;
        }

        // STEP 2: Query SQL to validate business rules
        List<DriverProfile> candidateDrivers;
        if ("COD".equals(order.getPaymentMethod())) {
            candidateDrivers = driverProfileRepository.findByUserIdsWithCodLimit(
                    nearbyDriverIds,
                    order.getTotalAmount());
        } else {
            candidateDrivers = driverProfileRepository.findByUserIds(nearbyDriverIds);
        }

        // If we found candidates, assign the closest one using Mapbox
        if (!candidateDrivers.isEmpty()) {
            log.info("✅ {} drivers passed validation", candidateDrivers.size());
            DriverProfile closestDriver = findClosestDriverWithMapbox(candidateDrivers, restaurant);
            if (closestDriver != null) {
                // Assign to next driver and keep current status
                order.setDriver(closestDriver.getUser());
                log.info("🎯 Reassigned to driver {}", closestDriver.getUser().getId());
            } else {
                // Failed to calculate distance for all candidates
                order.setDriver(null);
                log.warn("Failed to find closest driver for order {} - Mapbox distance calculation failed",
                        orderId);
            }
        } else {
            // No more available drivers, set driver to null and reset status
            order.setDriver(null);
            log.warn("No qualified drivers found after validation");
        }

        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify customer and restaurant about driver rejection and reassignment
        if (order.getDriver() != null) {
            webSocketService.notifyDriverOrderAssigned(order.getDriver().getId(), orderDTO);
        }
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Looking for another driver for your order");

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO markOrderAsPickedUp(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        // Check if order has this driver assigned
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new IdInvalidException("This order is not assigned to you");
        }

        if (!"READY".equals(order.getOrderStatus()) && !"DRIVER_ASSIGNED".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only mark orders as PICKED_UP from READY or DRIVER_ASSIGNED status. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus("PICKED_UP");
        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify customer about order pickup
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Your order has been picked up and is on the way");
        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO markOrderAsArrived(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        // Check if order has this driver assigned
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new IdInvalidException("This order is not assigned to you");
        }

        if (!"PICKED_UP".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only mark orders as ARRIVED from PICKED_UP status. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus("ARRIVED");
        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify customer about arrival
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Your order has arrived!");
        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    @Transactional
    public ResOrderDTO markOrderAsDelivered(Long orderId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        // Check if order has this driver assigned
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new IdInvalidException("This order is not assigned to you");
        }

        if (!"ARRIVED".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only mark orders as DELIVERED from ARRIVED status. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus("DELIVERED");
        if (order.getDeliveredAt() == null) {
            order.setDeliveredAt(Instant.now());
        }

        if ("COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
        }

        // Update driver's completed trips count
        Optional<DriverProfile> driverProfileOpt = driverProfileRepository.findByUserId(driver.getId());
        if (driverProfileOpt.isPresent()) {
            DriverProfile driverProfile = driverProfileOpt.get();
            Integer currentTrips = driverProfile.getCompletedTrips() != null ? driverProfile.getCompletedTrips() : 0;
            driverProfile.setCompletedTrips(currentTrips + 1);
            driverProfileRepository.save(driverProfile);
            log.info("Updated driver {} completed trips to {}", driver.getId(), currentTrips + 1);
        }

        // Create earnings summary when order is delivered
        orderEarningsSummaryService.createOrderEarningsSummaryFromOrder(orderId);

        order = orderRepository.save(order);

        ResOrderDTO orderDTO = convertToResOrderDTO(order);

        // Notify all parties about successful delivery
        webSocketService.notifyCustomerOrderUpdate(order.getCustomer().getId(),
                orderDTO, "Your order has been delivered successfully!");
        webSocketService.broadcastOrderStatusChange(orderDTO);

        return orderDTO;
    }

    public ResultPaginationDTO getAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> page = this.orderRepository.findAll(spec, pageable);
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

    public ResultPaginationDTO getAllOrdersDTO(Specification<Order> spec, Pageable pageable) {
        Page<Order> page = this.orderRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResOrderDTO)
                .collect(Collectors.toList()));
        return result;
    }

    public void deleteOrder(Long id) {
        this.orderRepository.deleteById(id);
    }
}
