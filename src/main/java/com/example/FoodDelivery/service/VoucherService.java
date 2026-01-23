package com.example.FoodDelivery.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Voucher;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.voucher.resVoucherDTO;
import com.example.FoodDelivery.repository.VoucherRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public VoucherService(VoucherRepository voucherRepository, OrderRepository orderRepository,
            RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.voucherRepository = voucherRepository;
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public resVoucherDTO convertToResVoucherDTO(Voucher voucher) {
        return convertToResVoucherDTO(voucher, null);
    }

    public resVoucherDTO convertToResVoucherDTO(Voucher voucher, Long userId) {
        if (voucher == null) {
            return null;
        }
        resVoucherDTO dto = new resVoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setTotalQuantity(voucher.getTotalQuantity());
        dto.setRemainingQuantity(voucher.getRemainingQuantity());
        dto.setActive(voucher.getActive());

        // Calculate remaining usage for current user
        if (userId != null && voucher.getUsageLimitPerUser() != null) {
            Long usageCount = this.orderRepository.countByCustomerIdAndVoucherId(userId, voucher.getId());
            int remaining = voucher.getUsageLimitPerUser() - usageCount.intValue();
            dto.setRemainingUsage(Math.max(0, remaining));
        } else if (voucher.getUsageLimitPerUser() != null) {
            // If no userId provided, try to get current user
            String email = SecurityUtil.getCurrentUserLogin().orElse(null);
            if (email != null) {
                User user = this.userRepository.findByEmail(email);
                if (user != null) {
                    Long usageCount = this.orderRepository.countByCustomerIdAndVoucherId(user.getId(), voucher.getId());
                    int remaining = voucher.getUsageLimitPerUser() - usageCount.intValue();
                    dto.setRemainingUsage(Math.max(0, remaining));
                } else {
                    dto.setRemainingUsage(voucher.getUsageLimitPerUser());
                }
            } else {
                dto.setRemainingUsage(voucher.getUsageLimitPerUser());
            }
        } else {
            dto.setRemainingUsage(null); // No limit
        }

        if (voucher.getRestaurants() != null && !voucher.getRestaurants().isEmpty()) {
            List<resVoucherDTO.Restaurant> restaurantDTOs = voucher.getRestaurants().stream()
                    .map(r -> {
                        resVoucherDTO.Restaurant restaurantDTO = new resVoucherDTO.Restaurant();
                        restaurantDTO.setId(r.getId());
                        restaurantDTO.setName(r.getName());
                        return restaurantDTO;
                    })
                    .collect(Collectors.toList());
            dto.setRestaurants(restaurantDTOs);
        }

        return dto;
    }

    @Transactional
    public Voucher getVoucherById(Long id) {
        Optional<Voucher> voucherOpt = this.voucherRepository.findById(id);
        if (voucherOpt.isPresent()) {
            return voucherOpt.get();
        }
        return null; // or throw an exception if preferred
    }

    public resVoucherDTO getVoucherByCode(String code) {
        Optional<Voucher> voucherOpt = this.voucherRepository.findByCode(code);
        return convertToResVoucherDTO(voucherOpt.orElse(null));
    }

    public List<resVoucherDTO> getVouchersByRestaurantId(Long restaurantId) {
        List<Voucher> vouchers = this.voucherRepository.findByRestaurantId(restaurantId);
        return vouchers.stream()
                .map(this::convertToResVoucherDTO)
                .collect(Collectors.toList());
    }

    public resVoucherDTO createVoucher(Voucher voucher) throws IdInvalidException {
        // check code exists
        if (this.voucherRepository.existsByCode(voucher.getCode())) {
            throw new IdInvalidException("Voucher code already exists: " + voucher.getCode());
        }

        // check restaurants exist if provided
        if (voucher.getRestaurants() != null && !voucher.getRestaurants().isEmpty()) {
            List<Restaurant> restaurants = new ArrayList<>();
            for (Restaurant r : voucher.getRestaurants()) {
                Restaurant restaurant = this.restaurantRepository.findById(r.getId()).orElse(null);
                if (restaurant == null) {
                    throw new IdInvalidException("Restaurant not found with id: " + r.getId());
                }
                restaurants.add(restaurant);
            }
            voucher.setRestaurants(restaurants);
        }

        // Set remainingQuantity = totalQuantity when creating new voucher
        if (voucher.getTotalQuantity() != null) {
            voucher.setRemainingQuantity(voucher.getTotalQuantity());
        }

        Voucher savedVoucher = voucherRepository.save(voucher);
        return convertToResVoucherDTO(savedVoucher);
    }

    public resVoucherDTO updateVoucher(Voucher voucher) throws IdInvalidException {
        // check id
        Voucher currentVoucher = this.voucherRepository.findById(voucher.getId()).orElse(null);
        if (currentVoucher == null) {
            throw new IdInvalidException("Voucher not found with id: " + voucher.getId());
        }

        if (voucher.getCode() != null && !voucher.getCode().equals(currentVoucher.getCode())) {
            if (this.voucherRepository.existsByCode(voucher.getCode())) {
                throw new IdInvalidException("Voucher code already exists: " + voucher.getCode());
            }
            currentVoucher.setCode(voucher.getCode());
        }
        if (voucher.getDescription() != null) {
            currentVoucher.setDescription(voucher.getDescription());
        }
        if (voucher.getDiscountType() != null) {
            currentVoucher.setDiscountType(voucher.getDiscountType());
        }
        if (voucher.getDiscountValue() != null) {
            currentVoucher.setDiscountValue(voucher.getDiscountValue());
        }
        if (voucher.getMinOrderValue() != null) {
            currentVoucher.setMinOrderValue(voucher.getMinOrderValue());
        }
        if (voucher.getMaxDiscountAmount() != null) {
            currentVoucher.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        }

        if (voucher.getUsageLimitPerUser() != null) {
            currentVoucher.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
        }
        if (voucher.getStartDate() != null) {
            currentVoucher.setStartDate(voucher.getStartDate());
        }
        if (voucher.getEndDate() != null) {
            currentVoucher.setEndDate(voucher.getEndDate());
        }
        if (voucher.getTotalQuantity() != null) {
            Integer quantityChange = voucher.getTotalQuantity();
            if (quantityChange > 0) {
                // Số dương: cộng cho cả totalQuantity và remainingQuantity
                currentVoucher.setTotalQuantity(currentVoucher.getTotalQuantity() + quantityChange);
                currentVoucher.setRemainingQuantity((currentVoucher.getRemainingQuantity() != null ? currentVoucher.getRemainingQuantity() : 0) + quantityChange);
            } else if (quantityChange < 0) {
                // Số âm: kiểm tra remainingQuantity trước khi trừ
                Integer absChange = Math.abs(quantityChange);
                Integer currentRemaining = currentVoucher.getRemainingQuantity() != null ? currentVoucher.getRemainingQuantity() : 0;
                if (currentRemaining >= absChange) {
                    currentVoucher.setTotalQuantity(currentVoucher.getTotalQuantity() + quantityChange);
                    currentVoucher.setRemainingQuantity(currentRemaining + quantityChange);
                } else {
                    throw new IdInvalidException("Cannot reduce quantity. Only " + currentRemaining + " vouchers remaining, but trying to reduce by " + absChange);
                }
            }
        }
        if (voucher.getRestaurants() != null && !voucher.getRestaurants().isEmpty()) {
            List<Restaurant> restaurants = new ArrayList<>();
            for (Restaurant r : voucher.getRestaurants()) {
                Restaurant restaurant = this.restaurantRepository.findById(r.getId()).orElse(null);
                if (restaurant == null) {
                    throw new IdInvalidException("Restaurant not found with id: " + r.getId());
                }
                restaurants.add(restaurant);
            }
            currentVoucher.setRestaurants(restaurants);
        }

        Voucher savedVoucher = voucherRepository.save(currentVoucher);
        return convertToResVoucherDTO(savedVoucher);
    }

    public ResultPaginationDTO getAllVouchers(Specification<Voucher> spec, Pageable pageable) {
        Page<Voucher> page = this.voucherRepository.findAll(spec, pageable);

        List<resVoucherDTO> voucherDTOs = page.getContent().stream()
                .map(this::convertToResVoucherDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(voucherDTOs);
        return result;
    }

    public void deleteVoucher(Long id) {
        this.voucherRepository.deleteById(id);
    }

    @Transactional
    public resVoucherDTO toggleVoucherActive(Long id) throws IdInvalidException {
        Voucher voucher = this.voucherRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Voucher not found with id: " + id));
        
        voucher.setActive(!voucher.getActive());
        Voucher savedVoucher = this.voucherRepository.save(voucher);
        return convertToResVoucherDTO(savedVoucher);
    }

    public List<resVoucherDTO> getAvailableVouchersForOrder(Long orderId) throws IdInvalidException {
        // Get order details
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order not found with id: " + orderId));

        if (order.getRestaurant() == null) {
            throw new IdInvalidException("Order must have a restaurant");
        }
        if (order.getCustomer() == null) {
            throw new IdInvalidException("Order must have a customer");
        }
        if (order.getSubtotal() == null) {
            throw new IdInvalidException("Order must have a subtotal");
        }

        Long restaurantId = order.getRestaurant().getId();
        Long customerId = order.getCustomer().getId();
        BigDecimal orderValue = order.getSubtotal();
        Instant currentTime = Instant.now();

        // Get all potentially available vouchers
        List<Voucher> vouchers = this.voucherRepository.findAvailableVouchersForOrder(
                restaurantId, orderValue, currentTime);

        // Filter vouchers based on active status and usage limit per user
        List<Voucher> availableVouchers = vouchers.stream()
                .filter(voucher -> {
                    // Check if voucher is active
                    if (voucher.getActive() == null || !voucher.getActive()) {
                        return false;
                    }

                    // If no usage limit, voucher is available
                    if (voucher.getUsageLimitPerUser() == null) {
                        return true;
                    }

                    // Count how many times customer has used this voucher
                    Long usageCount = this.orderRepository.countByCustomerIdAndVoucherId(
                            customerId, voucher.getId());

                    // Check if customer hasn't reached the limit
                    return usageCount < voucher.getUsageLimitPerUser();
                })
                .collect(Collectors.toList());

        // Convert to DTOs with user-specific remaining usage
        return availableVouchers.stream()
                .map(voucher -> convertToResVoucherDTO(voucher, customerId))
                .collect(Collectors.toList());
    }
}
