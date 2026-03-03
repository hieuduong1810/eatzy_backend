package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Address;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.address.ResAddressDTO;
import com.example.FoodDelivery.service.AddressService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/addresses")
    @ApiMessage("Create address")
    public ResponseEntity<ResAddressDTO> createAddress(@RequestBody Address address) throws IdInvalidException {
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.convertToDTO(createdAddress));
    }

    @PutMapping("/addresses")
    @ApiMessage("Update address")
    public ResponseEntity<ResAddressDTO> updateAddress(@RequestBody Address address) throws IdInvalidException {
        Address updatedAddress = addressService.updateAddress(address);
        return ResponseEntity.ok(addressService.convertToDTO(updatedAddress));
    }

    @GetMapping("/addresses")
    @ApiMessage("Get all addresses")
    public ResponseEntity<ResultPaginationDTO> getAllAddresses(
            @Filter Specification<Address> spec, Pageable pageable) {
        ResultPaginationDTO result = addressService.getAllAddresses(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addresses/{id}")
    @ApiMessage("Get address by id")
    public ResponseEntity<ResAddressDTO> getAddressById(@PathVariable("id") Long id) throws IdInvalidException {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            throw new IdInvalidException("Address not found with id: " + id);
        }
        return ResponseEntity.ok(addressService.convertToDTO(address));
    }

    @GetMapping("/addresses/customer/{customerId}")
    @ApiMessage("Get addresses by customer id")
    public ResponseEntity<List<ResAddressDTO>> getAddressesByCustomerId(@PathVariable("customerId") Long customerId) {
        List<Address> addresses = addressService.getAddressesByCustomerId(customerId);
        List<ResAddressDTO> dtoList = addresses.stream()
                .map(addressService::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/addresses/{id}")
    @ApiMessage("Delete address by id")
    public ResponseEntity<Void> deleteAddress(@PathVariable("id") Long id) throws IdInvalidException {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            throw new IdInvalidException("Address not found with id: " + id);
        }
        addressService.deleteAddress(id);
        return ResponseEntity.ok().body(null);
    }

    // Endpoints for current logged-in user

    @GetMapping("/addresses/me")
    @ApiMessage("Get current user's addresses")
    public ResponseEntity<List<ResAddressDTO>> getCurrentUserAddresses() throws IdInvalidException {
        List<ResAddressDTO> addresses = addressService.getCurrentUserAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/addresses/me/{id}")
    @ApiMessage("Get current user's address by id")
    public ResponseEntity<ResAddressDTO> getCurrentUserAddressById(@PathVariable("id") Long id)
            throws IdInvalidException {
        ResAddressDTO address = addressService.getCurrentUserAddressById(id);
        return ResponseEntity.ok(address);
    }

    @PostMapping("/addresses/me")
    @ApiMessage("Create address for current user")
    public ResponseEntity<ResAddressDTO> createCurrentUserAddress(@RequestBody Address address)
            throws IdInvalidException {
        Address createdAddress = addressService.createCurrentUserAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.convertToDTO(createdAddress));
    }

    @PutMapping("/addresses/me/{id}")
    @ApiMessage("Update current user's address")
    public ResponseEntity<ResAddressDTO> updateCurrentUserAddress(@PathVariable("id") Long id,
            @RequestBody Address address) throws IdInvalidException {
        Address updatedAddress = addressService.updateCurrentUserAddress(id, address);
        return ResponseEntity.ok(addressService.convertToDTO(updatedAddress));
    }

    @DeleteMapping("/addresses/me/{id}")
    @ApiMessage("Delete current user's address")
    public ResponseEntity<Void> deleteCurrentUserAddress(@PathVariable("id") Long id) throws IdInvalidException {
        addressService.deleteCurrentUserAddress(id);
        return ResponseEntity.ok().body(null);
    }
}
