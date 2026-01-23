package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.Favorite;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.favourite.ResFavouriteDTO;
import com.example.FoodDelivery.service.FavoriteService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FavoriteController {
    private final FavoriteService favouriteService;

    public FavoriteController(FavoriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @PostMapping("/favorites")
    @ApiMessage("Create new customer favorite")
    public ResponseEntity<ResFavouriteDTO> createFavourite(
            @Valid @RequestBody Favorite favourite)
            throws IdInvalidException {
        ResFavouriteDTO createdFavourite = favouriteService.createFavourite(favourite);
        return ResponseEntity.ok(createdFavourite);
    }

    @PutMapping("/favorites")
    @ApiMessage("Update customer favorite")
    public ResponseEntity<ResFavouriteDTO> updateFavourite(@RequestBody Favorite favourite)
            throws IdInvalidException {
        ResFavouriteDTO updatedFavourite = favouriteService.updateFavourite(favourite);
        return ResponseEntity.ok(updatedFavourite);
    }

    @GetMapping("/favorites")
    @ApiMessage("Get all customer favorites")
    public ResponseEntity<ResultPaginationDTO> getAllFavourites(
            @Filter Specification<Favorite> spec, Pageable pageable) {
        ResultPaginationDTO result = favouriteService.getAllFavourites(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/favorites/{id}")
    @ApiMessage("Get customer favorite by id")
    public ResponseEntity<ResFavouriteDTO> getFavouriteById(@PathVariable("id") Long id)
            throws IdInvalidException {
        ResFavouriteDTO favourite = favouriteService.getFavouriteById(id);
        if (favourite == null) {
            throw new IdInvalidException("Customer favorite not found with id: " + id);
        }
        return ResponseEntity.ok(favourite);
    }

    @GetMapping("/favorites/my-favorites")
    @ApiMessage("Get favorites for current user")
    public ResponseEntity<List<ResFavouriteDTO>> getMyFavorites() throws IdInvalidException {
        // Get current logged-in user's email from SecurityUtil
        String email = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not logged in"));

        List<ResFavouriteDTO> favourites = favouriteService.getFavouritesByEmail(email);
        return ResponseEntity.ok(favourites);
    }

    @DeleteMapping("/favorites/{id}")
    @ApiMessage("Delete customer favorite by id")
    public ResponseEntity<Void> deleteFavourite(@PathVariable("id") Long id) throws IdInvalidException {
        ResFavouriteDTO favourite = favouriteService.getFavouriteById(id);
        if (favourite == null) {
            throw new IdInvalidException("Customer favorite not found with id: " + id);
        }
        favouriteService.deleteFavourite(id);
        return ResponseEntity.ok().body(null);
    }
}
