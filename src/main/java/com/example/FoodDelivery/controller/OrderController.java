package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.req.ReqOrderDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.service.OrderService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    @ApiMessage("Create order")
    public ResponseEntity<ResOrderDTO> createOrder(@Valid @RequestBody ReqOrderDTO reqOrderDTO)
            throws IdInvalidException {
        ResOrderDTO createdOrder = orderService.createOrderFromReqDTO(reqOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/orders")
    @ApiMessage("Update order")
    public ResponseEntity<ResOrderDTO> updateOrder(@RequestBody Order order) throws IdInvalidException {
        ResOrderDTO updatedOrder = orderService.updateOrderDTO(order);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/orders")
    @ApiMessage("Get all orders")
    public ResponseEntity<ResultPaginationDTO> getAllOrders(
            @Filter Specification<Order> spec, Pageable pageable) {
        ResultPaginationDTO result = orderService.getAllOrdersDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/{id}")
    @ApiMessage("Get order by id")
    public ResponseEntity<ResOrderDTO> getOrderById(@PathVariable("id") Long id) throws IdInvalidException {
        ResOrderDTO order = orderService.getOrderDTOById(id);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + id);
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/customer/{customerId}")
    @ApiMessage("Get orders by customer id")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable("customerId") Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/restaurant/{restaurantId}")
    @ApiMessage("Get orders by restaurant id")
    public ResponseEntity<List<Order>> getOrdersByRestaurantId(@PathVariable("restaurantId") Long restaurantId) {
        List<Order> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/driver/{driverId}")
    @ApiMessage("Get orders by driver id")
    public ResponseEntity<List<Order>> getOrdersByDriverId(@PathVariable("driverId") Long driverId) {
        List<Order> orders = orderService.getOrdersByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/status/{orderStatus}")
    @ApiMessage("Get orders by status")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable("orderStatus") String orderStatus) {
        List<Order> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{id}/assign-driver")
    @ApiMessage("Assign driver to order")
    public ResponseEntity<Order> assignDriver(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, Long> body) throws IdInvalidException {
        Long driverId = body.get("driverId");
        if (driverId == null) {
            throw new IdInvalidException("Driver id is required");
        }
        Order order = orderService.assignDriver(orderId, driverId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/status")
    @ApiMessage("Update order status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, String> body) throws IdInvalidException {
        String status = body.get("status");
        if (status == null) {
            throw new IdInvalidException("Status is required");
        }
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/cancel")
    @ApiMessage("Cancel order")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, String> body) throws IdInvalidException {
        String cancellationReason = body.get("cancellationReason");
        Order order = orderService.cancelOrder(orderId, cancellationReason);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/orders/{id}")
    @ApiMessage("Delete order by id")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) throws IdInvalidException {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + id);
        }
        orderService.deleteOrder(id);
        return ResponseEntity.ok().body(null);
    }
}
