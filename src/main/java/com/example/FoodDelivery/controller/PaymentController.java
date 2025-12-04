package com.example.FoodDelivery.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.service.OrderService;
import com.example.FoodDelivery.service.PaymentService;
import com.example.FoodDelivery.service.VNPayService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, VNPayService vnPayService, OrderService orderService) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.orderService = orderService;
    }

    /**
     * Process wallet payment for an order
     */
    @PostMapping("/wallet")
    @ApiMessage("Process wallet payment")
    public ResponseEntity<Map<String, Object>> processWalletPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, Object> result = paymentService.processWalletPayment(order);

        // Update order payment status if successful
        if ((Boolean) result.get("success")) {
            order.setPaymentStatus("PAID");
            orderService.updateOrder(order);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Create VNPAY payment URL
     */
    @PostMapping("/vnpay/create")
    @ApiMessage("Create VNPAY payment URL")
    public ResponseEntity<Map<String, Object>> createVNPayPayment(
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) throws IdInvalidException, UnsupportedEncodingException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Get client IP address
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        // Get base URL from request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }

        String paymentUrl = vnPayService.createPaymentUrl(order, ipAddress, baseUrl);

        Map<String, Object> result = Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "orderId", orderId);

        return ResponseEntity.ok(result);
    }

    /**
     * VNPAY callback handler
     */
    @GetMapping("/vnpay/callback")
    @ApiMessage("VNPAY payment callback")
    public ResponseEntity<Map<String, Object>> vnpayCallback(@RequestParam Map<String, String> params)
            throws IdInvalidException {
        Map<String, Object> result = vnPayService.processCallback(params);

        // Update order payment status
        Long orderId = (Long) result.get("orderId");
        if (orderId != null) {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                if ((Boolean) result.get("success")) {
                    order.setPaymentStatus("PAID");
                } else {
                    order.setPaymentStatus("FAILED");
                }
                orderService.updateOrder(order);
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Validate COD payment for an order
     */
    @PostMapping("/cod/validate")
    @ApiMessage("Validate COD payment")
    public ResponseEntity<Map<String, Object>> validateCODPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        Long driverId = body.get("driverId");

        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, Object> result = paymentService.validateCODPayment(order, driverId);
        return ResponseEntity.ok(result);
    }

    /**
     * Check if COD is available for given amount
     */
    @GetMapping("/cod/available")
    @ApiMessage("Check COD availability")
    public ResponseEntity<Map<String, Object>> checkCODAvailability(@RequestParam BigDecimal amount) {
        boolean available = paymentService.isCODAvailable(amount);
        Map<String, Object> result = Map.of(
                "available", available,
                "amount", amount);
        return ResponseEntity.ok(result);
    }

    /**
     * Process COD payment when order is delivered
     */
    @PostMapping("/cod/process")
    @ApiMessage("Process COD payment on delivery")
    public ResponseEntity<Map<String, Object>> processCODPayment(@RequestBody Map<String, Long> body)
            throws IdInvalidException {
        Long orderId = body.get("orderId");
        if (orderId == null) {
            throw new IdInvalidException("Order ID is required");
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        // Check if order is delivered
        if (!"DELIVERED".equals(order.getOrderStatus())) {
            throw new IdInvalidException("Order must be delivered before processing COD payment");
        }

        Map<String, Object> result = paymentService.processCODPaymentOnDelivery(order);

        // Update order payment status
        if ((Boolean) result.get("success")) {
            order.setPaymentStatus("PAID");
            orderService.updateOrder(order);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Calculate commissions for an order
     */
    @GetMapping("/commission/{orderId}")
    @ApiMessage("Calculate order commissions")
    public ResponseEntity<Map<String, BigDecimal>> calculateCommissions(@PathVariable Long orderId)
            throws IdInvalidException {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        Map<String, BigDecimal> commissions = paymentService.calculateCommissions(order);
        return ResponseEntity.ok(commissions);
    }
}
