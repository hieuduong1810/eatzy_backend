package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.CartItemOption;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemOptionDTO;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.repository.CartItemOptionRepository;
import com.example.FoodDelivery.repository.CartItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.stream.Collectors;

@Service
public class CartItemOptionService {
    private final CartItemOptionRepository cartItemOptionRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuOptionRepository menuOptionRepository;

    public CartItemOptionService(CartItemOptionRepository cartItemOptionRepository,
            CartItemRepository cartItemRepository,
            MenuOptionRepository menuOptionRepository) {
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuOptionRepository = menuOptionRepository;
    }

    private ResCartItemOptionDTO convertToResCartItemOptionDTO(CartItemOption cartItemOption) {
        ResCartItemOptionDTO dto = new ResCartItemOptionDTO();
        dto.setId(cartItemOption.getId());

        // Convert menu option
        if (cartItemOption.getMenuOption() != null) {
            ResCartItemOptionDTO.MenuOption menuOption = new ResCartItemOptionDTO.MenuOption();
            menuOption.setId(cartItemOption.getMenuOption().getId());
            menuOption.setName(cartItemOption.getMenuOption().getName());
            menuOption.setPriceAdjustment(cartItemOption.getMenuOption().getPriceAdjustment());
            dto.setMenuOption(menuOption);
        }

        return dto;
    }

    public CartItemOption getCartItemOptionById(Long id) {
        Optional<CartItemOption> cartItemOptionOpt = this.cartItemOptionRepository.findById(id);
        return cartItemOptionOpt.orElse(null);
    }

    public ResCartItemOptionDTO getCartItemOptionDTOById(Long id) {
        CartItemOption cartItemOption = getCartItemOptionById(id);
        if (cartItemOption == null) {
            return null;
        }
        return convertToResCartItemOptionDTO(cartItemOption);
    }

    public List<CartItemOption> getCartItemOptionsByCartItemId(Long cartItemId) {
        return this.cartItemOptionRepository.findByCartItemId(cartItemId);
    }

    public CartItemOption createCartItemOption(CartItemOption cartItemOption) throws IdInvalidException {
        // check menu option exists
        if (cartItemOption.getMenuOption() != null) {
            MenuOption menuOption = this.menuOptionRepository.findById(cartItemOption.getMenuOption().getId())
                    .orElse(null);
            if (menuOption == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + cartItemOption.getMenuOption().getId());
            }
            cartItemOption.setMenuOption(menuOption);
        } else {
            throw new IdInvalidException("Menu option is required");
        }

        // check cart item exists
        if (cartItemOption.getCartItem() != null) {
            CartItem cartItem = this.cartItemRepository.findById(cartItemOption.getCartItem().getId())
                    .orElse(null);
            if (cartItem == null) {
                throw new IdInvalidException("Cart item not found with id: " + cartItemOption.getCartItem().getId());
            }
            cartItemOption.setCartItem(cartItem);
        } else {
            throw new IdInvalidException("Cart item is required");
        }

        return cartItemOptionRepository.save(cartItemOption);
    }

    public ResCartItemOptionDTO createCartItemOptionDTO(CartItemOption cartItemOption) throws IdInvalidException {
        CartItemOption savedCartItemOption = createCartItemOption(cartItemOption);
        return convertToResCartItemOptionDTO(savedCartItemOption);
    }

    public CartItemOption updateCartItemOption(CartItemOption cartItemOption) throws IdInvalidException {
        // check id
        CartItemOption currentCartItemOption = getCartItemOptionById(cartItemOption.getId());
        if (currentCartItemOption == null) {
            throw new IdInvalidException("Cart item option not found with id: " + cartItemOption.getId());
        }

        if (cartItemOption.getMenuOption() != null) {
            MenuOption menuOption = this.menuOptionRepository.findById(cartItemOption.getMenuOption().getId())
                    .orElse(null);
            if (menuOption == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + cartItemOption.getMenuOption().getId());
            }
            currentCartItemOption.setMenuOption(menuOption);
        }

        if (cartItemOption.getCartItem() != null) {
            CartItem cartItem = this.cartItemRepository.findById(cartItemOption.getCartItem().getId())
                    .orElse(null);
            if (cartItem == null) {
                throw new IdInvalidException("Cart item not found with id: " + cartItemOption.getCartItem().getId());
            }
            currentCartItemOption.setCartItem(cartItem);
        }

        return cartItemOptionRepository.save(currentCartItemOption);
    }

    public ResCartItemOptionDTO updateCartItemOptionDTO(CartItemOption cartItemOption) throws IdInvalidException {
        CartItemOption updatedCartItemOption = updateCartItemOption(cartItemOption);
        return convertToResCartItemOptionDTO(updatedCartItemOption);
    }

    public ResultPaginationDTO getAllCartItemOptions(Specification<CartItemOption> spec, Pageable pageable) {
        Page<CartItemOption> page = this.cartItemOptionRepository.findAll(spec, pageable);
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

    public ResultPaginationDTO getAllCartItemOptionsDTO(Specification<CartItemOption> spec, Pageable pageable) {
        Page<CartItemOption> page = this.cartItemOptionRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResCartItemOptionDTO)
                .collect(Collectors.toList()));
        return result;
    }

    public void deleteCartItemOption(Long id) {
        this.cartItemOptionRepository.deleteById(id);
    }
}
