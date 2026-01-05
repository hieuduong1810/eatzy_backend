package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.CartItemOption;
import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.req.ReqCartDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemDTO;
import com.example.FoodDelivery.domain.res.cart.ResCartItemOptionDTO;
import com.example.FoodDelivery.repository.CartRepository;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final MenuOptionRepository menuOptionRepository;

    public CartService(CartRepository cartRepository, UserService userService,
            RestaurantService restaurantService, DishService dishService,
            MenuOptionRepository menuOptionRepository) {
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.dishService = dishService;
        this.menuOptionRepository = menuOptionRepository;
    }

    private ResCartDTO convertToResCartDTO(Cart cart) {
        ResCartDTO dto = new ResCartDTO();
        dto.setId(cart.getId());

        // Convert customer
        if (cart.getCustomer() != null) {
            ResCartDTO.Customer customer = new ResCartDTO.Customer();
            customer.setId(cart.getCustomer().getId());
            customer.setName(cart.getCustomer().getName());
            dto.setCustomer(customer);
        }

        // Convert restaurant
        if (cart.getRestaurant() != null) {
            ResCartDTO.Restaurant restaurant = new ResCartDTO.Restaurant();
            restaurant.setId(cart.getRestaurant().getId());
            restaurant.setName(cart.getRestaurant().getName());
            restaurant.setAddress(cart.getRestaurant().getAddress());
            dto.setRestaurant(restaurant);
        }

        // Convert cart items
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            List<ResCartItemDTO> cartItemDtos = cart.getCartItems().stream()
                    .map(cartItem -> {
                        ResCartItemDTO itemDto = new ResCartItemDTO();
                        itemDto.setId(cartItem.getId());
                        itemDto.setQuantity(cartItem.getQuantity());

                        // Convert dish
                        if (cartItem.getDish() != null) {
                            ResCartItemDTO.Dish dish = new ResCartItemDTO.Dish();
                            dish.setId(cartItem.getDish().getId());
                            dish.setName(cartItem.getDish().getName());
                            dish.setPrice(cartItem.getDish().getPrice());
                            dish.setImage(cartItem.getDish().getImageUrl());
                            itemDto.setDish(dish);
                        }

                        // Calculate total price: dish price + all option price adjustments
                        java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
                        if (cartItem.getDish() != null && cartItem.getDish().getPrice() != null) {
                            totalPrice = cartItem.getDish().getPrice();
                        }

                        // Convert cart item options
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
                            itemDto.setCartItemOptions(optionDtos);

                            // Add all option price adjustments to total price
                            for (CartItemOption option : cartItem.getCartItemOptions()) {
                                if (option.getMenuOption() != null
                                        && option.getMenuOption().getPriceAdjustment() != null) {
                                    totalPrice = totalPrice.add(option.getMenuOption().getPriceAdjustment());
                                }
                            }
                        }

                        itemDto.setTotalPrice(totalPrice);

                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setCartItems(cartItemDtos);
        }

        return dto;
    }

    public Cart getCartById(Long id) {
        Optional<Cart> cartOpt = this.cartRepository.findById(id);
        return cartOpt.orElse(null);
    }

    public ResCartDTO getCartDTOById(Long id) {
        Cart cart = getCartById(id);
        if (cart == null) {
            return null;
        }
        return convertToResCartDTO(cart);
    }

    public List<Cart> getCartsByCustomerId(Long customerId) {
        return this.cartRepository.findByCustomerId(customerId);
    }

    public List<ResCartDTO> getCartsDTOByCustomerId(Long customerId) {
        List<Cart> carts = this.cartRepository.findByCustomerIdOrderByIdDesc(customerId);
        return carts.stream()
                .map(this::convertToResCartDTO)
                .collect(Collectors.toList());
    }

    public List<Cart> getCartsByRestaurantId(Long restaurantId) {
        return this.cartRepository.findByRestaurantId(restaurantId);
    }

    public Cart getCartByCustomerIdAndRestaurantId(Long customerId, Long restaurantId) {
        Optional<Cart> cartOpt = this.cartRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId);
        return cartOpt.orElse(null);
    }

    @Transactional
    public Cart createCart(Cart cart) throws IdInvalidException {
        // Validate and set customer
        if (cart.getCustomer() == null || cart.getCustomer().getId() == null) {
            throw new IdInvalidException("Customer is required");
        }
        User customer = this.userService.getUserById(cart.getCustomer().getId());
        if (customer == null) {
            throw new IdInvalidException("Customer not found with id: " + cart.getCustomer().getId());
        }
        cart.setCustomer(customer);

        // Validate and set restaurant
        if (cart.getRestaurant() == null || cart.getRestaurant().getId() == null) {
            throw new IdInvalidException("Restaurant is required");
        }
        Restaurant restaurant = this.restaurantService.getRestaurantById(cart.getRestaurant().getId());
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + cart.getRestaurant().getId());
        }
        cart.setRestaurant(restaurant);

        // Check if cart already exists for this customer and restaurant
        Cart existingCart = getCartByCustomerIdAndRestaurantId(customer.getId(), restaurant.getId());
        if (existingCart != null) {
            return existingCart;
        }

        return cartRepository.save(cart);
    }

    public ResCartDTO createCartDTO(Cart cart) throws IdInvalidException {
        Cart savedCart = createCart(cart);
        return convertToResCartDTO(savedCart);
    }

    @Transactional
    public Cart updateCart(Cart cart) throws IdInvalidException {
        // check id
        Cart currentCart = getCartById(cart.getId());
        if (currentCart == null) {
            throw new IdInvalidException("Cart not found with id: " + cart.getId());
        }

        // update fields if needed
        if (cart.getCustomer() != null) {
            User customer = this.userService.getUserById(cart.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + cart.getCustomer().getId());
            }
            currentCart.setCustomer(customer);
        }

        if (cart.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(cart.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + cart.getRestaurant().getId());
            }
            currentCart.setRestaurant(restaurant);
        }

        return cartRepository.save(currentCart);
    }

    public ResCartDTO updateCartDTO(Cart cart) throws IdInvalidException {
        Cart updatedCart = updateCart(cart);
        return convertToResCartDTO(updatedCart);
    }

    public ResultPaginationDTO getAllCarts(Specification<Cart> spec, Pageable pageable) {
        Page<Cart> page = this.cartRepository.findAll(spec, pageable);
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

    public ResultPaginationDTO getAllCartsDTO(Specification<Cart> spec, Pageable pageable) {
        Page<Cart> page = this.cartRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResCartDTO)
                .collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public void deleteCart(Long id) {
        this.cartRepository.deleteById(id);
    }

    @Transactional
    public ResCartDTO saveOrUpdateCart(ReqCartDTO reqCartDTO) throws IdInvalidException {
        // Validate customer
        if (reqCartDTO.getCustomer() == null || reqCartDTO.getCustomer().getId() == null) {
            throw new IdInvalidException("Customer is required");
        }
        User customer = this.userService.getUserById(reqCartDTO.getCustomer().getId());
        if (customer == null) {
            throw new IdInvalidException("Customer not found with id: " + reqCartDTO.getCustomer().getId());
        }

        // Validate restaurant
        if (reqCartDTO.getRestaurant() == null || reqCartDTO.getRestaurant().getId() == null) {
            throw new IdInvalidException("Restaurant is required");
        }
        Restaurant restaurant = this.restaurantService.getRestaurantById(reqCartDTO.getRestaurant().getId());
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + reqCartDTO.getRestaurant().getId());
        }

        // Check if cart exists
        Cart cart = getCartByCustomerIdAndRestaurantId(customer.getId(), restaurant.getId());

        if (cart == null) {
            // Create new cart
            cart = new Cart();
            cart.setCustomer(customer);
            cart.setRestaurant(restaurant);
            cart.setCartItems(new java.util.ArrayList<>());
        } else {
            // Clear existing cart items for update
            if (cart.getCartItems() == null) {
                cart.setCartItems(new java.util.ArrayList<>());
            } else {
                cart.getCartItems().clear();
            }
        }

        // Add cart items from request
        if (reqCartDTO.getCartItems() != null && !reqCartDTO.getCartItems().isEmpty()) {
            for (ReqCartDTO.CartItem reqCartItem : reqCartDTO.getCartItems()) {
                // Validate dish
                if (reqCartItem.getDish() == null || reqCartItem.getDish().getId() == null) {
                    throw new IdInvalidException("Dish is required for cart item");
                }
                Dish dish = this.dishService.getDishById(reqCartItem.getDish().getId());
                if (dish == null) {
                    throw new IdInvalidException("Dish not found with id: " + reqCartItem.getDish().getId());
                }

                // Create cart item
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setDish(dish);
                cartItem.setQuantity(reqCartItem.getQuantity());
                cartItem.setCartItemOptions(new java.util.ArrayList<>());

                // Add cart item options
                if (reqCartItem.getCartItemOptions() != null && !reqCartItem.getCartItemOptions().isEmpty()) {
                    for (ReqCartDTO.CartItem.CartItemOption reqOption : reqCartItem.getCartItemOptions()) {
                        if (reqOption.getMenuOption() == null || reqOption.getMenuOption().getId() == null) {
                            throw new IdInvalidException("Menu option is required for cart item option");
                        }
                        MenuOption menuOption = this.menuOptionRepository.findById(reqOption.getMenuOption().getId())
                                .orElseThrow(() -> new IdInvalidException(
                                        "Menu option not found with id: " + reqOption.getMenuOption().getId()));

                        CartItemOption cartItemOption = new CartItemOption();
                        cartItemOption.setCartItem(cartItem);
                        cartItemOption.setMenuOption(menuOption);
                        cartItem.getCartItemOptions().add(cartItemOption);
                    }
                }

                cart.getCartItems().add(cartItem);
            }

            // Save cart with items
            Cart savedCart = cartRepository.save(cart);
            return convertToResCartDTO(savedCart);
        } else {
            // If cart items are empty after update, delete the cart
            if (cart.getId() != null) {
                cartRepository.delete(cart);
            }
            return null;
        }
    }
}
