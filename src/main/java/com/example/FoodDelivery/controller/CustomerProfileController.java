package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.CustomerProfile;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.customerProfile.ResCustomerProfileDTO;
import com.example.FoodDelivery.service.CustomerProfileService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
public class CustomerProfileController {
    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @PostMapping("/customer-profiles")
    @ApiMessage("Create customer profile")
    public ResponseEntity<ResCustomerProfileDTO> createCustomerProfile(@RequestBody CustomerProfile customerProfile)
            throws IdInvalidException {
        CustomerProfile createdProfile = customerProfileService.createCustomerProfile(customerProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerProfileService.convertToDTO(createdProfile));
    }

    @PutMapping("/customer-profiles")
    @ApiMessage("Update customer profile")
    public ResponseEntity<ResCustomerProfileDTO> updateCustomerProfile(@RequestBody CustomerProfile customerProfile)
            throws IdInvalidException {
        CustomerProfile updatedProfile = customerProfileService.updateCustomerProfile(customerProfile);
        return ResponseEntity.ok(customerProfileService.convertToDTO(updatedProfile));
    }

    @GetMapping("/customer-profiles")
    @ApiMessage("Get all customer profiles")
    public ResponseEntity<ResultPaginationDTO> getAllCustomerProfiles(
            @Filter Specification<CustomerProfile> spec, Pageable pageable) {
        ResultPaginationDTO result = customerProfileService.getAllCustomerProfiles(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer-profiles/{id}")
    @ApiMessage("Get customer profile by id")
    public ResponseEntity<ResCustomerProfileDTO> getCustomerProfileById(@PathVariable("id") Long id)
            throws IdInvalidException {
        CustomerProfile profile = customerProfileService.getCustomerProfileById(id);
        if (profile == null) {
            throw new IdInvalidException("Customer profile not found with id: " + id);
        }
        return ResponseEntity.ok(customerProfileService.convertToDTO(profile));
    }

    @GetMapping("/customer-profiles/user/{userId}")
    @ApiMessage("Get customer profile by user id")
    public ResponseEntity<ResCustomerProfileDTO> getCustomerProfileByUserId(@PathVariable("userId") Long userId)
            throws IdInvalidException {
        CustomerProfile profile = customerProfileService.getCustomerProfileByUserId(userId);
        if (profile == null) {
            throw new IdInvalidException("Customer profile not found for user id: " + userId);
        }
        return ResponseEntity.ok(customerProfileService.convertToDTO(profile));
    }

    @DeleteMapping("/customer-profiles/{id}")
    @ApiMessage("Delete customer profile by id")
    public ResponseEntity<Void> deleteCustomerProfile(@PathVariable("id") Long id) throws IdInvalidException {
        CustomerProfile profile = customerProfileService.getCustomerProfileById(id);
        if (profile == null) {
            throw new IdInvalidException("Customer profile not found with id: " + id);
        }
        customerProfileService.deleteCustomerProfile(id);
        return ResponseEntity.ok().body(null);
    }
}
