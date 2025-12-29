package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemDTO;
import com.example.FoodDelivery.service.CartItemService;
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
public class CartItemController {
    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping("/cart-items")
    @ApiMessage("Create cart item")
    public ResponseEntity<ResCartItemDTO> createCartItem(@RequestBody CartItem cartItem) throws IdInvalidException {
        ResCartItemDTO createdCartItem = cartItemService.createCartItemDTO(cartItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCartItem);
    }

    @PutMapping("/cart-items")
    @ApiMessage("Update cart item")
    public ResponseEntity<ResCartItemDTO> updateCartItem(@RequestBody CartItem cartItem) throws IdInvalidException {
        ResCartItemDTO updatedCartItem = cartItemService.updateCartItemDTO(cartItem);
        return ResponseEntity.ok(updatedCartItem);
    }

    @GetMapping("/cart-items")
    @ApiMessage("Get all cart items")
    public ResponseEntity<ResultPaginationDTO> getAllCartItems(
            @Filter Specification<CartItem> spec, Pageable pageable) {
        ResultPaginationDTO result = cartItemService.getAllCartItemsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cart-items/{id}")
    @ApiMessage("Get cart item by id")
    public ResponseEntity<ResCartItemDTO> getCartItemById(@PathVariable("id") Long id) throws IdInvalidException {
        ResCartItemDTO cartItem = cartItemService.getCartItemDTOById(id);
        if (cartItem == null) {
            throw new IdInvalidException("Cart item not found with id: " + id);
        }
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping("/cart-items/cart/{cartId}")
    @ApiMessage("Get cart items by cart id")
    public ResponseEntity<List<CartItem>> getCartItemsByCartId(@PathVariable("cartId") Long cartId) {
        List<CartItem> cartItems = cartItemService.getCartItemsByCartId(cartId);
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/cart-items/dish/{dishId}")
    @ApiMessage("Get cart items by dish id")
    public ResponseEntity<List<CartItem>> getCartItemsByDishId(@PathVariable("dishId") Long dishId) {
        List<CartItem> cartItems = cartItemService.getCartItemsByDishId(dishId);
        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/cart-items/{id}")
    @ApiMessage("Delete cart item by id")
    public ResponseEntity<Void> deleteCartItem(@PathVariable("id") Long id) throws IdInvalidException {
        CartItem cartItem = cartItemService.getCartItemById(id);
        if (cartItem == null) {
            throw new IdInvalidException("Cart item not found with id: " + id);
        }
        cartItemService.deleteCartItem(id);
        return ResponseEntity.ok().body(null);
    }
}
