package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Review;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.review.ResReviewDTO;
import com.example.FoodDelivery.repository.ReviewRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
            OrderRepository orderRepository, RestaurantRepository restaurantRepository,
            DriverProfileRepository driverProfileRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userService = userService;
    }

    private ResReviewDTO convertToDTO(Review review) {
        if (review == null) {
            return null;
        }
        ResReviewDTO dto = new ResReviewDTO();
        dto.setId(review.getId());
        dto.setReviewTarget(review.getReviewTarget());
        dto.setTargetName(review.getTargetName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReply(review.getReply());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getOrder() != null) {
            ResReviewDTO.Order orderDTO = new ResReviewDTO.Order();
            orderDTO.setId(review.getOrder().getId());
            dto.setOrder(orderDTO);
        }

        if (review.getCustomer() != null) {
            ResReviewDTO.User customerDTO = new ResReviewDTO.User();
            customerDTO.setId(review.getCustomer().getId());
            customerDTO.setName(review.getCustomer().getName());
            dto.setCustomer(customerDTO);
        }

        return dto;
    }

    public ResReviewDTO getReviewById(Long id) {
        Optional<Review> reviewOpt = this.reviewRepository.findById(id);
        return convertToDTO(reviewOpt.orElse(null));
    }

    public List<ResReviewDTO> getReviewsByCustomerId(Long customerId) {
        return this.reviewRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ResReviewDTO> getReviewsByOrderId(Long orderId) {
        return this.reviewRepository.findByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ResReviewDTO> getReviewsByTarget(String reviewTarget, String TargetName) {
        return this.reviewRepository.findByReviewTargetAndTargetName(reviewTarget, TargetName).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ResReviewDTO createReview(Review review) throws IdInvalidException {
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User customer = this.userService.handleGetUserByUsername(currentUserEmail);
        if (customer == null) {
            throw new IdInvalidException("Customer not found with email: " + currentUserEmail);
        }

        review.setCustomer(customer);

        // check order exists
        if (review.getOrder() != null) {
            Order order = this.orderRepository.findById(review.getOrder().getId()).orElse(null);
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + review.getOrder().getId());
            }
            // check if current user is the customer of this order
            if (!order.getCustomer().getId().equals(customer.getId())) {
                throw new IdInvalidException("You are not allowed to review this order");
            }
            // check if this order has already been reviewed by this customer
            List<Review> existingReviews = this.reviewRepository.findByOrderId(order.getId());
            if (!existingReviews.isEmpty()) {
                throw new IdInvalidException("You have already reviewed this order");
            }
            // check order status
            if (!"DELIVERED".equals(order.getOrderStatus())) {
                throw new IdInvalidException("Only delivered orders can be reviewed");
            }
            review.setOrder(order);
        } else {
            throw new IdInvalidException("Order is required");
        }

        // validate review target
        if (review.getReviewTarget() == null || review.getReviewTarget().isEmpty()) {
            throw new IdInvalidException("Review target is required");
        }

        String reviewTarget = review.getReviewTarget().toLowerCase();
        if (!reviewTarget.equals("restaurant") && !reviewTarget.equals("driver")) {
            throw new IdInvalidException("Review target must be either 'restaurant' or 'driver'");
        }

        // validate rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IdInvalidException("Rating must be between 1 and 5");
        }

        review.setCreatedAt(Instant.now());

        // set TargetName based on review target
        if (reviewTarget.equals("restaurant")) {
            review.setTargetName(review.getOrder().getRestaurant().getName());
        } else if (reviewTarget.equals("driver")) {
            review.setTargetName(review.getOrder().getDriver().getName());
        } else {
            throw new IdInvalidException("Order does not have a driver assigned");
        }

        Review savedReview = reviewRepository.save(review);

        // update rating based on review target
        if (reviewTarget.equals("restaurant")) {
            updateRestaurantRating(savedReview.getTargetName());
        } else if (reviewTarget.equals("driver")) {
            updateDriverRating(savedReview.getTargetName());
        }

        return convertToDTO(savedReview);
    }

    private void updateRestaurantRating(String restaurantName) {
        List<Review> reviews = this.reviewRepository.findByReviewTargetAndTargetName("restaurant", restaurantName);
        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            // Count reviews by star rating
            int oneStarCount = (int) reviews.stream().filter(r -> r.getRating() == 1).count();
            int twoStarCount = (int) reviews.stream().filter(r -> r.getRating() == 2).count();
            int threeStarCount = (int) reviews.stream().filter(r -> r.getRating() == 3).count();
            int fourStarCount = (int) reviews.stream().filter(r -> r.getRating() == 4).count();
            int fiveStarCount = (int) reviews.stream().filter(r -> r.getRating() == 5).count();

            Restaurant restaurant = this.restaurantRepository.findByName(restaurantName).orElse(null);
            if (restaurant != null) {
                restaurant.setAverageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
                restaurant.setOneStarCount(oneStarCount);
                restaurant.setTwoStarCount(twoStarCount);
                restaurant.setThreeStarCount(threeStarCount);
                restaurant.setFourStarCount(fourStarCount);
                restaurant.setFiveStarCount(fiveStarCount);
                this.restaurantRepository.save(restaurant);
            }
        }
    }

    private void updateDriverRating(String driverName) {
        List<Review> reviews = this.reviewRepository.findByReviewTargetAndTargetName("driver", driverName);
        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            // Find driver user by name
            User driver = this.userRepository.findByName(driverName).orElse(null);
            if (driver != null) {
                // Find driver profile by user id
                DriverProfile driverProfile = this.driverProfileRepository.findByUserId(driver.getId()).orElse(null);
                if (driverProfile != null) {
                    driverProfile.setAverageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
                    this.driverProfileRepository.save(driverProfile);
                }
            }
        }
    }

    public ResReviewDTO updateReview(Review review) throws IdInvalidException {
        // check id
        Review currentReview = this.reviewRepository.findById(review.getId()).orElse(null);
        if (currentReview == null) {
            throw new IdInvalidException("Review not found with id: " + review.getId());
        }

        boolean ratingChanged = false;

        if (review.getReviewTarget() != null) {
            currentReview.setReviewTarget(review.getReviewTarget());
        }
        if (review.getTargetName() != null) {
            currentReview.setTargetName(review.getTargetName());
        }
        if (review.getRating() != null) {
            if (!review.getRating().equals(currentReview.getRating())) {
                ratingChanged = true;
            }
            if (review.getRating() < 1 || review.getRating() > 5) {
                throw new IdInvalidException("Rating must be between 1 and 5");
            }
            currentReview.setRating(review.getRating());
        }
        if (review.getComment() != null) {
            currentReview.setComment(review.getComment());
        }
        if (review.getReply() != null) {
            currentReview.setReply(review.getReply());
        }
        if (review.getCustomer() != null) {
            User customer = this.userRepository.findById(review.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + review.getCustomer().getId());
            }
            currentReview.setCustomer(customer);
        }
        if (review.getOrder() != null) {
            Order order = this.orderRepository.findById(review.getOrder().getId()).orElse(null);
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + review.getOrder().getId());
            }
            currentReview.setOrder(order);
        }

        Review updatedReview = reviewRepository.save(currentReview);

        // update rating if rating changed
        if (ratingChanged && updatedReview.getReviewTarget() != null && updatedReview.getTargetName() != null) {
            String reviewTarget = updatedReview.getReviewTarget().toLowerCase();
            if (reviewTarget.equals("restaurant")) {
                updateRestaurantRating(updatedReview.getTargetName());
            } else if (reviewTarget.equals("driver")) {
                updateDriverRating(updatedReview.getTargetName());
            }
        }

        return convertToDTO(updatedReview);
    }

    public ResultPaginationDTO getAllReviews(Specification<Review> spec, Pageable pageable) {
        Page<Review> page = this.reviewRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        List<ResReviewDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        result.setResult(dtoList);
        return result;
    }

    public void deleteReview(Long id) throws IdInvalidException {
        Review review = this.reviewRepository.findById(id).orElse(null);
        if (review == null) {
            throw new IdInvalidException("Review not found with id: " + id);
        }

        String reviewTarget = review.getReviewTarget();
        String TargetName = review.getTargetName();

        this.reviewRepository.deleteById(id);

        // update rating after delete
        if (reviewTarget != null && TargetName != null) {
            String target = reviewTarget.toLowerCase();
            if (target.equals("restaurant")) {
                updateRestaurantRating(TargetName);
            } else if (target.equals("driver")) {
                updateDriverRating(TargetName);
            }
        }
    }
}
