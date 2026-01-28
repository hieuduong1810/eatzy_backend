package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.CustomerProfile;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.customerProfile.ResCustomerProfileDTO;
import com.example.FoodDelivery.repository.CustomerProfileRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CustomerProfileService {
    private final CustomerProfileRepository customerProfileRepository;
    private final UserService userService;

    public CustomerProfileService(CustomerProfileRepository customerProfileRepository, UserService userService) {
        this.customerProfileRepository = customerProfileRepository;
        this.userService = userService;
    }

    public boolean existsByUserId(Long userId) {
        return customerProfileRepository.existsByUserId(userId);
    }

    public CustomerProfile getCustomerProfileById(Long id) {
        Optional<CustomerProfile> profileOpt = this.customerProfileRepository.findById(id);
        return profileOpt.orElse(null);
    }

    public CustomerProfile getCustomerProfileByUserId(Long userId) {
        Optional<CustomerProfile> profileOpt = this.customerProfileRepository.findByUserId(userId);
        return profileOpt.orElse(null);
    }

    public CustomerProfile createCustomerProfile(CustomerProfile customerProfile) throws IdInvalidException {
        // check user exists
        if (customerProfile.getUser() != null) {
            User user = this.userService.getUserById(customerProfile.getUser().getId());
            if (user == null) {
                throw new IdInvalidException("User not found with id: " + customerProfile.getUser().getId());
            }

            // check if profile already exists for this user
            if (this.existsByUserId(user.getId())) {
                throw new IdInvalidException("Customer profile already exists for user id: " + user.getId());
            }

            customerProfile.setUser(user);
        } else {
            throw new IdInvalidException("User is required");
        }

        return customerProfileRepository.save(customerProfile);
    }

    public CustomerProfile updateCustomerProfile(CustomerProfile customerProfile) throws IdInvalidException {
        // check id
        CustomerProfile currentProfile = getCustomerProfileById(customerProfile.getId());
        if (currentProfile == null) {
            throw new IdInvalidException("Customer profile not found with id: " + customerProfile.getId());
        }

        // update fields
        if (customerProfile.getDateOfBirth() != null) {
            currentProfile.setDateOfBirth(customerProfile.getDateOfBirth());
        }
        if (customerProfile.getHometown() != null) {
            currentProfile.setHometown(customerProfile.getHometown());
        }

        return customerProfileRepository.save(currentProfile);
    }

    public ResultPaginationDTO getAllCustomerProfiles(Specification<CustomerProfile> spec, Pageable pageable) {
        Page<CustomerProfile> page = this.customerProfileRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);

        List<ResCustomerProfileDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        result.setResult(dtoList);
        return result;
    }

    public void deleteCustomerProfile(Long id) {
        this.customerProfileRepository.deleteById(id);
    }

    public ResCustomerProfileDTO convertToDTO(CustomerProfile profile) {
        if (profile == null) {
            return null;
        }

        ResCustomerProfileDTO dto = new ResCustomerProfileDTO();
        dto.setId(profile.getId());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setHometown(profile.getHometown());

        if (profile.getUser() != null) {
            ResCustomerProfileDTO.UserCustomer userDTO = new ResCustomerProfileDTO.UserCustomer();
            userDTO.setId(profile.getUser().getId());
            userDTO.setName(profile.getUser().getName());
            userDTO.setPhoneNumber(profile.getUser().getPhoneNumber());
            userDTO.setIsActive(profile.getUser().getIsActive());
            dto.setUser(userDTO);
        }

        return dto;
    }
}
