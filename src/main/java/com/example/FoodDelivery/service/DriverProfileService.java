package com.example.FoodDelivery.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.DriverProfile;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.driverProfile.ResDriverProfileDTO;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.DriverProfileRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class DriverProfileService {
    private final DriverProfileRepository driverProfileRepository;
    private final UserService userService;

    public DriverProfileService(DriverProfileRepository driverProfileRepository, UserService userService) {
        this.driverProfileRepository = driverProfileRepository;
        this.userService = userService;
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

        // update document fields - Driver License
        if (driverProfile.getDriverLicenseFront() != null) {
            currentProfile.setDriverLicenseFront(driverProfile.getDriverLicenseFront());
        }
        if (driverProfile.getDriverLicenseBack() != null) {
            currentProfile.setDriverLicenseBack(driverProfile.getDriverLicenseBack());
        }
        if (driverProfile.getDriverLicenseNumber() != null) {
            currentProfile.setDriverLicenseNumber(driverProfile.getDriverLicenseNumber());
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

        // update document fields - Vehicle Registration
        if (driverProfile.getVehicleRegistrationFront() != null) {
            currentProfile.setVehicleRegistrationFront(driverProfile.getVehicleRegistrationFront());
        }
        if (driverProfile.getVehicleRegistrationBack() != null) {
            currentProfile.setVehicleRegistrationBack(driverProfile.getVehicleRegistrationBack());
        }
        if (driverProfile.getVehicleLicensePlate() != null) {
            currentProfile.setVehicleLicensePlate(driverProfile.getVehicleLicensePlate());
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

        // update document fields - Criminal Record
        if (driverProfile.getCriminalRecordImage() != null) {
            currentProfile.setCriminalRecordImage(driverProfile.getCriminalRecordImage());
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

        // User info (only id, name, email)
        if (profile.getUser() != null) {
            ResDriverProfileDTO.UserDriver userDriver = new ResDriverProfileDTO.UserDriver();
            userDriver.setId(profile.getUser().getId());
            userDriver.setName(profile.getUser().getName());
            userDriver.setEmail(profile.getUser().getEmail());
            dto.setUser(userDriver);
        }

        // Document fields - National ID
        dto.setNationalIdFront(profile.getNationalIdFront());
        dto.setNationalIdBack(profile.getNationalIdBack());
        dto.setNationalIdStatus(profile.getNationalIdStatus());
        dto.setNationalIdRejectionReason(profile.getNationalIdRejectionReason());

        // Document fields - Driver License
        dto.setDriverLicenseFront(profile.getDriverLicenseFront());
        dto.setDriverLicenseBack(profile.getDriverLicenseBack());
        dto.setDriverLicenseStatus(profile.getDriverLicenseStatus());
        dto.setDriverLicenseRejectionReason(profile.getDriverLicenseRejectionReason());

        // Document fields - Vehicle Registration
        dto.setVehicleRegistrationFront(profile.getVehicleRegistrationFront());
        dto.setVehicleRegistrationBack(profile.getVehicleRegistrationBack());
        dto.setVehicleRegistrationStatus(profile.getVehicleRegistrationStatus());
        dto.setVehicleRegistrationRejectionReason(profile.getVehicleRegistrationRejectionReason());

        // Document fields - Vehicle Insurance
        dto.setVehicleInsuranceImage(profile.getVehicleInsuranceImage());
        dto.setVehicleInsuranceStatus(profile.getVehicleInsuranceStatus());
        dto.setVehicleInsuranceRejectionReason(profile.getVehicleInsuranceRejectionReason());

        // Document fields - Profile Photo
        dto.setProfilePhoto(profile.getProfilePhoto());
        dto.setProfilePhotoStatus(profile.getProfilePhotoStatus());
        dto.setProfilePhotoRejectionReason(profile.getProfilePhotoRejectionReason());

        return dto;
    }
}
