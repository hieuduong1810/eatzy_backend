package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.SystemConfiguration;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.driverProfile.ResDriverProfileDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverProfileService {
    private final DriverProfileRepository driverProfileRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final MapboxService mapboxService;
    private final SystemConfigurationService systemConfigurationService;
    private final WebSocketService webSocketService;
    private final OrderService orderService;

    public DriverProfileService(DriverProfileRepository driverProfileRepository,
            UserService userService,
            OrderRepository orderRepository,
            MapboxService mapboxService,
            SystemConfigurationService systemConfigurationService,
            WebSocketService webSocketService,
            @Lazy OrderService orderService) {
        this.driverProfileRepository = driverProfileRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.mapboxService = mapboxService;
        this.systemConfigurationService = systemConfigurationService;
        this.webSocketService = webSocketService;
        this.orderService = orderService;
    }

    public boolean existsByUserId(Long userId) {
        return driverProfileRepository.existsByUserId(userId);
    }

    private DriverProfile getDriverProfileEntityById(Long id) throws IdInvalidException {
        return this.driverProfileRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Driver profile not found with id: " + id));
    }

    public ResDriverProfileDTO getDriverProfileById(Long id) {
        Optional<DriverProfile> profileOpt = this.driverProfileRepository.findById(id);
        return profileOpt.map(this::convertToResDriverProfileDTO).orElse(null);
    }

    public ResDriverProfileDTO getDriverProfileByUserId(Long userId) {
        Optional<DriverProfile> profileOpt = this.driverProfileRepository.findByUserId(userId);
        return profileOpt.map(this::convertToResDriverProfileDTO).orElse(null);
    }

    /**
     * Get the driver profile of the currently logged-in user
     * 
     * @return DriverProfile of current user
     * @throws IdInvalidException if user is not logged in or doesn't have a driver
     *                            profile
     */
    public DriverProfile getCurrentDriverProfile() throws IdInvalidException {
        // Get current user's email from security context
        String email = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User is not logged in"));

        // Find user by email
        User currentUser = this.userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("User not found with email: " + email);
        }

        // Find driver profile by user
        Optional<DriverProfile> profileOpt = this.driverProfileRepository.findByUserId(currentUser.getId());
        if (profileOpt.isEmpty()) {
            throw new IdInvalidException("No driver profile found for user: " + currentUser.getName());
        }

        return profileOpt.get();
    }

    /**
     * Get the driver profile DTO of the currently logged-in user
     * 
     * @return ResDriverProfileDTO for the driver profile of current user
     * @throws IdInvalidException if user is not logged in or doesn't have a driver
     *                            profile
     */
    public ResDriverProfileDTO getCurrentDriverProfileDTO() throws IdInvalidException {
        DriverProfile profile = getCurrentDriverProfile();
        return convertToResDriverProfileDTO(profile);
    }

    public ResDriverProfileDTO createDriverProfile(DriverProfile driverProfile) throws IdInvalidException {
        // check user exists
        if (driverProfile.getUser() != null) {
            User user = this.userService.getUserById(driverProfile.getUser().getId());
            if (user == null) {
                throw new IdInvalidException("User not found with id: " + driverProfile.getUser().getId());
            }

            // check if profile already exists for this user
            if (this.existsByUserId(user.getId())) {
                throw new IdInvalidException("Driver profile already exists for user id: " + user.getId());
            }

            driverProfile.setUser(user);
        } else {
            throw new IdInvalidException("User is required");
        }

        DriverProfile savedProfile = driverProfileRepository.save(driverProfile);
        return convertToResDriverProfileDTO(savedProfile);
    }

    public ResDriverProfileDTO updateDriverProfile(DriverProfile driverProfile) throws IdInvalidException {
        // check id
        DriverProfile currentProfile = getDriverProfileEntityById(driverProfile.getId());

        // update basic fields
        if (driverProfile.getVehicleDetails() != null) {
            currentProfile.setVehicleDetails(driverProfile.getVehicleDetails());
        }
        if (driverProfile.getStatus() != null) {
            currentProfile.setStatus(driverProfile.getStatus());
        }
        if (driverProfile.getCurrentLatitude() != null) {
            currentProfile.setCurrentLatitude(driverProfile.getCurrentLatitude());
        }
        if (driverProfile.getCurrentLongitude() != null) {
            currentProfile.setCurrentLongitude(driverProfile.getCurrentLongitude());
        }
        if (driverProfile.getCodLimit() != null) {
            currentProfile.setCodLimit(driverProfile.getCodLimit());
        }

        // update document fields - National ID
        if (driverProfile.getNationalIdFront() != null) {
            currentProfile.setNationalIdFront(driverProfile.getNationalIdFront());
        }
        if (driverProfile.getNationalIdBack() != null) {
            currentProfile.setNationalIdBack(driverProfile.getNationalIdBack());
        }
        if (driverProfile.getNationalIdNumber() != null) {
            currentProfile.setNationalIdNumber(driverProfile.getNationalIdNumber());
        }
        if (driverProfile.getNationalIdStatus() != null) {
            currentProfile.setNationalIdStatus(driverProfile.getNationalIdStatus());
        }
        if (driverProfile.getNationalIdRejectionReason() != null) {
            currentProfile.setNationalIdRejectionReason(driverProfile.getNationalIdRejectionReason());
        }

        // update document fields - Profile Photo
        if (driverProfile.getProfilePhoto() != null) {
            currentProfile.setProfilePhoto(driverProfile.getProfilePhoto());
        }
        if (driverProfile.getProfilePhotoStatus() != null) {
            currentProfile.setProfilePhotoStatus(driverProfile.getProfilePhotoStatus());
        }
        if (driverProfile.getProfilePhotoRejectionReason() != null) {
            currentProfile.setProfilePhotoRejectionReason(driverProfile.getProfilePhotoRejectionReason());
        }

        // update document fields - Driver License
        if (driverProfile.getDriverLicenseImage() != null) {
            currentProfile.setDriverLicenseImage(driverProfile.getDriverLicenseImage());
        }
        if (driverProfile.getDriverLicenseNumber() != null) {
            currentProfile.setDriverLicenseNumber(driverProfile.getDriverLicenseNumber());
        }
        if (driverProfile.getDriverLicenseClass() != null) {
            currentProfile.setDriverLicenseClass(driverProfile.getDriverLicenseClass());
        }
        if (driverProfile.getDriverLicenseExpiry() != null) {
            currentProfile.setDriverLicenseExpiry(driverProfile.getDriverLicenseExpiry());
        }
        if (driverProfile.getDriverLicenseStatus() != null) {
            currentProfile.setDriverLicenseStatus(driverProfile.getDriverLicenseStatus());
        }
        if (driverProfile.getDriverLicenseRejectionReason() != null) {
            currentProfile.setDriverLicenseRejectionReason(driverProfile.getDriverLicenseRejectionReason());
        }

        // update bank account & tax info
        if (driverProfile.getBankName() != null) {
            currentProfile.setBankName(driverProfile.getBankName());
        }
        if (driverProfile.getBankBranch() != null) {
            currentProfile.setBankBranch(driverProfile.getBankBranch());
        }
        if (driverProfile.getBankAccountHolder() != null) {
            currentProfile.setBankAccountHolder(driverProfile.getBankAccountHolder());
        }
        if (driverProfile.getBankAccountNumber() != null) {
            currentProfile.setBankAccountNumber(driverProfile.getBankAccountNumber());
        }
        if (driverProfile.getTaxCode() != null) {
            currentProfile.setTaxCode(driverProfile.getTaxCode());
        }
        if (driverProfile.getBankAccountImage() != null) {
            currentProfile.setBankAccountImage(driverProfile.getBankAccountImage());
        }
        if (driverProfile.getBankAccountStatus() != null) {
            currentProfile.setBankAccountStatus(driverProfile.getBankAccountStatus());
        }
        if (driverProfile.getBankAccountRejectionReason() != null) {
            currentProfile.setBankAccountRejectionReason(driverProfile.getBankAccountRejectionReason());
        }

        // update vehicle information
        if (driverProfile.getVehicleType() != null) {
            currentProfile.setVehicleType(driverProfile.getVehicleType());
        }
        if (driverProfile.getVehicleBrand() != null) {
            currentProfile.setVehicleBrand(driverProfile.getVehicleBrand());
        }
        if (driverProfile.getVehicleModel() != null) {
            currentProfile.setVehicleModel(driverProfile.getVehicleModel());
        }
        if (driverProfile.getVehicleLicensePlate() != null) {
            currentProfile.setVehicleLicensePlate(driverProfile.getVehicleLicensePlate());
        }
        if (driverProfile.getVehicleYear() != null) {
            currentProfile.setVehicleYear(driverProfile.getVehicleYear());
        }

        // update document fields - Vehicle Registration
        if (driverProfile.getVehicleRegistrationImage() != null) {
            currentProfile.setVehicleRegistrationImage(driverProfile.getVehicleRegistrationImage());
        }
        if (driverProfile.getVehicleRegistrationStatus() != null) {
            currentProfile.setVehicleRegistrationStatus(driverProfile.getVehicleRegistrationStatus());
        }
        if (driverProfile.getVehicleRegistrationRejectionReason() != null) {
            currentProfile.setVehicleRegistrationRejectionReason(driverProfile.getVehicleRegistrationRejectionReason());
        }

        // update document fields - Vehicle Insurance
        if (driverProfile.getVehicleInsuranceImage() != null) {
            currentProfile.setVehicleInsuranceImage(driverProfile.getVehicleInsuranceImage());
        }
        if (driverProfile.getVehicleInsuranceExpiry() != null) {
            currentProfile.setVehicleInsuranceExpiry(driverProfile.getVehicleInsuranceExpiry());
        }
        if (driverProfile.getVehicleInsuranceStatus() != null) {
            currentProfile.setVehicleInsuranceStatus(driverProfile.getVehicleInsuranceStatus());
        }
        if (driverProfile.getVehicleInsuranceRejectionReason() != null) {
            currentProfile.setVehicleInsuranceRejectionReason(driverProfile.getVehicleInsuranceRejectionReason());
        }

        // update document fields - Vehicle Photo
        if (driverProfile.getVehiclePhoto() != null) {
            currentProfile.setVehiclePhoto(driverProfile.getVehiclePhoto());
        }
        if (driverProfile.getVehiclePhotoStatus() != null) {
            currentProfile.setVehiclePhotoStatus(driverProfile.getVehiclePhotoStatus());
        }
        if (driverProfile.getVehiclePhotoRejectionReason() != null) {
            currentProfile.setVehiclePhotoRejectionReason(driverProfile.getVehiclePhotoRejectionReason());
        }

        // update document fields - Criminal Record
        if (driverProfile.getCriminalRecordImage() != null) {
            currentProfile.setCriminalRecordImage(driverProfile.getCriminalRecordImage());
        }
        if (driverProfile.getCriminalRecordNumber() != null) {
            currentProfile.setCriminalRecordNumber(driverProfile.getCriminalRecordNumber());
        }
        if (driverProfile.getCriminalRecordIssueDate() != null) {
            currentProfile.setCriminalRecordIssueDate(driverProfile.getCriminalRecordIssueDate());
        }
        if (driverProfile.getCriminalRecordStatus() != null) {
            currentProfile.setCriminalRecordStatus(driverProfile.getCriminalRecordStatus());
        }
        if (driverProfile.getCriminalRecordRejectionReason() != null) {
            currentProfile.setCriminalRecordRejectionReason(driverProfile.getCriminalRecordRejectionReason());
        }

        DriverProfile savedProfile = driverProfileRepository.save(currentProfile);
        return convertToResDriverProfileDTO(savedProfile);
    }

    /**
     * Update driver profile status by user ID
     */
    public void updateDriverProfileStatusByUserId(Long userId, String status) throws IdInvalidException {
        Optional<DriverProfile> profileOpt = driverProfileRepository.findByUserId(userId);
        if (profileOpt.isPresent()) {
            DriverProfile profile = profileOpt.get();
            profile.setStatus(status);
            driverProfileRepository.save(profile);
        } else {
            throw new IdInvalidException("Driver profile not found for user id: " + userId);
        }
    }

    /**
     * Driver goes online (opens app) - set status to AVAILABLE
     * Also automatically finds and assigns the oldest waiting PREPARING order if
     * available
     */
    @Transactional
    public ResDriverProfileDTO goOnline() throws IdInvalidException {
        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        Optional<DriverProfile> profileOpt = driverProfileRepository.findByUserId(driver.getId());
        if (!profileOpt.isPresent()) {
            throw new IdInvalidException("Driver profile not found for user id: " + driver.getId());
        }

        DriverProfile profile = profileOpt.get();

        // Validate driver location is set
        if (profile.getCurrentLatitude() == null || profile.getCurrentLongitude() == null) {
            throw new IdInvalidException("Driver location must be set before going online");
        }

        // Set driver status to AVAILABLE
        profile.setStatus("AVAILABLE");
        DriverProfile savedProfile = driverProfileRepository.save(profile);

        log.info("🟢 Driver {} (ID: {}) is now ONLINE", driver.getName(), driver.getId());

        // Try to find and assign a suitable order
        findAndAssignNextOrderForDriver(driver, savedProfile);

        return convertToResDriverProfileDTO(savedProfile);
    }

    /**
     * Find and assign the next suitable order for a driver.
     * Looks for PREPARING orders without driver, validates business rules, and
     * assigns if found.
     * This method is reusable - called from goOnline() and after delivery
     * completion.
     * 
     * @param driver        The driver user
     * @param driverProfile The driver's profile
     * @return true if an order was assigned, false otherwise
     */
    @Transactional
    public boolean findAndAssignNextOrderForDriver(User driver, DriverProfile driverProfile) {
        try {
            // STEP 1: Find all PREPARING orders without driver (ordered by oldest first)
            List<Order> preparingOrders = orderRepository
                    .findByOrderStatusAndDriverIsNullOrderByPreparingAtAsc("PREPARING");

            if (preparingOrders.isEmpty()) {
                log.info("No PREPARING orders available for driver {}", driver.getId());
                return false;
            }

            log.info("📋 Found {} PREPARING orders without driver", preparingOrders.size());

            // Get search radius from system configuration (default 10 km if not set)
            BigDecimal radiusKm = new BigDecimal("10.0");
            try {
                SystemConfiguration radiusConfig = systemConfigurationService
                        .getSystemConfigurationByKey("DRIVER_SEARCH_RADIUS_KM");
                if (radiusConfig != null && radiusConfig.getConfigValue() != null
                        && !radiusConfig.getConfigValue().isEmpty()) {
                    radiusKm = new BigDecimal(radiusConfig.getConfigValue());
                }
            } catch (Exception e) {
                log.warn("Failed to get DRIVER_SEARCH_RADIUS_KM config, using default 10 km", e);
            }

            // STEP 2: Validate each order against business rules and find the first
            // suitable one
            for (Order order : preparingOrders) {
                try {
                    Restaurant restaurant = order.getRestaurant();
                    if (restaurant == null || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
                        log.warn("Order {} has invalid restaurant location, skipping", order.getId());
                        continue;
                    }

                    // STEP 2.1: Validate business rules
                    // If COD payment, check driver's COD limit
                    if ("COD".equals(order.getPaymentMethod())) {
                        if (driverProfile.getCodLimit() == null ||
                                driverProfile.getCodLimit().compareTo(order.getTotalAmount()) < 0) {
                            log.info("❌ Order {} (COD: {}) exceeds driver's COD limit ({}), skipping",
                                    order.getId(), order.getTotalAmount(), driverProfile.getCodLimit());
                            continue;
                        }
                    }

                    // STEP 2.2: Calculate real driving distance using Mapbox API
                    BigDecimal distance = mapboxService.getDrivingDistance(
                            driverProfile.getCurrentLatitude(),
                            driverProfile.getCurrentLongitude(),
                            restaurant.getLatitude(),
                            restaurant.getLongitude());

                    if (distance == null) {
                        log.warn("Failed to calculate distance for order {}, skipping", order.getId());
                        continue;
                    }

                    log.info("📍 Order {} - Distance to restaurant: {} km (Max radius: {} km)",
                            order.getId(), distance, radiusKm);

                    // STEP 2.3: Check if within radius
                    if (distance.compareTo(radiusKm) > 0) {
                        log.info("❌ Order {} is {} km away, exceeds radius {} km, skipping",
                                order.getId(), distance, radiusKm);
                        continue;
                    }

                    // STEP 3: Order is suitable! Assign driver to this order
                    log.info("✅ Order {} passed all validations! Assigning driver {} (waiting time: {} minutes)",
                            order.getId(), driver.getId(),
                            java.time.Duration.between(order.getPreparingAt(), java.time.Instant.now()).toMinutes());

                    order.setDriver(driver);
                    Order updatedOrder = orderRepository.save(order);

                    // Set driver status to UNAVAILABLE after being assigned
                    driverProfile.setStatus("UNAVAILABLE");
                    driverProfileRepository.save(driverProfile);
                    log.info("🔴 Set driver {} status to UNAVAILABLE after order assignment", driver.getId());

                    // Convert to DTO for WebSocket notification
                    ResOrderDTO orderDTO = orderService.convertToResOrderDTO(updatedOrder);

                    // Notify driver about order assignment via WebSocket
                    webSocketService.notifyDriverOrderAssigned(driver.getId(), orderDTO);

                    // Notify customer about driver assignment
                    webSocketService.notifyCustomerOrderUpdate(
                            order.getCustomer().getId(),
                            orderDTO,
                            "A driver has been assigned to your order");

                    log.info("🎯 Successfully auto-assigned order {} to driver {}",
                            order.getId(), driver.getId());

                    return true; // Only assign one order, then stop
                } catch (Exception e) {
                    log.error("Error processing order {} for driver {}: {}",
                            order.getId(), driver.getId(), e.getMessage(), e);
                    // Continue to next order
                }
            }

            log.info("No suitable orders found for driver {}", driver.getId());
            return false;

        } catch (Exception e) {
            log.error("Error finding next order for driver {}: {}", driver.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Driver goes offline (closes app) - set status to OFFLINE
     */
    public ResDriverProfileDTO goOffline() throws IdInvalidException {
        // Get current driver from JWT token
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User driver = this.userService.handleGetUserByUsername(currentUserEmail);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with email: " + currentUserEmail);
        }

        Optional<DriverProfile> profileOpt = driverProfileRepository.findByUserId(driver.getId());
        if (!profileOpt.isPresent()) {
            throw new IdInvalidException("Driver profile not found for user id: " + driver.getId());
        }

        DriverProfile profile = profileOpt.get();
        profile.setStatus("OFFLINE");
        DriverProfile savedProfile = driverProfileRepository.save(profile);
        return convertToResDriverProfileDTO(savedProfile);
    }

    /**
     * Update driver location (latitude, longitude)
     */
    public void updateDriverLocation(Long userId, java.math.BigDecimal latitude, java.math.BigDecimal longitude)
            throws IdInvalidException {
        Optional<DriverProfile> profileOpt = driverProfileRepository.findByUserId(userId);
        if (profileOpt.isPresent()) {
            DriverProfile profile = profileOpt.get();
            profile.setCurrentLatitude(latitude);
            profile.setCurrentLongitude(longitude);
            driverProfileRepository.save(profile);
        } else {
            throw new IdInvalidException("Driver profile not found for user id: " + userId);
        }
    }

    public ResultPaginationDTO getAllDriverProfiles(Specification<DriverProfile> spec, Pageable pageable) {
        Page<DriverProfile> page = this.driverProfileRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);

        // Convert to DTO
        result.setResult(page.getContent().stream()
                .map(this::convertToResDriverProfileDTO)
                .collect(java.util.stream.Collectors.toList()));

        return result;
    }

    public void deleteDriverProfile(Long id) {
        this.driverProfileRepository.deleteById(id);
    }

    /**
     * Get first available driver with COD limit >= order amount
     * Filters by status (ONLINE/AVAILABLE) and returns driver with highest rating
     * 
     * @param orderAmount Order total amount
     * @return Driver profile with highest rating or null if none found
     */
    // public ResDriverProfileDTO
    // getFirstAvailableDriverByCodLimit(java.math.BigDecimal orderAmount) {
    // java.util.Optional<DriverProfile> driverOpt = driverProfileRepository
    // .findFirstAvailableDriverByCodLimit(orderAmount);
    // return driverOpt.map(this::convertToResDriverProfileDTO).orElse(null);
    // }

    /**
     * Convert DriverProfile to ResDriverProfileDTO
     */
    public ResDriverProfileDTO convertToResDriverProfileDTO(DriverProfile profile) {
        ResDriverProfileDTO dto = new ResDriverProfileDTO();

        // Basic fields
        dto.setId(profile.getId());
        dto.setVehicleDetails(profile.getVehicleDetails());
        dto.setStatus(profile.getStatus());
        dto.setCurrentLatitude(profile.getCurrentLatitude());
        dto.setCurrentLongitude(profile.getCurrentLongitude());
        dto.setAverageRating(profile.getAverageRating());
        dto.setCodLimit(profile.getCodLimit());
        dto.setCompletedTrips(profile.getCompletedTrips());

        // User info (only id, name, email, phoneNumber, isActive)
        if (profile.getUser() != null) {
            ResDriverProfileDTO.UserDriver userDriver = new ResDriverProfileDTO.UserDriver();
            userDriver.setId(profile.getUser().getId());
            userDriver.setName(profile.getUser().getName());
            userDriver.setEmail(profile.getUser().getEmail());
            userDriver.setPhoneNumber(profile.getUser().getPhoneNumber());
            userDriver.setIsActive(profile.getUser().getIsActive());
            dto.setUser(userDriver);
        }

        // Document fields - National ID
        dto.setNationalIdFront(profile.getNationalIdFront());
        dto.setNationalIdBack(profile.getNationalIdBack());
        dto.setNationalIdNumber(profile.getNationalIdNumber());
        dto.setNationalIdStatus(profile.getNationalIdStatus());
        dto.setNationalIdRejectionReason(profile.getNationalIdRejectionReason());

        // Document fields - Profile Photo
        dto.setProfilePhoto(profile.getProfilePhoto());
        dto.setProfilePhotoStatus(profile.getProfilePhotoStatus());
        dto.setProfilePhotoRejectionReason(profile.getProfilePhotoRejectionReason());

        // Document fields - Driver License
        dto.setDriverLicenseImage(profile.getDriverLicenseImage());
        dto.setDriverLicenseNumber(profile.getDriverLicenseNumber());
        dto.setDriverLicenseClass(profile.getDriverLicenseClass());
        dto.setDriverLicenseExpiry(profile.getDriverLicenseExpiry());
        dto.setDriverLicenseStatus(profile.getDriverLicenseStatus());
        dto.setDriverLicenseRejectionReason(profile.getDriverLicenseRejectionReason());

        // Bank Account & Tax Info
        dto.setBankName(profile.getBankName());
        dto.setBankBranch(profile.getBankBranch());
        dto.setBankAccountHolder(profile.getBankAccountHolder());
        dto.setBankAccountNumber(profile.getBankAccountNumber());
        dto.setTaxCode(profile.getTaxCode());
        dto.setBankAccountImage(profile.getBankAccountImage());
        dto.setBankAccountStatus(profile.getBankAccountStatus());
        dto.setBankAccountRejectionReason(profile.getBankAccountRejectionReason());

        // Vehicle Information
        dto.setVehicleType(profile.getVehicleType());
        dto.setVehicleBrand(profile.getVehicleBrand());
        dto.setVehicleModel(profile.getVehicleModel());
        dto.setVehicleLicensePlate(profile.getVehicleLicensePlate());
        dto.setVehicleYear(profile.getVehicleYear());

        // Document fields - Vehicle Registration
        dto.setVehicleRegistrationImage(profile.getVehicleRegistrationImage());
        dto.setVehicleRegistrationStatus(profile.getVehicleRegistrationStatus());
        dto.setVehicleRegistrationRejectionReason(profile.getVehicleRegistrationRejectionReason());

        // Document fields - Vehicle Insurance
        dto.setVehicleInsuranceImage(profile.getVehicleInsuranceImage());
        dto.setVehicleInsuranceExpiry(profile.getVehicleInsuranceExpiry());
        dto.setVehicleInsuranceStatus(profile.getVehicleInsuranceStatus());
        dto.setVehicleInsuranceRejectionReason(profile.getVehicleInsuranceRejectionReason());

        // Document fields - Vehicle Photo
        dto.setVehiclePhoto(profile.getVehiclePhoto());
        dto.setVehiclePhotoStatus(profile.getVehiclePhotoStatus());
        dto.setVehiclePhotoRejectionReason(profile.getVehiclePhotoRejectionReason());

        // Document fields - Criminal Record
        dto.setCriminalRecordImage(profile.getCriminalRecordImage());
        dto.setCriminalRecordNumber(profile.getCriminalRecordNumber());
        dto.setCriminalRecordIssueDate(profile.getCriminalRecordIssueDate());
        dto.setCriminalRecordStatus(profile.getCriminalRecordStatus());
        dto.setCriminalRecordRejectionReason(profile.getCriminalRecordRejectionReason());

        return dto;
    }
}
