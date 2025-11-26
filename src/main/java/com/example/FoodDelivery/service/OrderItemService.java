package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.repository.OrderItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.stream.Collectors;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final DishService dishService;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderService orderService,
            DishService dishService) {
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.dishService = dishService;
    }

    private ResOrderItemDTO convertToResOrderItemDTO(OrderItem orderItem) {
        ResOrderItemDTO dto = new ResOrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtPurchase(orderItem.getPriceAtPurchase());

        // Convert dish
        if (orderItem.getDish() != null) {
            ResOrderItemDTO.Dish dish = new ResOrderItemDTO.Dish();
            dish.setId(orderItem.getDish().getId());
            dish.setName(orderItem.getDish().getName());
            dish.setPrice(orderItem.getDish().getPrice());
            dto.setDish(dish);
        }

        // Convert order item options list
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
            dto.setOrderItemOptions(optionDtos);
        }

        return dto;
    }

    public OrderItem getOrderItemById(Long id) {
        Optional<OrderItem> orderItemOpt = this.orderItemRepository.findById(id);
        return orderItemOpt.orElse(null);
    }

    public ResOrderItemDTO getOrderItemDTOById(Long id) {
        OrderItem orderItem = getOrderItemById(id);
        if (orderItem == null) {
            return null;
        }
        return convertToResOrderItemDTO(orderItem);
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return this.orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> getOrderItemsByDishId(Long dishId) {
        return this.orderItemRepository.findByDishId(dishId);
    }

    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) throws IdInvalidException {
        // check order exists
        if (orderItem.getOrder() != null) {
            Order order = this.orderService.getOrderById(orderItem.getOrder().getId());
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + orderItem.getOrder().getId());
            }
            orderItem.setOrder(order);
        } else {
            throw new IdInvalidException("Order is required");
        }

        // check dish exists
        if (orderItem.getDish() != null) {
            Dish dish = this.dishService.getDishById(orderItem.getDish().getId());
            if (dish == null) {
                throw new IdInvalidException("Dish not found with id: " + orderItem.getDish().getId());
            }
            orderItem.setDish(dish);

            // set price at purchase from current dish price if not provided
            if (orderItem.getPriceAtPurchase() == null) {
                orderItem.setPriceAtPurchase(dish.getPrice());
            }
        } else {
            throw new IdInvalidException("Dish is required");
        }

        // validate quantity
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }

        return orderItemRepository.save(orderItem);
    }

    public ResOrderItemDTO createOrderItemDTO(OrderItem orderItem) throws IdInvalidException {
        OrderItem savedOrderItem = createOrderItem(orderItem);
        return convertToResOrderItemDTO(savedOrderItem);
    }

    @Transactional
    public OrderItem updateOrderItem(OrderItem orderItem) throws IdInvalidException {
        // check id
        OrderItem currentOrderItem = getOrderItemById(orderItem.getId());
        if (currentOrderItem == null) {
            throw new IdInvalidException("Order item not found with id: " + orderItem.getId());
        }

        // update quantity
        if (orderItem.getQuantity() != null) {
            if (orderItem.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            currentOrderItem.setQuantity(orderItem.getQuantity());
        }

        // update price at purchase
        if (orderItem.getPriceAtPurchase() != null) {
            currentOrderItem.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        }

        return orderItemRepository.save(currentOrderItem);
    }

    public ResOrderItemDTO updateOrderItemDTO(OrderItem orderItem) throws IdInvalidException {
        OrderItem updatedOrderItem = updateOrderItem(orderItem);
        return convertToResOrderItemDTO(updatedOrderItem);
    }

    public ResultPaginationDTO getAllOrderItems(Specification<OrderItem> spec, Pageable pageable) {
        Page<OrderItem> page = this.orderItemRepository.findAll(spec, pageable);
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

    public ResultPaginationDTO getAllOrderItemsDTO(Specification<OrderItem> spec, Pageable pageable) {
        Page<OrderItem> page = this.orderItemRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResOrderItemDTO)
                .collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public void deleteOrderItem(Long id) {
        this.orderItemRepository.deleteById(id);
    }
}
