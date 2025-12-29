package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemOptionDTO;
import com.example.FoodDelivery.repository.CartItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.stream.Collectors;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final DishService dishService;

    public CartItemService(CartItemRepository cartItemRepository, CartService cartService,
            DishService dishService) {
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.dishService = dishService;
    }

    private ResCartItemDTO convertToResCartItemDTO(CartItem cartItem) {
        ResCartItemDTO dto = new ResCartItemDTO();
        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());

        // Convert dish
        if (cartItem.getDish() != null) {
            ResCartItemDTO.Dish dish = new ResCartItemDTO.Dish();
            dish.setId(cartItem.getDish().getId());
            dish.setName(cartItem.getDish().getName());
            dish.setPrice(cartItem.getDish().getPrice());
            dish.setImage(cartItem.getDish().getImageUrl());
            dto.setDish(dish);
        }

        // Convert cart item options list
        if (cartItem.getCartItemOptions() != null && !cartItem.getCartItemOptions().isEmpty()) {
            List<ResCartItemOptionDTO> optionDtos = cartItem.getCartItemOptions().stream()
                    .map(option -> {
                        ResCartItemOptionDTO optionDto = new ResCartItemOptionDTO();
                        optionDto.setId(option.getId());

                        // Convert menu option
                        if (option.getMenuOption() != null) {
                            ResCartItemOptionDTO.MenuOption menuOption = new ResCartItemOptionDTO.MenuOption();
                            menuOption.setId(option.getMenuOption().getId());
                            menuOption.setName(option.getMenuOption().getName());
                            menuOption.setPriceAdjustment(option.getMenuOption().getPriceAdjustment());
                            optionDto.setMenuOption(menuOption);
                        }

                        return optionDto;
                    })
                    .collect(Collectors.toList());
            dto.setCartItemOptions(optionDtos);
        }

        return dto;
    }

    public CartItem getCartItemById(Long id) {
        Optional<CartItem> cartItemOpt = this.cartItemRepository.findById(id);
        return cartItemOpt.orElse(null);
    }

    public ResCartItemDTO getCartItemDTOById(Long id) {
        CartItem cartItem = getCartItemById(id);
        if (cartItem == null) {
            return null;
        }
        return convertToResCartItemDTO(cartItem);
    }

    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return this.cartItemRepository.findByCartId(cartId);
    }

    public List<CartItem> getCartItemsByDishId(Long dishId) {
        return this.cartItemRepository.findByDishId(dishId);
    }

    @Transactional
    public CartItem createCartItem(CartItem cartItem) throws IdInvalidException {
        // check cart exists
        if (cartItem.getCart() != null) {
            Cart cart = this.cartService.getCartById(cartItem.getCart().getId());
            if (cart == null) {
                throw new IdInvalidException("Cart not found with id: " + cartItem.getCart().getId());
            }
            cartItem.setCart(cart);
        } else {
            throw new IdInvalidException("Cart is required");
        }

        // check dish exists
        if (cartItem.getDish() != null) {
            Dish dish = this.dishService.getDishById(cartItem.getDish().getId());
            if (dish == null) {
                throw new IdInvalidException("Dish not found with id: " + cartItem.getDish().getId());
            }
            cartItem.setDish(dish);
        } else {
            throw new IdInvalidException("Dish is required");
        }

        // validate quantity
        if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }

        return cartItemRepository.save(cartItem);
    }

    public ResCartItemDTO createCartItemDTO(CartItem cartItem) throws IdInvalidException {
        CartItem savedCartItem = createCartItem(cartItem);
        return convertToResCartItemDTO(savedCartItem);
    }

    @Transactional
    public CartItem updateCartItem(CartItem cartItem) throws IdInvalidException {
        // check id
        CartItem currentCartItem = getCartItemById(cartItem.getId());
        if (currentCartItem == null) {
            throw new IdInvalidException("Cart item not found with id: " + cartItem.getId());
        }

        // update quantity
        if (cartItem.getQuantity() != null) {
            if (cartItem.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            currentCartItem.setQuantity(cartItem.getQuantity());
        }

        return cartItemRepository.save(currentCartItem);
    }

    public ResCartItemDTO updateCartItemDTO(CartItem cartItem) throws IdInvalidException {
        CartItem updatedCartItem = updateCartItem(cartItem);
        return convertToResCartItemDTO(updatedCartItem);
    }

    public ResultPaginationDTO getAllCartItems(Specification<CartItem> spec, Pageable pageable) {
        Page<CartItem> page = this.cartItemRepository.findAll(spec, pageable);
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

    public ResultPaginationDTO getAllCartItemsDTO(Specification<CartItem> spec, Pageable pageable) {
        Page<CartItem> page = this.cartItemRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResCartItemDTO)
                .collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public void deleteCartItem(Long id) {
        this.cartItemRepository.deleteById(id);
    }
}
