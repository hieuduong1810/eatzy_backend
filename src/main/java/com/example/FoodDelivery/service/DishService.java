package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.DishCategory;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.DishRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class DishService {
    private final DishRepository dishRepository;
    private final RestaurantService restaurantService;
    private final DishCategoryService dishCategoryService;
    private SeoService seoService;

    public DishService(DishRepository dishRepository, RestaurantService restaurantService,
            DishCategoryService dishCategoryService) {
        this.dishRepository = dishRepository;
        this.restaurantService = restaurantService;
        this.dishCategoryService = dishCategoryService;
    }
    
    @Autowired
    public void setSeoService(@Lazy SeoService seoService) {
        this.seoService = seoService;
    }

    public boolean existsByNameAndRestaurantId(String name, Long restaurantId) {
        return dishRepository.existsByNameAndRestaurantId(name, restaurantId);
    }

    public Dish getDishById(Long id) {
        Optional<Dish> dishOpt = this.dishRepository.findById(id);
        return dishOpt.orElse(null);
    }

    public List<Dish> getDishesByRestaurantId(Long restaurantId) {
        return this.dishRepository.findByRestaurantId(restaurantId);
    }

    public List<Dish> getDishesByCategoryId(Long categoryId) {
        return this.dishRepository.findByCategoryId(categoryId);
    }

    public Dish createDish(Dish dish) throws IdInvalidException {
        // check restaurant exists
        if (dish.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(dish.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + dish.getRestaurant().getId());
            }

            // check duplicate name in same restaurant
            if (this.existsByNameAndRestaurantId(dish.getName(), restaurant.getId())) {
                throw new IdInvalidException("Dish name already exists in this restaurant: " + dish.getName());
            }

            dish.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        // check category exists and belongs to same restaurant
        if (dish.getCategory() != null) {
            DishCategory category = this.dishCategoryService.getDishCategoryById(dish.getCategory().getId());
            if (category == null) {
                throw new IdInvalidException("Category not found with id: " + dish.getCategory().getId());
            }
            if (!category.getRestaurant().getId().equals(dish.getRestaurant().getId())) {
                throw new IdInvalidException("Category does not belong to this restaurant");
            }
            dish.setCategory(category);
        }

        // Auto-generate SEO fields
        seoService.generateDishSeo(dish);

        return dishRepository.save(dish);
    }

    public Dish updateDish(Dish dish) throws IdInvalidException {
        // check id
        Dish currentDish = getDishById(dish.getId());
        if (currentDish == null) {
            throw new IdInvalidException("Dish not found with id: " + dish.getId());
        }

        // update fields
        if (dish.getName() != null) {
            // check duplicate name if name is changed
            if (!currentDish.getName().equals(dish.getName())) {
                if (this.existsByNameAndRestaurantId(dish.getName(), currentDish.getRestaurant().getId())) {
                    throw new IdInvalidException("Dish name already exists in this restaurant: " + dish.getName());
                }
            }
            currentDish.setName(dish.getName());
        }
        if (dish.getDescription() != null) {
            currentDish.setDescription(dish.getDescription());
        }
        if (dish.getPrice() != null) {
            currentDish.setPrice(dish.getPrice());
        }
        if (dish.getImageUrl() != null) {
            currentDish.setImageUrl(dish.getImageUrl());
        }
        if (dish.getAvailabilityQuantity() >= 0) {
            currentDish.setAvailabilityQuantity(dish.getAvailabilityQuantity());
        }
        if (dish.getCategory() != null) {
            DishCategory category = this.dishCategoryService.getDishCategoryById(dish.getCategory().getId());
            if (category == null) {
                throw new IdInvalidException("Category not found with id: " + dish.getCategory().getId());
            }
            if (!category.getRestaurant().getId().equals(currentDish.getRestaurant().getId())) {
                throw new IdInvalidException("Category does not belong to this restaurant");
            }
            currentDish.setCategory(category);
        }

        return dishRepository.save(currentDish);
    }

    public ResultPaginationDTO getAllDishes(Specification<Dish> spec, Pageable pageable) {
        Page<Dish> page = this.dishRepository.findAll(spec, pageable);
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

    public void deleteDish(Long id) {
        this.dishRepository.deleteById(id);
    }
}
