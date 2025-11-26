package com.example.FoodDelivery.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Permission;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.RestaurantType;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantDTO;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.repository.RestaurantTypeRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;
    private final RestaurantTypeRepository restaurantTypeRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserService userService,
            RestaurantTypeRepository restaurantTypeRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
        this.restaurantTypeRepository = restaurantTypeRepository;
    }

    public boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    private ResRestaurantDTO convertToResRestaurantDTO(Restaurant restaurant) {
        ResRestaurantDTO dto = new ResRestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setLatitude(restaurant.getLatitude());
        dto.setLongitude(restaurant.getLongitude());
        dto.setContactPhone(restaurant.getContactPhone());
        dto.setStatus(restaurant.getStatus());
        dto.setCommissionRate(restaurant.getCommissionRate());
        dto.setAverageRating(restaurant.getAverageRating());
        dto.setSchedule(restaurant.getSchedule());

        // Convert owner
        if (restaurant.getOwner() != null) {
            ResRestaurantDTO.User owner = new ResRestaurantDTO.User();
            owner.setId(restaurant.getOwner().getId());
            owner.setName(restaurant.getOwner().getName());
            dto.setOwner(owner);
        }

        // Convert restaurant types (take first one if exists)
        if (restaurant.getRestaurantTypes() != null && !restaurant.getRestaurantTypes().isEmpty()) {
            ResRestaurantDTO.RestaurantType type = new ResRestaurantDTO.RestaurantType();
            type.setId(restaurant.getRestaurantTypes().get(0).getId());
            type.setName(restaurant.getRestaurantTypes().get(0).getName());
            dto.setRestaurantTypes(type);
        }

        return dto;
    }

    public Restaurant getRestaurantById(Long id) {
        Optional<Restaurant> restaurantOpt = this.restaurantRepository.findById(id);
        if (restaurantOpt.isPresent()) {
            return restaurantOpt.get();
        }
        return null;
    }

    public ResRestaurantDTO getRestaurantDTOById(Long id) {
        Restaurant restaurant = getRestaurantById(id);
        if (restaurant == null) {
            return null;
        }
        return convertToResRestaurantDTO(restaurant);
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

        // check restaurant types exist
        if (restaurant.getRestaurantTypes() != null) {
            List<Long> reqRestaurant = restaurant.getRestaurantTypes().stream()
                    .map(restaurantType -> restaurantType.getId())
                    .collect(Collectors.toList());
            List<RestaurantType> restaurantTypes = this.restaurantTypeRepository.findByIdIn(reqRestaurant);
            restaurant.setRestaurantTypes(restaurantTypes);
        }

        return restaurantRepository.save(restaurant);
    }

    public ResRestaurantDTO createRestaurantDTO(Restaurant restaurant) throws IdInvalidException {
        Restaurant savedRestaurant = createRestaurant(restaurant);
        return convertToResRestaurantDTO(savedRestaurant);
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

        // check restaurant types exist
        if (restaurant.getRestaurantTypes() != null) {
            List<Long> reqRestaurant = restaurant.getRestaurantTypes().stream()
                    .map(restaurantType -> restaurantType.getId())
                    .collect(Collectors.toList());
            List<RestaurantType> restaurantTypes = this.restaurantTypeRepository.findByIdIn(reqRestaurant);
            currentRestaurant.setRestaurantTypes(restaurantTypes);
        }

        return restaurantRepository.save(currentRestaurant);
    }

    public ResRestaurantDTO updateRestaurantDTO(Restaurant restaurant) throws IdInvalidException {
        Restaurant updatedRestaurant = updateRestaurant(restaurant);
        return convertToResRestaurantDTO(updatedRestaurant);
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

    public ResultPaginationDTO getAllRestaurantsDTO(Specification<Restaurant> spec, Pageable pageable) {
        Page<Restaurant> page = this.restaurantRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        // Convert to DTO
        result.setResult(page.getContent().stream()
                .map(this::convertToResRestaurantDTO)
                .collect(Collectors.toList()));
        return result;
    }

    public void deleteRestaurant(Long id) {
        this.restaurantRepository.deleteById(id);
    }
}
