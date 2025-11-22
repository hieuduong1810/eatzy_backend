package com.example.FoodDelivery.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;

    public RestaurantService(RestaurantRepository restaurantRepository, UserService userService) {
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
    }

    public boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    public Restaurant getRestaurantById(Long id) {
        Optional<Restaurant> restaurantOpt = this.restaurantRepository.findById(id);
        if (restaurantOpt.isPresent()) {
            return restaurantOpt.get();
        }
        return null;
    }

    public Restaurant createRestaurant(Restaurant restaurant) throws IdInvalidException {
        // check owner exists
        if (restaurant.getOwner() != null) {
            User owner = this.userService.getUserById(restaurant.getOwner().getId());
            if (owner == null) {
                throw new IdInvalidException("Owner not found with id: " + restaurant.getOwner().getId());
            }
            restaurant.setOwner(owner);
        }
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(Restaurant restaurant) throws IdInvalidException {
        // check id
        Restaurant currentRestaurant = getRestaurantById(restaurant.getId());
        if (currentRestaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + restaurant.getId());
        }

        // update fields
        if (restaurant.getName() != null) {
            currentRestaurant.setName(restaurant.getName());
        }
        if (restaurant.getAddress() != null) {
            currentRestaurant.setAddress(restaurant.getAddress());
        }
        if (restaurant.getLatitude() != null) {
            currentRestaurant.setLatitude(restaurant.getLatitude());
        }
        if (restaurant.getLongitude() != null) {
            currentRestaurant.setLongitude(restaurant.getLongitude());
        }
        if (restaurant.getContactPhone() != null) {
            currentRestaurant.setContactPhone(restaurant.getContactPhone());
        }
        if (restaurant.getStatus() != null) {
            currentRestaurant.setStatus(restaurant.getStatus());
        }
        if (restaurant.getCommissionRate() != null) {
            currentRestaurant.setCommissionRate(restaurant.getCommissionRate());
        }
        if (restaurant.getSchedule() != null) {
            currentRestaurant.setSchedule(restaurant.getSchedule());
        }
        if (restaurant.getOwner() != null) {
            User owner = this.userService.getUserById(restaurant.getOwner().getId());
            if (owner == null) {
                throw new IdInvalidException("Owner not found with id: " + restaurant.getOwner().getId());
            }
            currentRestaurant.setOwner(owner);
        }

        return restaurantRepository.save(currentRestaurant);
    }

    public ResultPaginationDTO getAllRestaurants(Specification<Restaurant> spec, Pageable pageable) {
        Page<Restaurant> page = this.restaurantRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteRestaurant(Long id) {
        this.restaurantRepository.deleteById(id);
    }
}
