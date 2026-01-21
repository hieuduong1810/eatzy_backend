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
import com.example.FoodDelivery.domain.res.restaurant.ResRestaurantMenuDTO;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.repository.RestaurantTypeRepository;
import com.example.FoodDelivery.repository.UserRestaurantScoreRepository;
import com.example.FoodDelivery.repository.UserTypeScoreRepository;
import com.example.FoodDelivery.util.SlugUtils;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final RedisCacheService redisCacheService;
    private final UserTypeScoreRepository userTypeScoreRepository;
    private final UserRestaurantScoreRepository userRestaurantScoreRepository;
    private final UserScoringService userScoringService;

    public RestaurantService(RestaurantRepository restaurantRepository, UserService userService,
            RestaurantTypeRepository restaurantTypeRepository,
            MapboxService mapboxService,
            SystemConfigurationService systemConfigurationService,
            RedisCacheService redisCacheService,
            UserTypeScoreRepository userTypeScoreRepository,
            UserRestaurantScoreRepository userRestaurantScoreRepository,
            @org.springframework.context.annotation.Lazy UserScoringService userScoringService) {
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
        this.restaurantTypeRepository = restaurantTypeRepository;
        this.mapboxService = mapboxService;
        this.systemConfigurationService = systemConfigurationService;
        this.redisCacheService = redisCacheService;
        this.userTypeScoreRepository = userTypeScoreRepository;
        this.userRestaurantScoreRepository = userRestaurantScoreRepository;
        this.userScoringService = userScoringService;
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
        dto.setOneStarCount(restaurant.getOneStarCount());
        dto.setTwoStarCount(restaurant.getTwoStarCount());
        dto.setThreeStarCount(restaurant.getThreeStarCount());
        dto.setFourStarCount(restaurant.getFourStarCount());
        dto.setFiveStarCount(restaurant.getFiveStarCount());
        dto.setAverageRating(restaurant.getAverageRating());

        // Calculate total review count
        int reviewCount = 0;
        if (restaurant.getOneStarCount() != null)
            reviewCount += restaurant.getOneStarCount();
        if (restaurant.getTwoStarCount() != null)
            reviewCount += restaurant.getTwoStarCount();
        if (restaurant.getThreeStarCount() != null)
            reviewCount += restaurant.getThreeStarCount();
        if (restaurant.getFourStarCount() != null)
            reviewCount += restaurant.getFourStarCount();
        if (restaurant.getFiveStarCount() != null)
            reviewCount += restaurant.getFiveStarCount();
        dto.setReviewCount(reviewCount);

        dto.setSchedule(restaurant.getSchedule());
        dto.setAvatarUrl(restaurant.getAvatarUrl());
        dto.setCoverImageUrl(restaurant.getCoverImageUrl());

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

    private ResRestaurantMenuDTO convertToResRestaurantMenuDTO(Restaurant restaurant) {
        ResRestaurantMenuDTO dto = new ResRestaurantMenuDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());

        // Convert dish categories and dishes
        if (restaurant.getDishCategories() != null && !restaurant.getDishCategories().isEmpty()) {
            List<com.example.FoodDelivery.domain.res.restaurant.ResDishCategoryDTO> categories = restaurant
                    .getDishCategories().stream()
                    .map(dishCategory -> {
                        com.example.FoodDelivery.domain.res.restaurant.ResDishCategoryDTO category = new com.example.FoodDelivery.domain.res.restaurant.ResDishCategoryDTO();
                        category.setId(dishCategory.getId());
                        category.setName(dishCategory.getName());

                        // Convert dishes for this category
                        if (dishCategory.getDishes() != null && !dishCategory.getDishes().isEmpty()) {
                            List<com.example.FoodDelivery.domain.res.restaurant.ResDishDTO> dishes = dishCategory
                                    .getDishes().stream()
                                    .map(dish -> {
                                        com.example.FoodDelivery.domain.res.restaurant.ResDishDTO dishDTO = new com.example.FoodDelivery.domain.res.restaurant.ResDishDTO();
                                        dishDTO.setId(dish.getId());
                                        dishDTO.setName(dish.getName());
                                        dishDTO.setDescription(dish.getDescription());
                                        dishDTO.setPrice(dish.getPrice());
                                        dishDTO.setAvailabilityQuantity(dish.getAvailabilityQuantity());
                                        dishDTO.setImageUrl(dish.getImageUrl());

                                        // Convert menu option groups
                                        if (dish.getMenuOptionGroups() != null
                                                && !dish.getMenuOptionGroups().isEmpty()) {
                                            List<com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionGroupDTO> optionGroups = dish
                                                    .getMenuOptionGroups().stream()
                                                    .map(optionGroup -> {
                                                        com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionGroupDTO groupDTO = new com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionGroupDTO();
                                                        groupDTO.setId(optionGroup.getId());
                                                        groupDTO.setName(optionGroup.getGroupName());

                                                        // Convert menu options
                                                        if (optionGroup.getMenuOptions() != null
                                                                && !optionGroup.getMenuOptions().isEmpty()) {
                                                            List<com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionDTO> options = optionGroup
                                                                    .getMenuOptions().stream()
                                                                    .map(option -> {
                                                                        com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionDTO optionDTO = new com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionDTO();
                                                                        optionDTO.setId(option.getId());
                                                                        optionDTO.setName(option.getName());
                                                                        optionDTO.setPriceAdjustment(
                                                                                option.getPriceAdjustment());
                                                                        optionDTO.setAvailable(option.getIsAvailable());
                                                                        return optionDTO;
                                                                    })
                                                                    .collect(Collectors.toList());
                                                            groupDTO.setMenuOptions(options);
                                                        }

                                                        return groupDTO;
                                                    })
                                                    .collect(Collectors.toList());
                                            dishDTO.setMenuOptionGroups(optionGroups);
                                            dishDTO.setMenuOptionGroupCount(optionGroups.size());
                                        } else {
                                            dishDTO.setMenuOptionGroupCount(0);
                                        }

                                        return dishDTO;
                                    })
                                    .collect(Collectors.toList());
                            category.setDishes(dishes);
                        }

                        return category;
                    })
                    .collect(Collectors.toList());
            dto.setDishes(categories);
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
        dto.setOneStarCount(restaurant.getOneStarCount());
        dto.setTwoStarCount(restaurant.getTwoStarCount());
        dto.setThreeStarCount(restaurant.getThreeStarCount());
        dto.setFourStarCount(restaurant.getFourStarCount());
        dto.setFiveStarCount(restaurant.getFiveStarCount());
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

    /**
     * Get restaurant DTO by ID with user tracking for recommendations
     * 
     * @param id   Restaurant ID
     * @param user Current user (can be null for anonymous access)
     * @return Restaurant DTO
     */
    public ResRestaurantDTO getRestaurantDTOByIdWithTracking(Long id, User user) {
        Restaurant restaurant = getRestaurantById(id);
        if (restaurant == null) {
            return null;
        }

        // Track user view for scoring (only if user is logged in)
        if (user != null && userScoringService != null) {
            userScoringService.trackViewRestaurantDetails(user, restaurant);
            log.info("👁️ User {} viewed restaurant {}: tracking for recommendations", user.getId(),
                    restaurant.getId());
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

    public com.example.FoodDelivery.domain.res.restaurant.ResRestaurantMenuDTO getRestaurantMenuDTOById(Long id) {
        Restaurant restaurant = getRestaurantById(id);
        if (restaurant == null) {
            return null;
        }
        return convertToResRestaurantMenuDTO(restaurant);
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

        Restaurant savedRestaurant = restaurantRepository.save(currentRestaurant);

        // Clear search cache when restaurant data changes
        clearSearchCache();

        return savedRestaurant;
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
     * Supports personalized ranking if user is logged in
     * WITH REDIS CACHE for search results
     * 
     * @param latitude      User's latitude
     * @param longitude     User's longitude
     * @param searchKeyword Search keyword for restaurant name, dish name, or
     *                      category name
     * @param spec          Specification for additional filtering
     * @param pageable      Pagination information
     * @return Paginated list of nearby restaurants with distance and optional
     *         ranking scores
     */
    public ResultPaginationDTO getNearbyRestaurants(BigDecimal latitude, BigDecimal longitude,
            String searchKeyword, Specification<Restaurant> spec, Pageable pageable) {

        // Get current user if logged in (for personalized ranking)
        User currentUser = null;
        try {
            String email = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin().orElse(null);
            if (email != null && !email.isEmpty()) {
                currentUser = this.userService.handleGetUserByUsername(email);
                if (currentUser != null) {
                    log.info("🔐 User {} logged in - enabling personalized ranking", currentUser.getId());
                }
            }
        } catch (Exception e) {
            log.debug("No authenticated user - using default ranking");
        }

        // 1. Build cache key (include userId for personalized cache)
        String cacheKey = buildCacheKey(latitude, longitude, searchKeyword, pageable,
                currentUser != null ? currentUser.getId() : null);

        // 2. Check cache first
        Object cachedResult = redisCacheService.get(cacheKey);
        if (cachedResult != null && cachedResult instanceof ResultPaginationDTO) {
            log.info("🎯 CACHE HIT for key: {}", cacheKey);
            return (ResultPaginationDTO) cachedResult;
        }

        log.info("❌ CACHE MISS for key: {}", cacheKey);

        // 3. Cache miss - Query database
        // Get max distance from configuration (default 10 km)
        BigDecimal maxDistanceKm = new BigDecimal("10.0");
        try {
            com.example.FoodDelivery.domain.SystemConfiguration config = systemConfigurationService
                    .getSystemConfigurationByKey("MAX_RESTAURANT_DISTANCE_KM");
            if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
                maxDistanceKm = new BigDecimal(config.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Failed to get MAX_RESTAURANT_DISTANCE_KM config, using default {} km", maxDistanceKm);
        }

        // Build search specification if keyword is provided
        Specification<Restaurant> finalSpec = spec;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            String keyword = "%" + searchKeyword.trim().toLowerCase() + "%";

            Specification<Restaurant> searchSpec = (root, query, criteriaBuilder) -> {
                // Search in restaurant name
                var restaurantNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);

                // Search in dish names (via dishCategories -> dishes)
                var dishSubquery = query.subquery(Long.class);
                var dishRoot = dishSubquery.from(Restaurant.class);
                var dishCategoryJoin = dishRoot.join("dishCategories");
                var dishJoin = dishCategoryJoin.join("dishes");
                dishSubquery.select(dishRoot.get("id"))
                        .where(
                                criteriaBuilder.and(
                                        criteriaBuilder.equal(dishRoot.get("id"), root.get("id")),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(dishJoin.get("name")), keyword)));
                var dishPredicate = criteriaBuilder.exists(dishSubquery);

                // Search in category names (via dishCategories)
                var categorySubquery = query.subquery(Long.class);
                var categoryRoot = categorySubquery.from(Restaurant.class);
                var categoryJoin = categoryRoot.join("dishCategories");
                categorySubquery.select(categoryRoot.get("id"))
                        .where(
                                criteriaBuilder.and(
                                        criteriaBuilder.equal(categoryRoot.get("id"), root.get("id")),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(categoryJoin.get("name")), keyword)));
                var categoryPredicate = criteriaBuilder.exists(categorySubquery);

                // Combine with OR: restaurant name OR dish name OR category name
                return criteriaBuilder.or(restaurantNamePredicate, dishPredicate, categoryPredicate);
            };

            // Combine search spec with existing spec
            finalSpec = finalSpec != null ? finalSpec.and(searchSpec) : searchSpec;
        }

        // Get filtered restaurants using spec
        List<Restaurant> filteredRestaurants;
        if (finalSpec != null) {
            filteredRestaurants = restaurantRepository.findAll(finalSpec);
        } else {
            filteredRestaurants = restaurantRepository.findAll();
        }

        // Filter only ACTIVE or OPEN restaurants
        filteredRestaurants = filteredRestaurants.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()) || "OPEN".equals(r.getStatus()))
                .collect(Collectors.toList());

        log.info("Checking {} ACTIVE/OPEN restaurants within {} km from location ({}, {})",
                filteredRestaurants.size(), maxDistanceKm, latitude, longitude);

        // Calculate distances and scores
        List<ResRestaurantMagazineDTO> nearbyRestaurants = new ArrayList<>();
        final User finalCurrentUser = currentUser;

        // Track search scoring only once per search (not per restaurant)
        boolean hasTrackedSearchScoring = false;

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

                // Track user search scoring only once per search
                if (!hasTrackedSearchScoring && finalCurrentUser != null && searchKeyword != null
                        && !searchKeyword.trim().isEmpty()) {
                    boolean tracked = trackSearchScoring(finalCurrentUser, restaurant, searchKeyword);
                    if (tracked) {
                        hasTrackedSearchScoring = true;
                    }
                }

                // Calculate personalized scores if user is logged in
                if (finalCurrentUser != null) {
                    try {
                        double typeScore = calculateTypeScore(finalCurrentUser.getId(), restaurant);
                        double loyaltyScore = calculateLoyaltyScore(finalCurrentUser.getId(), restaurant);
                        double distanceScore = calculateDistanceScore(distance);
                        double qualityScore = calculateQualityScore(restaurant);
                        double finalScore = (typeScore * 0.40) + (loyaltyScore * 0.30) +
                                (distanceScore * 0.20) + (qualityScore * 0.10);

                        dto.setTypeScore(typeScore);
                        dto.setLoyaltyScore(loyaltyScore);
                        dto.setDistanceScore(distanceScore);
                        dto.setQualityScore(qualityScore);
                        dto.setFinalScore(finalScore);

                        log.debug("🎯 Restaurant {}: Type={}, Loyalty={}, Distance={}, Quality={}, Final={}",
                                restaurant.getName(), typeScore, loyaltyScore, distanceScore, qualityScore, finalScore);
                    } catch (Exception e) {
                        log.error("Error calculating scores for restaurant {}: {}", restaurant.getId(), e.getMessage());
                    }
                }

                nearbyRestaurants.add(dto);
                log.debug("Restaurant {} is {} km away (within {} km limit)",
                        restaurant.getName(), distance, maxDistanceKm);
            }
        }

        // Sort restaurants
        if (finalCurrentUser != null && !nearbyRestaurants.isEmpty()
                && nearbyRestaurants.get(0).getFinalScore() != null) {
            // Sort by personalized final score (highest first)
            nearbyRestaurants.sort(Comparator.comparing(ResRestaurantMagazineDTO::getFinalScore).reversed());
            log.info("📊 Sorted {} restaurants by personalized ranking for user {}",
                    nearbyRestaurants.size(), finalCurrentUser.getId());
        } else {
            // Sort by distance (closest first) - default for non-logged-in users
            nearbyRestaurants.sort(Comparator.comparing(ResRestaurantMagazineDTO::getDistance));
            log.info("📍 Sorted {} restaurants by distance (no personalization)", nearbyRestaurants.size());
        }

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

        // 4. Save to cache (TTL: 5 minutes)
        redisCacheService.set(cacheKey, result, 5, java.util.concurrent.TimeUnit.MINUTES);
        log.info("💾 Cached result with key: {}", cacheKey);

        return result;
    }

    /**
     * Build cache key for search results (including userId for personalized cache)
     */
    private String buildCacheKey(BigDecimal latitude, BigDecimal longitude,
            String searchKeyword, Pageable pageable, Long userId) {
        String keyword = searchKeyword != null ? searchKeyword.trim() : "all";
        String userKey = userId != null ? "user:" + userId : "guest";
        return String.format("search:nearby:%s:%s:%s:%s:page:%d:size:%d",
                latitude.toPlainString(),
                longitude.toPlainString(),
                keyword,
                userKey,
                pageable.getPageNumber(),
                pageable.getPageSize());
    }

    /**
     * Clear search cache (call when restaurant data changes)
     */
    public void clearSearchCache() {
        redisCacheService.deletePattern("search:nearby:*");
        log.info("🗑️  Cleared all search cache");
    }

    /**
     * Calculate S_Type: User's preference score for restaurant types (scaled to
     * 100)
     * Formula: S_Type(100) = Min(100, (RawScore / 200) × 100)
     * Threshold: 200 points (e.g., ~40 orders of same food type)
     */
    private double calculateTypeScore(Long userId, Restaurant restaurant) {
        if (restaurant.getRestaurantTypes() == null || restaurant.getRestaurantTypes().isEmpty()) {
            return 0.0;
        }

        List<Long> typeIds = restaurant.getRestaurantTypes().stream()
                .map(RestaurantType::getId)
                .collect(Collectors.toList());

        Integer rawScore = userTypeScoreRepository.getTotalTypeScoreForUser(userId, typeIds);
        if (rawScore == null || rawScore <= 0) {
            return 0.0;
        }

        // Scale to 100: Min(100, (RawScore / 200) × 100)
        double scaledScore = (rawScore / 200.0) * 100.0;
        return Math.min(100.0, scaledScore);
    }

    /**
     * Calculate S_Quen: User's loyalty/familiarity score with restaurant (scaled to
     * 100)
     * Formula: S_Quen(100) = Min(100, (RawScore / 50) × 100)
     * Threshold: 50 points (~5 orders at same restaurant = "loyal customer")
     */
    private double calculateLoyaltyScore(Long userId, Restaurant restaurant) {
        Integer rawScore = userRestaurantScoreRepository.getScoreOrDefault(userId, restaurant.getId());
        if (rawScore == null || rawScore <= 0) {
            return 0.0;
        }

        // Scale to 100: Min(100, (RawScore / 50) × 100)
        double scaledScore = (rawScore / 50.0) * 100.0;
        return Math.min(100.0, scaledScore);
    }

    /**
     * Calculate S_Gần: Distance score (100 at 0km, decrease 10 points per km)
     * Formula: Max(0, 100 - (distance × 10))
     */
    private double calculateDistanceScore(BigDecimal distanceKm) {
        double distance = distanceKm.doubleValue();
        double score = 100.0 - (distance * 10.0);
        return Math.max(0.0, score);
    }

    /**
     * Calculate S_Ngon: Quality score based on restaurant rating
     * Formula: rating × 20 (to scale to 100)
     */
    private double calculateQualityScore(Restaurant restaurant) {
        if (restaurant.getAverageRating() == null) {
            return 0.0;
        }
        return restaurant.getAverageRating().doubleValue() * 20.0;
    }

    /**
     * Track user search scoring based on how the restaurant matched the search
     * keyword
     * - If keyword matches restaurant name: +2 restaurant score, +2 type score
     * - If keyword matches restaurant type: 0 restaurant score, +2 type score
     * 
     * @param user          Current logged-in user
     * @param restaurant    Restaurant found in search results
     * @param searchKeyword The keyword user used to search
     * @return true if scoring was applied, false otherwise
     */
    private boolean trackSearchScoring(User user, Restaurant restaurant, String searchKeyword) {
        if (user == null || restaurant == null || userScoringService == null) {
            return false;
        }

        String keyword = searchKeyword.trim().toLowerCase();

        // Check if search keyword exactly matches restaurant name (case-insensitive)
        boolean matchedByRestaurantName = restaurant.getName() != null &&
                restaurant.getName().toLowerCase().equals(keyword);

        // Check if search keyword exactly matches any restaurant type name
        boolean matchedByRestaurantType = false;
        if (restaurant.getRestaurantTypes() != null) {
            matchedByRestaurantType = restaurant.getRestaurantTypes().stream()
                    .anyMatch(type -> type.getName() != null &&
                            type.getName().toLowerCase().equals(keyword));
        }

        // Apply scoring based on what matched
        if (matchedByRestaurantName) {
            // User searched by restaurant name: +2 restaurant, +2 type
            userScoringService.trackSearchRestaurantByNameAndClick(user, restaurant);
            log.info("🔍 User {} searched '{}' - matched restaurant name '{}': +2 restaurant, +2 type",
                    user.getId(), searchKeyword, restaurant.getName());
            return true;
        } else if (matchedByRestaurantType) {
            // User searched by restaurant type: 0 restaurant, +2 type
            userScoringService.trackSearchDishAndClick(user, restaurant);
            log.info("🍜 User {} searched '{}' - matched restaurant type in '{}': +2 type (once per search)",
                    user.getId(), searchKeyword, restaurant.getName());
            return true;
        }

        return false;
    }

    /**
     * Get the restaurant owned by the currently logged-in user
     * 
     * @return Restaurant owned by current user
     * @throws IdInvalidException if user is not logged in or doesn't own a
     *                            restaurant
     */
    public Restaurant getCurrentOwnerRestaurant() throws IdInvalidException {
        // Get current user's email from security context
        String email = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User is not logged in"));

        // Find user by email
        User currentUser = this.userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("User not found with email: " + email);
        }

        // Find restaurant by owner
        Optional<Restaurant> restaurantOpt = this.restaurantRepository.findByOwnerId(currentUser.getId());
        if (restaurantOpt.isEmpty()) {
            throw new IdInvalidException("No restaurant found for owner: " + currentUser.getName());
        }

        return restaurantOpt.get();
    }

    /**
     * Get the restaurant DTO owned by the currently logged-in user
     * 
     * @return ResRestaurantDTO for the restaurant owned by current user
     * @throws IdInvalidException if user is not logged in or doesn't own a
     *                            restaurant
     */
    public ResRestaurantDTO getCurrentOwnerRestaurantDTO() throws IdInvalidException {
        Restaurant restaurant = getCurrentOwnerRestaurant();
        return convertToResRestaurantDTO(restaurant);
    }
}
