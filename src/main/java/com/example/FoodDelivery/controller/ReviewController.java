package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.Review;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.review.ResReviewDTO;
import com.example.FoodDelivery.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewService reviewService;
    private final com.example.FoodDelivery.service.RestaurantService restaurantService;
    private final com.example.FoodDelivery.service.DriverProfileService driverProfileService;

    public ReviewController(ReviewService reviewService,
            com.example.FoodDelivery.service.RestaurantService restaurantService,
            com.example.FoodDelivery.service.DriverProfileService driverProfileService) {
        this.reviewService = reviewService;
        this.restaurantService = restaurantService;
        this.driverProfileService = driverProfileService;
    }

    @PostMapping("/reviews")
    @ApiMessage("Create new review")
    public ResponseEntity<ResReviewDTO> createReview(@Valid @RequestBody Review review)
            throws IdInvalidException {
        ResReviewDTO createdReview = reviewService.createReview(review);
        return ResponseEntity.ok(createdReview);
    }

    @PutMapping("/reviews")
    @ApiMessage("Update review")
    public ResponseEntity<ResReviewDTO> updateReview(@RequestBody Review review)
            throws IdInvalidException {
        ResReviewDTO updatedReview = reviewService.updateReview(review);
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/reviews")
    @ApiMessage("Get all reviews")
    public ResponseEntity<ResultPaginationDTO> getAllReviews(
            @Filter Specification<Review> spec, Pageable pageable) {
        ResultPaginationDTO result = reviewService.getAllReviews(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reviews/{id}")
    @ApiMessage("Get review by id")
    public ResponseEntity<ResReviewDTO> getReviewById(@PathVariable("id") Long id) throws IdInvalidException {
        ResReviewDTO review = reviewService.getReviewById(id);
        if (review == null) {
            throw new IdInvalidException("Review not found with id: " + id);
        }
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews/customer/{customerId}")
    @ApiMessage("Get reviews by customer id")
    public ResponseEntity<List<ResReviewDTO>> getReviewsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<ResReviewDTO> reviews = reviewService.getReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/order/{orderId}")
    @ApiMessage("Get reviews by order id")
    public ResponseEntity<List<ResReviewDTO>> getReviewsByOrderId(@PathVariable("orderId") Long orderId) {
        List<ResReviewDTO> reviews = reviewService.getReviewsByOrderId(orderId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/target")
    @ApiMessage("Get reviews by target")
    public ResponseEntity<List<ResReviewDTO>> getReviewsByTarget(
            @RequestParam("reviewTarget") String reviewTarget,
            @RequestParam("targetName") String targetName) {
        List<ResReviewDTO> reviews = reviewService.getReviewsByTarget(reviewTarget, targetName);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/my-restaurant")
    @ApiMessage("Get reviews for current owner's restaurant")
    public ResponseEntity<List<ResReviewDTO>> getReviewsForMyRestaurant() throws IdInvalidException {
        // Get restaurant owned by current logged-in user
        com.example.FoodDelivery.domain.Restaurant restaurant = restaurantService.getCurrentOwnerRestaurant();
        List<ResReviewDTO> reviews = reviewService.getReviewsByTarget("restaurant", restaurant.getName());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/my-driver")
    @ApiMessage("Get reviews for current driver")
    public ResponseEntity<List<ResReviewDTO>> getReviewsForMyDriver() throws IdInvalidException {
        // Get driver profile of current logged-in user
        com.example.FoodDelivery.domain.DriverProfile driverProfile = driverProfileService.getCurrentDriverProfile();
        List<ResReviewDTO> reviews = reviewService.getReviewsByTarget("driver", driverProfile.getUser().getName());
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/reviews/{id}")
    @ApiMessage("Delete review by id")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) throws IdInvalidException {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().body(null);
    }
}
