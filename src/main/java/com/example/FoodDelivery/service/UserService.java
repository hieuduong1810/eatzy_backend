package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.Role;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.user.ResCreateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUpdateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUserDTO;
import com.example.FoodDelivery.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final WalletService walletService;
    private final EmailVerificationService emailVerificationService;
    private final RestaurantRepository restaurantRepository;
    private final DriverProfileRepository driverProfileRepository;

    public UserService(
            UserRepository userRepository,
            RoleService roleService,
            WalletService walletService,
            EmailVerificationService emailVerificationService,
            RestaurantRepository restaurantRepository,
            DriverProfileRepository driverProfileRepository) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.walletService = walletService;
        this.emailVerificationService = emailVerificationService;
        this.restaurantRepository = restaurantRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    @Transactional
    public User handleCreateUser(User user) {

        // Set user as inactive until email verification
        user.setIsActive(false);

        // check role
        if (user.getRole() != null) {
            Role role = this.roleService.getRoleById(user.getRole().getId());
            user.setRole(role != null ? role : null);
        }

        User savedUser = this.userRepository.save(user);

        // create wallet for new user with balance = 0
        this.walletService.createWalletForUser(savedUser);

        // Send verification email with OTP
        try {
            emailVerificationService.sendVerificationEmail(savedUser);
        } catch (Exception e) {
            // Log error but don't fail user creation
            // User can request resend later
            System.err.println("Failed to send verification email to " + savedUser.getEmail() + ": " + e.getMessage());
        }

        return savedUser;
    }

    @Transactional
    public void handleDeleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    @Transactional
    public User getUserById(Long id) {
        Optional<User> userOpt = this.userRepository.findById(id);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return null; // or throw an exception if preferred
    }

    @Transactional
    public User getUserByRoleName(String roleName) {
        List<User> users = this.userRepository.findAll();
        return users.stream()
                .filter(user -> user.getRole() != null && roleName.equals(user.getRole().getName()))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> page = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);

        // remove sensitive data

        List<ResUserDTO> listUser = page.getContent().stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        result.setResult(listUser);
        return result;
    }

    @Transactional
    public User handleUpdateUser(User user) {

        User currentUser = this.getUserById(user.getId());

        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setAddress(user.getAddress());
            currentUser.setAge(user.getAge());
            currentUser.setGender(user.getGender());
            if (user.getRole() != null) {
                Role role = this.roleService.getRoleById(user.getRole().getId());
                currentUser.setRole(role != null ? role : null);
            }
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean checkEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO resUserDTO = new ResCreateUserDTO();
        ResCreateUserDTO.Role role = new ResCreateUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO resUserDTO = new ResUserDTO();
        ResUserDTO.Role role = new ResUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO resUserDTO = new ResUpdateUserDTO();
        ResUpdateUserDTO.Role role = new ResUpdateUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    /**
     * Set user active status
     * When changing from true to false:
     * - If role is RESTAURANT: set restaurant status to CLOSED
     * - If role is DRIVER: set driver profile status to OFFLINE
     * - Clear refresh token to force logout
     */
    @Transactional
    public User setUserActiveStatus(Long userId, Boolean isActive) {
        User user = this.getUserById(userId);
        if (user == null) {
            return null;
        }

        Boolean previousStatus = user.getIsActive();
        user.setIsActive(isActive);

        // If changing to inactive, update related entities and force logout
        if (Boolean.FALSE.equals(isActive)) {
            // Clear refresh token to force logout
            user.setRefreshToken(null);

            // Only update related entities status if changing from active to inactive
            if (Boolean.TRUE.equals(previousStatus)) {
                String roleName = user.getRole() != null ? user.getRole().getName() : null;

                if ("RESTAURANT".equals(roleName)) {
                    // Find and close the restaurant owned by this user
                    Restaurant restaurant = this.restaurantRepository.findByOwnerId(user.getId()).orElse(null);
                    if (restaurant != null) {
                        restaurant.setStatus("CLOSED");
                        this.restaurantRepository.save(restaurant);
                    }
                } else if ("DRIVER".equals(roleName)) {
                    // Find and set driver profile to OFFLINE
                    DriverProfile driverProfile = this.driverProfileRepository.findByUserId(user.getId()).orElse(null);
                    if (driverProfile != null) {
                        driverProfile.setStatus("OFFLINE");
                        this.driverProfileRepository.save(driverProfile);
                    }
                }
            }
        }

        return this.userRepository.save(user);
    }
}
