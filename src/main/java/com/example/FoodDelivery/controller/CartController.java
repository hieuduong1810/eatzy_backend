package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.req.ReqCartDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartDTO;
import com.example.FoodDelivery.service.CartService;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import jakarta.validation.Valid;

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
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PutMapping("/carts")
    @ApiMessage("Update cart")
    public ResponseEntity<ResCartDTO> updateCart(@RequestBody Cart cart) throws IdInvalidException {
        ResCartDTO updatedCart = cartService.updateCartDTO(cart);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/carts")
    @ApiMessage("Get all carts")
    public ResponseEntity<ResultPaginationDTO> getAllCarts(
            @Filter Specification<Cart> spec, Pageable pageable) {
        ResultPaginationDTO result = cartService.getAllCartsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/carts/{id}")
    @ApiMessage("Get cart by id")
    public ResponseEntity<ResCartDTO> getCartById(@PathVariable("id") Long id) throws IdInvalidException {
        ResCartDTO cart = cartService.getCartDTOById(id);
        if (cart == null) {
            throw new IdInvalidException("Cart not found with id: " + id);
        }
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/carts/customer/{customerId}")
    @ApiMessage("Get carts by customer id")
    public ResponseEntity<List<ResCartDTO>> getCartsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<ResCartDTO> carts = cartService.getCartsDTOByCustomerId(customerId);
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/carts/my-carts")
    @ApiMessage("Get current user's carts")
    public ResponseEntity<List<ResCartDTO>> getMyCart() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() 
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        
        if (email.isEmpty()) {
            throw new IdInvalidException("User not authenticated");
        }
        
        User currentUser = this.userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("User not found");
        }
        
        List<ResCartDTO> carts = cartService.getCartsDTOByCustomerId(currentUser.getId());
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/carts/restaurant/{restaurantId}")
    @ApiMessage("Get carts by restaurant id")
    public ResponseEntity<List<Cart>> getCartsByRestaurantId(@PathVariable("restaurantId") Long restaurantId) {
        List<Cart> carts = cartService.getCartsByRestaurantId(restaurantId);
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/carts/customer/{customerId}/restaurant/{restaurantId}")
    @ApiMessage("Get cart by customer id and restaurant id")
    public ResponseEntity<Cart> getCartByCustomerIdAndRestaurantId(
            @PathVariable("customerId") Long customerId,
            @PathVariable("restaurantId") Long restaurantId) {
        Cart cart = cartService.getCartByCustomerIdAndRestaurantId(customerId, restaurantId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/carts/{id}")
    @ApiMessage("Delete cart by id")
    public ResponseEntity<Void> deleteCart(@PathVariable("id") Long id) throws IdInvalidException {
        Cart cart = cartService.getCartById(id);
        if (cart == null) {
            throw new IdInvalidException("Cart not found with id: " + id);
        }
        cartService.deleteCart(id);
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/carts")
    @ApiMessage("Save or update cart")
    public ResponseEntity<ResCartDTO> saveOrUpdateCart(@Valid @RequestBody ReqCartDTO reqCartDTO)
            throws IdInvalidException {
        ResCartDTO result = cartService.saveOrUpdateCart(reqCartDTO);
        if (result == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(result);
    }
}
