package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.DishCategory;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.MenuOptionGroup;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResDishDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionGroupDTO;
import com.example.FoodDelivery.domain.res.restaurant.ResMenuOptionDTO;
import com.example.FoodDelivery.repository.DishRepository;
import com.example.FoodDelivery.repository.MenuOptionGroupRepository;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class DishService {
    private final DishRepository dishRepository;
    private final RestaurantService restaurantService;
    private final DishCategoryService dishCategoryService;
    private final MenuOptionGroupRepository menuOptionGroupRepository;
    private final MenuOptionRepository menuOptionRepository;

    public DishService(DishRepository dishRepository, RestaurantService restaurantService,
            DishCategoryService dishCategoryService, MenuOptionGroupRepository menuOptionGroupRepository,
            MenuOptionRepository menuOptionRepository) {
        this.dishRepository = dishRepository;
        this.restaurantService = restaurantService;
        this.dishCategoryService = dishCategoryService;
        this.menuOptionGroupRepository = menuOptionGroupRepository;
        this.menuOptionRepository = menuOptionRepository;
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

    public ResDishDTO convertToResDishDTO(Dish dish) {
        if (dish == null) {
            return null;
        }
        ResDishDTO dto = new ResDishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getPrice());
        dto.setAvailabilityQuantity(dish.getAvailabilityQuantity());
        dto.setImageUrl(dish.getImageUrl());

        if (dish.getMenuOptionGroups() != null) {
            dto.setMenuOptionGroupCount(dish.getMenuOptionGroups().size());
            List<ResMenuOptionGroupDTO> groupDTOs = dish.getMenuOptionGroups().stream()
                    .map(group -> {
                        ResMenuOptionGroupDTO groupDTO = new ResMenuOptionGroupDTO();
                        groupDTO.setId(group.getId());
                        groupDTO.setName(group.getGroupName());
                        groupDTO.setMinChoices(group.getMinChoices());
                        groupDTO.setMaxChoices(group.getMaxChoices());

                        if (group.getMenuOptions() != null) {
                            List<ResMenuOptionDTO> optionDTOs = group.getMenuOptions().stream()
                                    .map(option -> {
                                        ResMenuOptionDTO optionDTO = new ResMenuOptionDTO();
                                        optionDTO.setId(option.getId());
                                        optionDTO.setName(option.getName());
                                        optionDTO.setPriceAdjustment(option.getPriceAdjustment());
                                        optionDTO.setAvailable(
                                                option.getIsAvailable() != null ? option.getIsAvailable() : false);
                                        return optionDTO;
                                    })
                                    .collect(Collectors.toList());
                            groupDTO.setMenuOptions(optionDTOs);
                        }
                        return groupDTO;
                    })
                    .collect(Collectors.toList());
            dto.setMenuOptionGroups(groupDTOs);
        } else {
            dto.setMenuOptionGroupCount(0);
            dto.setMenuOptionGroups(new ArrayList<>());
        }

        return dto;
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

    /**
     * Update dish with nested menuOptionGroups and menuOptions
     * - If group/option id is null -> create new
     * - If group/option id exists -> update
     * - Groups/options not in request will be deleted
     */
    @Transactional
    public Dish updateDishWithMenuOptions(ResDishDTO dto) throws IdInvalidException {
        // Check dish exists
        Dish currentDish = getDishById(dto.getId());
        if (currentDish == null) {
            throw new IdInvalidException("Dish not found with id: " + dto.getId());
        }

        // Update basic dish fields
        if (dto.getName() != null) {
            if (!currentDish.getName().equals(dto.getName())) {
                if (this.existsByNameAndRestaurantId(dto.getName(), currentDish.getRestaurant().getId())) {
                    throw new IdInvalidException("Dish name already exists in this restaurant: " + dto.getName());
                }
            }
            currentDish.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            currentDish.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            currentDish.setPrice(dto.getPrice());
        }
        if (dto.getImageUrl() != null) {
            currentDish.setImageUrl(dto.getImageUrl());
        }
        if (dto.getAvailabilityQuantity() >= 0) {
            currentDish.setAvailabilityQuantity(dto.getAvailabilityQuantity());
        }

        // Save dish first to ensure it exists for relationships
        currentDish = dishRepository.save(currentDish);

        // Handle menuOptionGroups
        if (dto.getMenuOptionGroups() != null) {
            // Get existing groups from database
            List<MenuOptionGroup> existingGroups = menuOptionGroupRepository.findByDishId(currentDish.getId());
            Set<Long> requestGroupIds = new HashSet<>();

            for (ResMenuOptionGroupDTO groupDTO : dto.getMenuOptionGroups()) {
                MenuOptionGroup group;

                if (groupDTO.getId() == null) {
                    // Create new group
                    group = new MenuOptionGroup();
                    group.setDish(currentDish);
                } else {
                    // Update existing group
                    requestGroupIds.add(groupDTO.getId());
                    group = menuOptionGroupRepository.findById(groupDTO.getId()).orElse(null);
                    if (group == null) {
                        throw new IdInvalidException("MenuOptionGroup not found with id: " + groupDTO.getId());
                    }
                }

                if (groupDTO.getName() != null) {
                    group.setGroupName(groupDTO.getName());
                }
                if (groupDTO.getMinChoices() != null) {
                    group.setMinChoices(groupDTO.getMinChoices());
                }
                if (groupDTO.getMaxChoices() != null) {
                    group.setMaxChoices(groupDTO.getMaxChoices());
                }

                group = menuOptionGroupRepository.save(group);

                // Handle menuOptions within this group
                if (groupDTO.getMenuOptions() != null) {
                    List<MenuOption> existingOptions = menuOptionRepository.findByMenuOptionGroupId(group.getId());
                    Set<Long> requestOptionIds = new HashSet<>();

                    for (ResMenuOptionDTO optionDTO : groupDTO.getMenuOptions()) {
                        MenuOption option;

                        if (optionDTO.getId() == null) {
                            // Create new option
                            option = new MenuOption();
                            option.setMenuOptionGroup(group);
                        } else {
                            // Update existing option
                            requestOptionIds.add(optionDTO.getId());
                            option = menuOptionRepository.findById(optionDTO.getId()).orElse(null);
                            if (option == null) {
                                throw new IdInvalidException("MenuOption not found with id: " + optionDTO.getId());
                            }
                        }

                        if (optionDTO.getName() != null) {
                            option.setName(optionDTO.getName());
                        }
                        if (optionDTO.getPriceAdjustment() != null) {
                            option.setPriceAdjustment(optionDTO.getPriceAdjustment());
                        }
                        option.setIsAvailable(optionDTO.isAvailable());

                        menuOptionRepository.save(option);
                    }

                    // Delete options not in request
                    for (MenuOption existingOption : existingOptions) {
                        if (!requestOptionIds.contains(existingOption.getId())) {
                            menuOptionRepository.delete(existingOption);
                        }
                    }
                }
            }

            // Delete groups not in request
            for (MenuOptionGroup existingGroup : existingGroups) {
                if (!requestGroupIds.contains(existingGroup.getId())) {
                    // Delete all options in this group first
                    List<MenuOption> groupOptions = menuOptionRepository.findByMenuOptionGroupId(existingGroup.getId());
                    menuOptionRepository.deleteAll(groupOptions);
                    menuOptionGroupRepository.delete(existingGroup);
                }
            }
        }

        // Refresh and return the dish
        return getDishById(currentDish.getId());
    }
}
