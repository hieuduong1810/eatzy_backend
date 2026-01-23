package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Favorite;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.favourite.ResFavouriteDTO;
import com.example.FoodDelivery.repository.FavoriteRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class FavoriteService {
    private final FavoriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;

    public FavoriteService(FavoriteRepository favouriteRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            UserService userService) {
        this.favouriteRepository = favouriteRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
    }

    private ResFavouriteDTO convertToDTO(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        ResFavouriteDTO dto = new ResFavouriteDTO();
        dto.setId(favorite.getId());

        if (favorite.getCustomer() != null) {
            ResFavouriteDTO.User customerDTO = new ResFavouriteDTO.User();
            customerDTO.setId(favorite.getCustomer().getId());
            customerDTO.setName(favorite.getCustomer().getName());
            dto.setCustomer(customerDTO);
        }

        if (favorite.getRestaurant() != null) {
            ResFavouriteDTO.Restaurant restaurantDTO = new ResFavouriteDTO.Restaurant();
            restaurantDTO.setId(favorite.getRestaurant().getId());
            restaurantDTO.setName(favorite.getRestaurant().getName());
            restaurantDTO.setAddress(favorite.getRestaurant().getAddress());
            restaurantDTO.setDescription(favorite.getRestaurant().getDescription());
            restaurantDTO.setAverageRating(favorite.getRestaurant().getAverageRating());
            dto.setRestaurant(restaurantDTO);
        }

        return dto;
    }

    public ResFavouriteDTO getFavouriteById(Long id) {
        Optional<Favorite> favouriteOpt = this.favouriteRepository.findById(id);
        return convertToDTO(favouriteOpt.orElse(null));
    }

    public List<ResFavouriteDTO> getFavouritesByCustomerId(Long customerId) {
        return this.favouriteRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ResFavouriteDTO> getFavouritesByEmail(String email) throws IdInvalidException {
        User customer = this.userService.handleGetUserByUsername(email);
        if (customer == null) {
            throw new IdInvalidException("User not found with email: " + email);
        }
        return this.favouriteRepository.findByCustomerId(customer.getId()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ResFavouriteDTO createFavourite(Favorite favourite) throws IdInvalidException {
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User customer = this.userService.handleGetUserByUsername(currentUserEmail);
        if (customer == null) {
            throw new IdInvalidException("Customer not found with email: " + currentUserEmail);
        }

        favourite.setCustomer(customer);

        // check restaurant exists
        if (favourite.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantRepository.findById(favourite.getRestaurant().getId())
                    .orElse(null);
            if (restaurant == null) {
                throw new IdInvalidException(
                        "Restaurant not found with id: " + favourite.getRestaurant().getId());
            }
            favourite.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        // check if already favorited
        Optional<Favorite> existingFavorite = this.favouriteRepository
                .findByCustomerIdAndRestaurantId(favourite.getCustomer().getId(),
                        favourite.getRestaurant().getId());
        if (existingFavorite.isPresent()) {
            throw new IdInvalidException("This restaurant is already in favorites");
        }

        Favorite savedFavorite = favouriteRepository.save(favourite);
        return convertToDTO(savedFavorite);
    }

    public ResFavouriteDTO updateFavourite(Favorite favourite) throws IdInvalidException {
        // check id
        Optional<Favorite> currentOpt = this.favouriteRepository.findById(favourite.getId());
        if (currentOpt.isEmpty()) {
            throw new IdInvalidException("Customer favorite not found with id: " + favourite.getId());
        }
        Favorite currentFavourite = currentOpt.get();

        if (favourite.getCustomer() != null) {
            User customer = this.userRepository.findById(favourite.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + favourite.getCustomer().getId());
            }
            currentFavourite.setCustomer(customer);
        }

        if (favourite.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantRepository.findById(favourite.getRestaurant().getId())
                    .orElse(null);
            if (restaurant == null) {
                throw new IdInvalidException(
                        "Restaurant not found with id: " + favourite.getRestaurant().getId());
            }
            currentFavourite.setRestaurant(restaurant);
        }

        Favorite updatedFavorite = favouriteRepository.save(currentFavourite);
        return convertToDTO(updatedFavorite);
    }

    public ResultPaginationDTO getAllFavourites(Specification<Favorite> spec, Pageable pageable) {
        Page<Favorite> page = this.favouriteRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        List<ResFavouriteDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        result.setResult(dtoList);
        return result;
    }

    public void deleteFavourite(Long id) {
        this.favouriteRepository.deleteById(id);
    }
}
