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
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantMagazineDTO;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.repository.RestaurantTypeRepository;
import com.example.FoodDelivery.util.SlugUtils;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;
    private final RestaurantTypeRepository restaurantTypeRepository;
    private final MapboxService mapboxService;
    private final SystemConfigurationService systemConfigurationService;

    public RestaurantService(RestaurantRepository restaurantRepository, UserService userService,
            RestaurantTypeRepository restaurantTypeRepository,
            MapboxService mapboxService,
            SystemConfigurationService systemConfigurationService) {
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
        this.restaurantTypeRepository = restaurantTypeRepository;
        this.mapboxService = mapboxService;
        this.systemConfigurationService = systemConfigurationService;
    }

    public boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    private ResRestaurantDTO convertToResRestaurantDTO(Restaurant restaurant) {
        ResRestaurantDTO dto = new ResRestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setSlug(restaurant.getSlug());
        dto.setAddress(restaurant.getAddress());
        dto.setDescription(restaurant.getDescription());
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

    private ResRestaurantMagazineDTO convertToResRestaurantMagazineDTO(Restaurant restaurant) {
        ResRestaurantMagazineDTO dto = new ResRestaurantMagazineDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setSlug(restaurant.getSlug());
        dto.setAddress(restaurant.getAddress());
        dto.setDescription(restaurant.getDescription());
        dto.setAverageRating(restaurant.getAverageRating());

        // Convert dish categories and dishes
        if (restaurant.getDishCategories() != null && !restaurant.getDishCategories().isEmpty()) {
            List<ResRestaurantMagazineDTO.Category> categories = restaurant.getDishCategories().stream()
                    .map(dishCategory -> {
                        ResRestaurantMagazineDTO.Category category = new ResRestaurantMagazineDTO.Category();
                        category.setId(dishCategory.getId());
                        category.setName(dishCategory.getName());

                        // Convert dishes for this category
                        if (dishCategory.getDishes() != null && !dishCategory.getDishes().isEmpty()) {
                            List<ResRestaurantMagazineDTO.Category.Dish> dishes = dishCategory.getDishes().stream()
                                    .map(dish -> {
                                        ResRestaurantMagazineDTO.Category.Dish dishDTO = new ResRestaurantMagazineDTO.Category.Dish();
                                        dishDTO.setId(dish.getId());
                                        dishDTO.setName(dish.getName());
                                        dishDTO.setDescription(dish.getDescription());
                                        dishDTO.setPrice(dish.getPrice());
                                        dishDTO.setImageUrl(dish.getImageUrl());
                                        return dishDTO;
                                    })
                                    .collect(Collectors.toList());
                            category.setDishes(dishes);
                        }

                        return category;
                    })
                    .collect(Collectors.toList());
            dto.setCategory(categories);
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

    public Restaurant getRestaurantBySlug(String slug) {
        Optional<Restaurant> restaurantOpt = this.restaurantRepository.findBySlug(slug);
        return restaurantOpt.orElse(null);
    }

    public ResRestaurantDTO getRestaurantDTOBySlug(String slug) {
        Restaurant restaurant = getRestaurantBySlug(slug);
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

        // Generate slug from restaurant name
        if (restaurant.getSlug() == null || restaurant.getSlug().isEmpty()) {
            String baseSlug = SlugUtils.generateSlug(restaurant.getName());
            String uniqueSlug = baseSlug;
            int counter = 2;

            // Ensure slug is unique
            while (restaurantRepository.existsBySlug(uniqueSlug)) {
                uniqueSlug = SlugUtils.makeUniqueSlug(baseSlug, counter);
                counter++;
            }

            restaurant.setSlug(uniqueSlug);
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

            // Regenerate slug when name changes
            String baseSlug = SlugUtils.generateSlug(restaurant.getName());
            String uniqueSlug = baseSlug;
            int counter = 2;

            // Ensure slug is unique (excluding current restaurant)
            while (restaurantRepository.existsBySlug(uniqueSlug) &&
                    !uniqueSlug.equals(currentRestaurant.getSlug())) {
                uniqueSlug = SlugUtils.makeUniqueSlug(baseSlug, counter);
                counter++;
            }

            currentRestaurant.setSlug(uniqueSlug);
        }
        if (restaurant.getAddress() != null) {
            currentRestaurant.setAddress(restaurant.getAddress());
        }
        if (restaurant.getDescription() != null) {
            currentRestaurant.setDescription(restaurant.getDescription());
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

    /**
     * Owner opens restaurant - set status to OPEN
     */
    public ResRestaurantDTO openRestaurant(Long restaurantId) throws IdInvalidException {
        Restaurant restaurant = getRestaurantById(restaurantId);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + restaurantId);
        }

        restaurant.setStatus("OPEN");
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return convertToResRestaurantDTO(savedRestaurant);
    }

    /**
     * Owner closes restaurant - set status to CLOSED
     */
    public ResRestaurantDTO closeRestaurant(Long restaurantId) throws IdInvalidException {
        Restaurant restaurant = getRestaurantById(restaurantId);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + restaurantId);
        }

        restaurant.setStatus("CLOSED");
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return convertToResRestaurantDTO(savedRestaurant);
    }

    /**
     * Get restaurants within maximum distance from user location
     * Uses Mapbox API to calculate real driving distance
     * Supports filtering and pagination
     * 
     * @param latitude  User's latitude
     * @param longitude User's longitude
     * @param spec      Specification for filtering
     * @param pageable  Pagination information
     * @return Paginated list of nearby restaurants with distance
     */
    public ResultPaginationDTO getNearbyRestaurants(BigDecimal latitude, BigDecimal longitude,
            Specification<Restaurant> spec, Pageable pageable) {
        // Get max distance from configuration (default 10 km)
        BigDecimal maxDistanceKm = new BigDecimal("10.0");
        try {
            SystemConfiguration config = systemConfigurationService
                    .getSystemConfigurationByKey("MAX_RESTAURANT_DISTANCE_KM");
            if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                maxDistanceKm = new BigDecimal(config.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Failed to get MAX_RESTAURANT_DISTANCE_KM config, using default {} km", maxDistanceKm);
        }

        // Get filtered restaurants using spec
        List<Restaurant> filteredRestaurants;
        if (spec != null) {
            filteredRestaurants = restaurantRepository.findAll(spec);
        } else {
            filteredRestaurants = restaurantRepository.findAll();
        }

        log.info("Checking {} restaurants within {} km from location ({}, {})",
                filteredRestaurants.size(), maxDistanceKm, latitude, longitude);

        // Calculate distances and filter by max distance
        List<ResRestaurantMagazineDTO> nearbyRestaurants = new ArrayList<>();
        for (Restaurant restaurant : filteredRestaurants) {
            // Skip restaurants without location data
            if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
                continue;
            }

            // Calculate driving distance using Mapbox API
            BigDecimal distance = mapboxService.getDrivingDistance(
                    latitude,
                    longitude,
                    restaurant.getLatitude(),
                    restaurant.getLongitude());

            // Skip if Mapbox API fails
            if (distance == null) {
                log.warn("Failed to calculate distance for restaurant: {}", restaurant.getName());
                continue;
            }

            // Check if restaurant is within max distance
            if (distance.compareTo(maxDistanceKm) <= 0) {
                ResRestaurantMagazineDTO dto = convertToResRestaurantMagazineDTO(restaurant);
                dto.setDistance(distance);
                nearbyRestaurants.add(dto);
                log.debug("Restaurant {} is {} km away (within {} km limit)",
                        restaurant.getName(), distance, maxDistanceKm);
            }
        }

        // Sort by distance (closest first)
        nearbyRestaurants.sort(Comparator.comparing(ResRestaurantMagazineDTO::getDistance));

        log.info("Found {} restaurants within {} km", nearbyRestaurants.size(), maxDistanceKm);

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), nearbyRestaurants.size());
        List<ResRestaurantMagazineDTO> paginatedRestaurants = start < nearbyRestaurants.size()
                ? nearbyRestaurants.subList(start, end)
                : new ArrayList<>();

        // Build result with pagination meta
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal((long) nearbyRestaurants.size());
        meta.setPages((int) Math.ceil((double) nearbyRestaurants.size() / pageable.getPageSize()));
        result.setMeta(meta);
        result.setResult(paginatedRestaurants);

        return result;
    }
}
