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
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<ResOrderDTO> createOrder(
            @Valid @RequestBody ReqOrderDTO reqOrderDTO,
            HttpServletRequest request) throws IdInvalidException {
        String clientIp = getClientIp(request);

        // Get base URL from request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }

        ResOrderDTO createdOrder = orderService.createOrderFromReqDTO(reqOrderDTO, clientIp, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Extract client IP address from request
     * Handles proxy/load balancer scenarios
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    @PutMapping("/orders")
    @ApiMessage("Update order")
    public ResponseEntity<ResOrderDTO> updateOrder(@RequestBody Order order) throws IdInvalidException {
        ResOrderDTO updatedOrder = orderService.updateOrder(order);
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

    @PatchMapping("/orders/{id}/assign-driver")
    @ApiMessage("Assign driver to order")
    public ResponseEntity<ResOrderDTO> assignDriver(
            @PathVariable("id") Long orderId) throws IdInvalidException {
        ResOrderDTO order = orderService.assignDriver(orderId);
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

    @GetMapping("/orders/restaurant/{restaurantId}/status/{orderStatus}")
    @ApiMessage("Get orders by restaurant id and status")
    public ResponseEntity<List<ResOrderDTO>> getOrdersByRestaurantIdAndStatus(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("orderStatus") String orderStatus) {
        List<ResOrderDTO> orders = orderService.getOrdersDTOByRestaurantIdAndStatus(restaurantId, orderStatus);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/customer/{customerId}/status/{orderStatus}")
    @ApiMessage("Get orders by customer id and status")
    public ResponseEntity<List<ResOrderDTO>> getOrdersByCustomerIdAndStatus(
            @PathVariable("customerId") Long customerId,
            @PathVariable("orderStatus") String orderStatus) {
        List<ResOrderDTO> orders = orderService.getOrdersDTOByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/driver/{driverId}/status/{orderStatus}")
    @ApiMessage("Get orders by driver id and status")
    public ResponseEntity<List<ResOrderDTO>> getOrdersByDriverIdAndStatus(
            @PathVariable("driverId") Long driverId,
            @PathVariable("orderStatus") String orderStatus) {
        List<ResOrderDTO> orders = orderService.getOrdersDTOByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{id}/restaurant/ready")
    @ApiMessage("Mark order as ready for pickup")
    public ResponseEntity<ResOrderDTO> markOrderAsReady(@PathVariable("id") Long orderId) throws IdInvalidException {
        ResOrderDTO order = orderService.markOrderAsReady(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/customer/cancel")
    @ApiMessage("Cancel order")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, String> body) throws IdInvalidException {
        String cancellationReason = body.get("cancellationReason");
        Order order = orderService.cancelOrder(orderId, cancellationReason);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/restaurant/accept")
    @ApiMessage("Accept order")
    public ResponseEntity<ResOrderDTO> acceptOrder(@PathVariable("id") Long orderId) throws IdInvalidException {
        ResOrderDTO order = orderService.acceptOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/restaurant/reject")
    @ApiMessage("Reject order")
    public ResponseEntity<ResOrderDTO> rejectOrder(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, String> body) throws IdInvalidException {
        String rejectionReason = body.get("rejectionReason");
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IdInvalidException("Rejection reason is required");
        }
        ResOrderDTO order = orderService.rejectOrder(orderId, rejectionReason);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/driver/accept")
    @ApiMessage("Driver accepts order")
    public ResponseEntity<ResOrderDTO> acceptOrderByDriver(@PathVariable("id") Long orderId) throws IdInvalidException {
        ResOrderDTO order = orderService.acceptOrderByDriver(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/driver/reject")
    @ApiMessage("Driver rejects order")
    public ResponseEntity<ResOrderDTO> rejectOrderByDriver(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, String> body) throws IdInvalidException {
        String rejectionReason = body.get("rejectionReason");
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IdInvalidException("Rejection reason is required");
        }
        ResOrderDTO order = orderService.rejectOrderByDriver(orderId, rejectionReason);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/driver/picked-up")
    @ApiMessage("Driver marks order as picked up")
    public ResponseEntity<ResOrderDTO> markOrderAsPickedUp(@PathVariable("id") Long orderId) throws IdInvalidException {
        ResOrderDTO order = orderService.markOrderAsPickedUp(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/driver/delivered")
    @ApiMessage("Driver marks order as delivered")
    public ResponseEntity<ResOrderDTO> markOrderAsDelivered(@PathVariable("id") Long orderId)
            throws IdInvalidException {
        ResOrderDTO order = orderService.markOrderAsDelivered(orderId);
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
