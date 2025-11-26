package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.service.OrderItemOptionService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderItemOptionController {
    private final OrderItemOptionService orderItemOptionService;

    public OrderItemOptionController(OrderItemOptionService orderItemOptionService) {
        this.orderItemOptionService = orderItemOptionService;
    }

    @PostMapping("/order-item-options")
    @ApiMessage("Create new order item option")
    public ResponseEntity<ResOrderItemOptionDTO> createOrderItemOption(
            @Valid @RequestBody OrderItemOption orderItemOption)
            throws IdInvalidException {
        ResOrderItemOptionDTO createdOrderItemOption = orderItemOptionService.createOrderItemOptionDTO(orderItemOption);
        return ResponseEntity.ok(createdOrderItemOption);
    }

    @PutMapping("/order-item-options")
    @ApiMessage("Update order item option")
    public ResponseEntity<ResOrderItemOptionDTO> updateOrderItemOption(@RequestBody OrderItemOption orderItemOption)
            throws IdInvalidException {
        ResOrderItemOptionDTO updatedOrderItemOption = orderItemOptionService.updateOrderItemOptionDTO(orderItemOption);
        return ResponseEntity.ok(updatedOrderItemOption);
    }

    @GetMapping("/order-item-options")
    @ApiMessage("Get all order item options")
    public ResponseEntity<ResultPaginationDTO> getAllOrderItemOptions(
            @Filter Specification<OrderItemOption> spec, Pageable pageable) {
        ResultPaginationDTO result = orderItemOptionService.getAllOrderItemOptionsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/order-item-options/{id}")
    @ApiMessage("Get order item option by id")
    public ResponseEntity<ResOrderItemOptionDTO> getOrderItemOptionById(@PathVariable("id") Long id)
            throws IdInvalidException {
        ResOrderItemOptionDTO orderItemOption = orderItemOptionService.getOrderItemOptionDTOById(id);
        if (orderItemOption == null) {
            throw new IdInvalidException("Order item option not found with id: " + id);
        }
        return ResponseEntity.ok(orderItemOption);
    }

    @GetMapping("/order-item-options/order-item/{orderItemId}")
    @ApiMessage("Get order item options by order item id")
    public ResponseEntity<List<OrderItemOption>> getOrderItemOptionsByOrderItemId(
            @PathVariable("orderItemId") Long orderItemId) {
        List<OrderItemOption> orderItemOptions = orderItemOptionService.getOrderItemOptionsByOrderItemId(orderItemId);
        return ResponseEntity.ok(orderItemOptions);
    }

    @DeleteMapping("/order-item-options/{id}")
    @ApiMessage("Delete order item option by id")
    public ResponseEntity<Void> deleteOrderItemOption(@PathVariable("id") Long id) throws IdInvalidException {
        OrderItemOption orderItemOption = orderItemOptionService.getOrderItemOptionById(id);
        if (orderItemOption == null) {
            throw new IdInvalidException("Order item option not found with id: " + id);
        }
        orderItemOptionService.deleteOrderItemOption(id);
        return ResponseEntity.ok().body(null);
    }
}
