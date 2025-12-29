package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.CartItemOption;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemOptionDTO;
import com.example.FoodDelivery.service.CartItemOptionService;
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
public class CartItemOptionController {
    private final CartItemOptionService cartItemOptionService;

    public CartItemOptionController(CartItemOptionService cartItemOptionService) {
        this.cartItemOptionService = cartItemOptionService;
    }

    @PostMapping("/cart-item-options")
    @ApiMessage("Create new cart item option")
    public ResponseEntity<ResCartItemOptionDTO> createCartItemOption(
            @Valid @RequestBody CartItemOption cartItemOption)
            throws IdInvalidException {
        ResCartItemOptionDTO createdCartItemOption = cartItemOptionService.createCartItemOptionDTO(cartItemOption);
        return ResponseEntity.ok(createdCartItemOption);
    }

    @PutMapping("/cart-item-options")
    @ApiMessage("Update cart item option")
    public ResponseEntity<ResCartItemOptionDTO> updateCartItemOption(@RequestBody CartItemOption cartItemOption)
            throws IdInvalidException {
        ResCartItemOptionDTO updatedCartItemOption = cartItemOptionService.updateCartItemOptionDTO(cartItemOption);
        return ResponseEntity.ok(updatedCartItemOption);
    }

    @GetMapping("/cart-item-options")
    @ApiMessage("Get all cart item options")
    public ResponseEntity<ResultPaginationDTO> getAllCartItemOptions(
            @Filter Specification<CartItemOption> spec, Pageable pageable) {
        ResultPaginationDTO result = cartItemOptionService.getAllCartItemOptionsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cart-item-options/{id}")
    @ApiMessage("Get cart item option by id")
    public ResponseEntity<ResCartItemOptionDTO> getCartItemOptionById(@PathVariable("id") Long id)
            throws IdInvalidException {
        ResCartItemOptionDTO cartItemOption = cartItemOptionService.getCartItemOptionDTOById(id);
        if (cartItemOption == null) {
            throw new IdInvalidException("Cart item option not found with id: " + id);
        }
        return ResponseEntity.ok(cartItemOption);
    }

    @GetMapping("/cart-item-options/cart-item/{cartItemId}")
    @ApiMessage("Get cart item options by cart item id")
    public ResponseEntity<List<CartItemOption>> getCartItemOptionsByCartItemId(
            @PathVariable("cartItemId") Long cartItemId) {
        List<CartItemOption> cartItemOptions = cartItemOptionService.getCartItemOptionsByCartItemId(cartItemId);
        return ResponseEntity.ok(cartItemOptions);
    }

    @DeleteMapping("/cart-item-options/{id}")
    @ApiMessage("Delete cart item option by id")
    public ResponseEntity<Void> deleteCartItemOption(@PathVariable("id") Long id) throws IdInvalidException {
        CartItemOption cartItemOption = cartItemOptionService.getCartItemOptionById(id);
        if (cartItemOption == null) {
            throw new IdInvalidException("Cart item option not found with id: " + id);
        }
        cartItemOptionService.deleteCartItemOption(id);
        return ResponseEntity.ok().body(null);
    }
}
