package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
import com.example.FoodDelivery.domain.OrderDriverRejection;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.req.ReqOrderDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.repository.OrderDriverRejectionRepository;
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
    private final DishService dishService;
    private final MenuOptionRepository menuOptionRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final OrderDriverRejectionRepository orderDriverRejectionRepository;
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    public OrderService(OrderRepository orderRepository, UserService userService,
            RestaurantService restaurantService, DishService dishService,
            MenuOptionRepository menuOptionRepository, @Lazy OrderEarningsSummaryService orderEarningsSummaryService,
            DriverProfileRepository driverProfileRepository,
            OrderDriverRejectionRepository orderDriverRejectionRepository,
            PaymentService paymentService,
            VNPayService vnPayService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.dishService = dishService;
        this.menuOptionRepository = menuOptionRepository;
        this.orderEarningsSummaryService = orderEarningsSummaryService;
        this.driverProfileRepository = driverProfileRepository;
        this.orderDriverRejectionRepository = orderDriverRejectionRepository;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
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
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDeliveredAt(order.getDeliveredAt());

        // Convert customer
        if (order.getCustomer() != null) {
            ResOrderDTO.User customer = new ResOrderDTO.User();
            customer.setId(order.getCustomer().getId());
            customer.setName(order.getCustomer().getName());
            dto.setCustomer(customer);
        }

        // Convert restaurant
        if (order.getRestaurant() != null) {
            ResOrderDTO.Restaurant restaurant = new ResOrderDTO.Restaurant();
            restaurant.setId(order.getRestaurant().getId());
            restaurant.setName(order.getRestaurant().getName());
            dto.setRestaurant(restaurant);
        }

        // Convert driver
        if (order.getDriver() != null) {
            ResOrderDTO.User driver = new ResOrderDTO.User();
            driver.setId(order.getDriver().getId());
            driver.setName(order.getDriver().getName());
            dto.setDriver(driver);
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

        return dto;
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
        order.setDeliveryFee(reqOrderDTO.getDeliveryFee());
        order.setTotalAmount(reqOrderDTO.getTotalAmount());
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

        // Calculate total amount: subtotal + delivery fee
        savedOrder.setSubtotal(subtotal);
        BigDecimal deliveryFee = savedOrder.getDeliveryFee() != null ? savedOrder.getDeliveryFee() : BigDecimal.ZERO;
        savedOrder.setTotalAmount(subtotal.add(deliveryFee));

        savedOrder = orderRepository.save(savedOrder);

        // Process payment based on payment method
        ResOrderDTO orderDTO = convertToResOrderDTO(savedOrder);

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
        if (order.getPaymentMethod() == "COD") {
            Optional<DriverProfile> driverOpt = driverProfileRepository
                    .findFirstAvailableDriverByCodLimit(order.getTotalAmount());
            if (!driverOpt.isPresent()) {
                throw new IdInvalidException("No available driver found for this order");
            }

            User driver = this.userService.getUserById(driverOpt.get().getUser().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + driverOpt.get().getUser().getId());
            }
            order.setDriver(driver);
        } else {
            Optional<DriverProfile> driverOpt = driverProfileRepository
                    .findFirstAvailableDriver();
            if (!driverOpt.isPresent()) {
                throw new IdInvalidException("No available driver found for this order");
            }

            User driver = this.userService.getUserById(driverOpt.get().getUser().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + driverOpt.get().getUser().getId());
            }
            order.setDriver(driver);
        }
        order.setOrderStatus("ASSIGNED");
        order = orderRepository.save(order);

        return convertToResOrderDTO(order);
    }

    // CUSTOMER ACTIONS
    @Transactional
    public Order cancelOrder(Long orderId, String cancellationReason) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if ("DELIVERED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new IdInvalidException("Cannot cancel order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("CANCELLED");
        order.setCancellationReason(cancellationReason);
        return orderRepository.save(order);
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

        return convertToResOrderDTO(order);
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
        order = orderRepository.save(order);
        assignDriver(orderId);

        return convertToResOrderDTO(order);
    }

    @Transactional
    public ResOrderDTO rejectOrder(Long orderId, String rejectionReason) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new IdInvalidException(
                    "Can only reject orders with PENDING status. Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus("REJECTED");
        order.setCancellationReason(rejectionReason);

        // If payment was already made (WALLET or VNPAY), process refund
        if ("PAID".equals(order.getPaymentStatus()) &&
                ("WALLET".equals(order.getPaymentMethod()) || "VNPAY".equals(order.getPaymentMethod()))) {
            // TODO: Process refund to customer wallet
            order.setPaymentStatus("REFUNDED");
        }

        order = orderRepository.save(order);

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

        return convertToResOrderDTO(order);
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

        // Save rejection record
        OrderDriverRejection rejection = OrderDriverRejection.builder()
                .order(order)
                .driver(driver)
                .rejectionReason(rejectionReason)
                .rejectedAt(Instant.now())
                .build();
        orderDriverRejectionRepository.save(rejection);

        // Get list of all rejected driver IDs for this order
        List<Long> rejectedDriverIds = orderDriverRejectionRepository.findRejectedDriverIdsByOrderId(orderId);

        // Find next available driver excluding rejected ones
        if (order.getPaymentMethod() == "COD") {
            Optional<DriverProfile> nextDriverProfileOpt;
            if (rejectedDriverIds.isEmpty()) {
                nextDriverProfileOpt = driverProfileRepository
                        .findFirstAvailableDriverByCodLimit(order.getTotalAmount());
            } else {
                nextDriverProfileOpt = driverProfileRepository.findFirstAvailableDriverByCodLimitExcluding(
                        order.getTotalAmount(), rejectedDriverIds);
                if (!nextDriverProfileOpt.isPresent()) {
                    throw new IdInvalidException("No available driver found for this order");
                }
            }

            if (nextDriverProfileOpt.isPresent()) {
                // Assign to next driver
                DriverProfile nextDriverProfile = nextDriverProfileOpt.get();
                order.setDriver(nextDriverProfile.getUser());
                // Keep status as READY or current status for next driver to accept
                if ("DRIVER_ASSIGNED".equals(order.getOrderStatus())) {
                    order.setOrderStatus("READY");
                }
            } else {
                // No more available drivers, set driver to null
                order.setDriver(null);
                order.setOrderStatus("READY");
            }
        } else {
            Optional<DriverProfile> nextDriverProfileOpt;
            if (rejectedDriverIds.isEmpty()) {
                nextDriverProfileOpt = driverProfileRepository.findFirstAvailableDriver();
            } else {
                nextDriverProfileOpt = driverProfileRepository.findFirstAvailableDriverExcluding(rejectedDriverIds);
                if (!nextDriverProfileOpt.isPresent()) {
                    throw new IdInvalidException("No available driver found for this order");
                }
            }

            if (nextDriverProfileOpt.isPresent()) {
                // Assign to next driver
                DriverProfile nextDriverProfile = nextDriverProfileOpt.get();
                order.setDriver(nextDriverProfile.getUser());
                // Keep status as READY or current status for next driver to accept
                if ("DRIVER_ASSIGNED".equals(order.getOrderStatus())) {
                    order.setOrderStatus("READY");
                }
            } else {
                // No more available drivers, set driver to null
                order.setDriver(null);
                order.setOrderStatus("READY");
            }
        }

        order = orderRepository.save(order);

        return convertToResOrderDTO(order);
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

        return convertToResOrderDTO(order);
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

        return convertToResOrderDTO(order);
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

        // Create earnings summary when order is delivered
        orderEarningsSummaryService.createOrderEarningsSummaryFromOrder(orderId);

        order = orderRepository.save(order);

        return convertToResOrderDTO(order);
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
