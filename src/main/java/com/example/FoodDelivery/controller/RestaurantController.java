package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantDTO;
import com.example.FoodDelivery.service.RestaurantService;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.util.List;

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

@RestController
@RequestMapping("/api/v1")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final UserService userService;

    public RestaurantController(RestaurantService restaurantService, UserService userService) {
        this.restaurantService = restaurantService;
        this.userService = userService;
    }

    @PostMapping("/restaurants")
    @ApiMessage("Create restaurant")
    public ResponseEntity<ResRestaurantDTO> createRestaurant(@RequestBody Restaurant restaurant)
            throws IdInvalidException {
        if (this.restaurantService.existsByName(restaurant.getName())) {
            throw new IdInvalidException("Restaurant name already exists: " + restaurant.getName());
        }

        ResRestaurantDTO createdRestaurant = restaurantService.createRestaurantDTO(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
    }

    @PutMapping("/restaurants")
    @ApiMessage("Update restaurant")
    public ResponseEntity<ResRestaurantDTO> updateRestaurant(@RequestBody Restaurant restaurant)
            throws IdInvalidException {
        ResRestaurantDTO updatedRestaurant = restaurantService.updateRestaurantDTO(restaurant);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping("/restaurants")
    @ApiMessage("Get all restaurants")
    public ResponseEntity<ResultPaginationDTO> getAllRestaurants(
            @Filter Specification<Restaurant> spec, Pageable pageable) {
        ResultPaginationDTO result = restaurantService.getAllRestaurantsDTO(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/restaurants/nearby")
    @ApiMessage("Get nearby restaurants within configured distance")
    public ResponseEntity<ResultPaginationDTO> getNearbyRestaurants(
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam(value = "search", required = false) String searchKeyword,
            @Filter Specification<Restaurant> spec,
            Pageable pageable) throws IdInvalidException {
        if (latitude == null || longitude == null) {
            throw new IdInvalidException("Latitude and longitude are required");
        }
        ResultPaginationDTO result = restaurantService.getNearbyRestaurants(latitude, longitude, searchKeyword, spec,
                pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/restaurants/{id}")
    @ApiMessage("Get restaurant by id")
    public ResponseEntity<ResRestaurantDTO> getRestaurantById(@PathVariable("id") Long id) throws IdInvalidException {
        // Get current user if logged in (for tracking)
        User currentUser = null;
        try {
            String email = SecurityUtil.getCurrentUserLogin().orElse(null);
            if (email != null) {
                currentUser = userService.handleGetUserByUsername(email);
            }
        } catch (Exception e) {
            // Ignore - user is not logged in
        }
        
        // Use tracking method if user is logged in, otherwise use regular method
        ResRestaurantDTO restaurant = currentUser != null 
            ? restaurantService.getRestaurantDTOByIdWithTracking(id, currentUser)
            : restaurantService.getRestaurantDTOById(id);
            
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + id);
        }
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/restaurants/{id}/menu")
    @ApiMessage("Get restaurant menu by id")
    public ResponseEntity<com.example.FoodDelivery.domain.res.restaurant.ResRestaurantMenuDTO> getRestaurantMenu(
            @PathVariable("id") Long id) throws IdInvalidException {
        com.example.FoodDelivery.domain.res.restaurant.ResRestaurantMenuDTO menu = restaurantService
                .getRestaurantMenuDTOById(id);
        if (menu == null) {
            throw new IdInvalidException("Restaurant not found with id: " + id);
        }
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/restaurants/slug/{slug}")
    @ApiMessage("Get restaurant by slug")
    public ResponseEntity<ResRestaurantDTO> getRestaurantBySlug(@PathVariable("slug") String slug)
            throws IdInvalidException {
        ResRestaurantDTO restaurant = restaurantService.getRestaurantDTOBySlug(slug);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with slug: " + slug);
        }
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/restaurants/{id}")
    @ApiMessage("Delete restaurant by id")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("id") Long id) throws IdInvalidException {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + id);
        }
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/restaurants/{id}/open")
    @ApiMessage("Owner opens restaurant")
    public ResponseEntity<ResRestaurantDTO> openRestaurant(@PathVariable("id") Long id) throws IdInvalidException {
        ResRestaurantDTO restaurant = restaurantService.openRestaurant(id);
        return ResponseEntity.ok(restaurant);
    }

    @PostMapping("/restaurants/{id}/close")
    @ApiMessage("Owner closes restaurant")
    public ResponseEntity<ResRestaurantDTO> closeRestaurant(@PathVariable("id") Long id) throws IdInvalidException {
        ResRestaurantDTO restaurant = restaurantService.closeRestaurant(id);
        return ResponseEntity.ok(restaurant);
    }
}
