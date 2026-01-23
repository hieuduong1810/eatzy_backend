package com.example.FoodDelivery.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.res.driverProfile.ResDriverProfileDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.DriverProfileService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.math.BigDecimal;
import java.util.List;

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
public class DriverProfileController {
    private final DriverProfileService driverProfileService;

    public DriverProfileController(DriverProfileService driverProfileService) {
        this.driverProfileService = driverProfileService;
    }

    @PostMapping("/driver-profiles")
    @ApiMessage("Create driver profile")
    public ResponseEntity<ResDriverProfileDTO> createDriverProfile(@RequestBody DriverProfile driverProfile)
            throws IdInvalidException {
        ResDriverProfileDTO createdProfile = driverProfileService.createDriverProfile(driverProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @PutMapping("/driver-profiles")
    @ApiMessage("Update driver profile")
    public ResponseEntity<ResDriverProfileDTO> updateDriverProfile(@RequestBody DriverProfile driverProfile)
            throws IdInvalidException {
        ResDriverProfileDTO updatedProfile = driverProfileService.updateDriverProfile(driverProfile);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/driver-profiles")
    @ApiMessage("Get all driver profiles")
    public ResponseEntity<ResultPaginationDTO> getAllDriverProfiles(
            @Filter Specification<DriverProfile> spec, Pageable pageable) {
        ResultPaginationDTO result = driverProfileService.getAllDriverProfiles(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/driver-profiles/{id}")
    @ApiMessage("Get driver profile by id")
    public ResponseEntity<ResDriverProfileDTO> getDriverProfileById(@PathVariable("id") Long id)
            throws IdInvalidException {
        ResDriverProfileDTO profile = driverProfileService.getDriverProfileById(id);
        if (profile == null) {
            throw new IdInvalidException("Driver profile not found with id: " + id);
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/driver-profiles/user/{userId}")
    @ApiMessage("Get driver profile by user id")
    public ResponseEntity<ResDriverProfileDTO> getDriverProfileByUserId(@PathVariable("userId") Long userId)
            throws IdInvalidException {
        ResDriverProfileDTO profile = driverProfileService.getDriverProfileByUserId(userId);
        if (profile == null) {
            throw new IdInvalidException("Driver profile not found for user id: " + userId);
        }
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/driver-profiles/{id}")
    @ApiMessage("Delete driver profile by id")
    public ResponseEntity<Void> deleteDriverProfile(@PathVariable("id") Long id) throws IdInvalidException {
        ResDriverProfileDTO profile = driverProfileService.getDriverProfileById(id);
        if (profile == null) {
            throw new IdInvalidException("Driver profile not found with id: " + id);
        }
        driverProfileService.deleteDriverProfile(id);
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/driver-profiles/go-online")
    @ApiMessage("Driver goes online (opens app)")
    public ResponseEntity<ResDriverProfileDTO> goOnline() throws IdInvalidException {
        ResDriverProfileDTO profile = driverProfileService.goOnline();
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/driver-profiles/go-offline")
    @ApiMessage("Driver goes offline (closes app)")
    public ResponseEntity<ResDriverProfileDTO> goOffline() throws IdInvalidException {
        ResDriverProfileDTO profile = driverProfileService.goOffline();
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/driver-profiles/my-profile/status")
    @ApiMessage("Get current driver's profile status")
    public ResponseEntity<java.util.Map<String, String>> getMyProfileStatus() throws IdInvalidException {
        com.example.FoodDelivery.domain.DriverProfile profile = driverProfileService.getCurrentDriverProfile();
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("status", profile.getStatus());
        return ResponseEntity.ok(response);
    }

    // @GetMapping("/driver-profiles/available-by-cod-limit")
    // @ApiMessage("Get first available driver by COD limit")
    // public ResponseEntity<ResDriverProfileDTO> getFirstAvailableDriverByCodLimit(
    // @RequestParam(name = "orderAmount") BigDecimal orderAmount) throws
    // IdInvalidException {
    // ResDriverProfileDTO driver =
    // driverProfileService.getFirstAvailableDriverByCodLimit(orderAmount);
    // if (driver == null) {
    // throw new IdInvalidException("No available driver found with COD limit >= " +
    // orderAmount);
    // }
    // return ResponseEntity.ok(driver);
    // }
}
