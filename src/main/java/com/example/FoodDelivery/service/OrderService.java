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
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.req.ReqOrderDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderEarningsSummaryService orderEarningsSummaryService;

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final MenuOptionRepository menuOptionRepository;

    public OrderService(OrderRepository orderRepository, UserService userService,
            RestaurantService restaurantService, DishService dishService,
            MenuOptionRepository menuOptionRepository, @Lazy OrderEarningsSummaryService orderEarningsSummaryService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.dishService = dishService;
        this.menuOptionRepository = menuOptionRepository;
        this.orderEarningsSummaryService = orderEarningsSummaryService;
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

    @Transactional
    public Order createOrder(Order order) throws IdInvalidException {
        // check customer exists
        if (order.getCustomer() != null) {
            User customer = this.userService.getUserById(order.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + order.getCustomer().getId());
            }
            order.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        // check restaurant exists
        if (order.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(order.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + order.getRestaurant().getId());
            }
            order.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        // check driver exists (if assigned)
        if (order.getDriver() != null) {
            User driver = this.userService.getUserById(order.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + order.getDriver().getId());
            }
            order.setDriver(driver);
        }

        // set default values
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("PENDING");
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("UNPAID");
        }
        order.setCreatedAt(Instant.now());

        return orderRepository.save(order);
    }

    public ResOrderDTO createOrderDTO(Order order) throws IdInvalidException {
        Order savedOrder = createOrder(order);
        return convertToResOrderDTO(savedOrder);
    }

    @Transactional
    public ResOrderDTO createOrderFromReqDTO(ReqOrderDTO reqOrderDTO) throws IdInvalidException {
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

        return convertToResOrderDTO(savedOrder);
    }

    @Transactional
    public Order updateOrder(Order order) throws IdInvalidException {
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

        return orderRepository.save(currentOrder);
    }

    public ResOrderDTO updateOrderDTO(Order order) throws IdInvalidException {
        Order updatedOrder = updateOrder(order);
        return convertToResOrderDTO(updatedOrder);
    }

    @Transactional
    public ResOrderDTO assignDriver(Long orderId, Long driverId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        User driver = this.userService.getUserById(driverId);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with id: " + driverId);
        }

        order.setDriver(driver);
        order.setOrderStatus("ASSIGNED");
        order = orderRepository.save(order);

        return convertToResOrderDTO(order);
    }

    @Transactional
    public ResOrderDTO updateOrderStatus(Long orderId, String status) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        order.setOrderStatus(status);

        // set deliveredAt when status is DELIVERED
        if ("DELIVERED".equals(status) && order.getDeliveredAt() == null) {
            order.setDeliveredAt(Instant.now());
        }
        orderEarningsSummaryService.createOrderEarningsSummaryFromOrder(orderId);
        order = orderRepository.save(order);

        return convertToResOrderDTO(order);
    }

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
