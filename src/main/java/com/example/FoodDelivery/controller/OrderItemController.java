package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemDTO;
import com.example.FoodDelivery.service.OrderItemService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PostMapping("/order-items")
    @ApiMessage("Create order item")
    public ResponseEntity<ResOrderItemDTO> createOrderItem(@RequestBody OrderItem orderItem) throws IdInvalidException {
        ResOrderItemDTO createdOrderItem = orderItemService.createOrderItemDTO(orderItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrderItem);
    }

    @PutMapping("/order-items")
    @ApiMessage("Update order item")
    public ResponseEntity<ResOrderItemDTO> updateOrderItem(@RequestBody OrderItem orderItem) throws IdInvalidException {
        ResOrderItemDTO updatedOrderItem = orderItemService.updateOrderItemDTO(orderItem);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @GetMapping("/order-items")
    @ApiMessage("Get all order items")
    public ResponseEntity<ResultPaginationDTO> getAllOrderItems(
            @Filter Specification<OrderItem> spec, Pageable pageable) {
        ResultPaginationDTO result = orderItemService.getAllOrderItemsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/order-items/{id}")
    @ApiMessage("Get order item by id")
    public ResponseEntity<ResOrderItemDTO> getOrderItemById(@PathVariable("id") Long id) throws IdInvalidException {
        ResOrderItemDTO orderItem = orderItemService.getOrderItemDTOById(id);
        if (orderItem == null) {
            throw new IdInvalidException("Order item not found with id: " + id);
        }
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping("/order-items/order/{orderId}")
    @ApiMessage("Get order items by order id")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrderId(@PathVariable("orderId") Long orderId) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/order-items/dish/{dishId}")
    @ApiMessage("Get order items by dish id")
    public ResponseEntity<List<OrderItem>> getOrderItemsByDishId(@PathVariable("dishId") Long dishId) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByDishId(dishId);
        return ResponseEntity.ok(orderItems);
    }

    @DeleteMapping("/order-items/{id}")
    @ApiMessage("Delete order item by id")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable("id") Long id) throws IdInvalidException {
        OrderItem orderItem = orderItemService.getOrderItemById(id);
        if (orderItem == null) {
            throw new IdInvalidException("Order item not found with id: " + id);
        }
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.ok().body(null);
    }
}
