package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantDTO;
import com.example.FoodDelivery.service.RestaurantService;
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

@RestController
@RequestMapping("/api/v1")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
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

    @GetMapping("/restaurants/{id}")
    @ApiMessage("Get restaurant by id")
    public ResponseEntity<ResRestaurantDTO> getRestaurantById(@PathVariable("id") Long id) throws IdInvalidException {
        ResRestaurantDTO restaurant = restaurantService.getRestaurantDTOById(id);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + id);
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
}
