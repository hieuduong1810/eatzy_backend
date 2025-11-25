package com.example.FoodDelivery.service;

import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.repository.DishRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.util.SlugUtil;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class SeoService {

    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;

    public SeoService(RestaurantRepository restaurantRepository, DishRepository dishRepository) {
        this.restaurantRepository = restaurantRepository;
        this.dishRepository = dishRepository;
    }

    /**
     * Auto-generate SEO fields for Restaurant if not provided
     */
    public void generateRestaurantSeo(Restaurant restaurant) {
        // Generate slug if not provided
        if (restaurant.getSlug() == null || restaurant.getSlug().isEmpty()) {
            String baseSlug = SlugUtil.toSlug(restaurant.getName());
            restaurant.setSlug(ensureUniqueRestaurantSlug(baseSlug, null));
        }

        // Generate meta title if not provided
        if (restaurant.getMetaTitle() == null || restaurant.getMetaTitle().isEmpty()) {
            restaurant.setMetaTitle(SlugUtil.generateMetaTitle(restaurant.getName(), "Food Delivery"));
        }

        // Generate meta description if not provided
        if (restaurant.getMetaDescription() == null || restaurant.getMetaDescription().isEmpty()) {
            String description = "Đặt món từ " + restaurant.getName();
            if (restaurant.getAddress() != null) {
                description += " tại " + restaurant.getAddress();
            }
            description += ". Giao hàng nhanh, đồ ăn ngon.";
            restaurant.setMetaDescription(SlugUtil.truncateForMeta(description, 160));
        }

        // Use imageUrl as ogImage if not provided
        if (restaurant.getOgImage() == null || restaurant.getOgImage().isEmpty()) {
            // You can set a default restaurant image here
            restaurant.setOgImage(null); // Will be set when restaurant uploads image
        }
    }

    /**
     * Auto-generate SEO fields for Dish if not provided
     */
    public void generateDishSeo(Dish dish) {
        // Generate slug if not provided
        if (dish.getSlug() == null || dish.getSlug().isEmpty()) {
            String baseSlug = SlugUtil.toSlug(dish.getName());
            dish.setSlug(ensureUniqueDishSlug(baseSlug, null));
        }

        // Generate meta title if not provided
        if (dish.getMetaTitle() == null || dish.getMetaTitle().isEmpty()) {
            String restaurantName = dish.getRestaurant() != null ? dish.getRestaurant().getName() : "";
            dish.setMetaTitle(SlugUtil.generateMetaTitle(dish.getName(), restaurantName));
        }

        // Generate meta description if not provided
        if (dish.getMetaDescription() == null || dish.getMetaDescription().isEmpty()) {
            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                dish.setMetaDescription(SlugUtil.generateMetaDescription(dish.getDescription()));
            } else {
                String description = dish.getName();
                if (dish.getRestaurant() != null) {
                    description += " từ " + dish.getRestaurant().getName();
                }
                if (dish.getPrice() != null) {
                    description += ". Giá chỉ " + dish.getPrice() + "đ";
                }
                description += ". Đặt ngay!";
                dish.setMetaDescription(SlugUtil.truncateForMeta(description, 160));
            }
        }

        // Use imageUrl as ogImage if not provided
        if (dish.getOgImage() == null || dish.getOgImage().isEmpty()) {
            dish.setOgImage(dish.getImageUrl());
        }
    }

    /**
     * Ensure slug is unique for Restaurant by appending counter if needed
     */
    public String ensureUniqueRestaurantSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists;
            if (excludeId != null) {
                exists = restaurantRepository.existsBySlugAndIdNot(slug, excludeId);
            } else {
                exists = restaurantRepository.existsBySlug(slug);
            }

            if (!exists) {
                return slug;
            }

            counter++;
            slug = SlugUtil.generateUniqueSlug(baseSlug, counter);
        }
    }

    /**
     * Ensure slug is unique for Dish by appending counter if needed
     */
    public String ensureUniqueDishSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists;
            if (excludeId != null) {
                exists = dishRepository.existsBySlugAndIdNot(slug, excludeId);
            } else {
                exists = dishRepository.existsBySlug(slug);
            }

            if (!exists) {
                return slug;
            }

            counter++;
            slug = SlugUtil.generateUniqueSlug(baseSlug, counter);
        }
    }

    /**
     * Update Restaurant SEO fields
     */
    public Restaurant updateRestaurantSeo(Long restaurantId, String slug, String metaTitle, String metaDescription,
            String metaKeywords, String ogImage) throws IdInvalidException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IdInvalidException("Restaurant not found"));

        if (slug != null && !slug.isEmpty()) {
            String newSlug = SlugUtil.toSlug(slug);
            restaurant.setSlug(ensureUniqueRestaurantSlug(newSlug, restaurantId));
        }

        if (metaTitle != null) {
            restaurant.setMetaTitle(SlugUtil.truncateForMeta(metaTitle, 70));
        }

        if (metaDescription != null) {
            restaurant.setMetaDescription(SlugUtil.truncateForMeta(metaDescription, 160));
        }

        if (metaKeywords != null) {
            restaurant.setMetaKeywords(metaKeywords);
        }

        if (ogImage != null) {
            restaurant.setOgImage(ogImage);
        }

        return restaurantRepository.save(restaurant);
    }

    /**
     * Update Dish SEO fields
     */
    public Dish updateDishSeo(Long dishId, String slug, String metaTitle, String metaDescription,
            String metaKeywords, String ogImage) throws IdInvalidException {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new IdInvalidException("Dish not found"));

        if (slug != null && !slug.isEmpty()) {
            String newSlug = SlugUtil.toSlug(slug);
            dish.setSlug(ensureUniqueDishSlug(newSlug, dishId));
        }

        if (metaTitle != null) {
            dish.setMetaTitle(SlugUtil.truncateForMeta(metaTitle, 70));
        }

        if (metaDescription != null) {
            dish.setMetaDescription(SlugUtil.truncateForMeta(metaDescription, 160));
        }

        if (metaKeywords != null) {
            dish.setMetaKeywords(metaKeywords);
        }

        if (ogImage != null) {
            dish.setOgImage(ogImage);
        }

        return dishRepository.save(dish);
    }

    /**
     * Get Restaurant by slug
     */
    public Restaurant getRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug).orElse(null);
    }

    /**
     * Get Dish by slug
     */
    public Dish getDishBySlug(String slug) {
        return dishRepository.findBySlug(slug).orElse(null);
    }
}
