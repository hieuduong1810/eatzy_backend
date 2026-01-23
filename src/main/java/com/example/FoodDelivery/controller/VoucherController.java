package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Voucher;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.user.ResUserDTO;
import com.example.FoodDelivery.domain.res.voucher.resVoucherDTO;
import com.example.FoodDelivery.service.VoucherService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/vouchers")
    @ApiMessage("Create new voucher")
    public ResponseEntity<resVoucherDTO> createVoucher(@Valid @RequestBody Voucher voucher)
            throws IdInvalidException {
        resVoucherDTO createdVoucher = voucherService.createVoucher(voucher);
        return ResponseEntity.ok(createdVoucher);
    }

    @PutMapping("/vouchers")
    @ApiMessage("Update voucher")
    public ResponseEntity<resVoucherDTO> updateVoucher(@RequestBody Voucher voucher)
            throws IdInvalidException {
        resVoucherDTO updatedVoucher = voucherService.updateVoucher(voucher);
        return ResponseEntity.ok(updatedVoucher);
    }

    @GetMapping("/vouchers")
    @ApiMessage("Get all vouchers")
    public ResponseEntity<ResultPaginationDTO> getAllVouchers(
            @Filter Specification<Voucher> spec, Pageable pageable) {
        ResultPaginationDTO result = voucherService.getAllVouchers(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vouchers/{id}")
    @ApiMessage("Get voucher by id")
    public ResponseEntity<resVoucherDTO> getVoucherById(@PathVariable("id") Long id) throws IdInvalidException {
        Voucher voucher = this.voucherService.getVoucherById(id);
        if (voucher == null) {
            throw new IdInvalidException("Voucher with id " + id + " does not exist");
        }
        resVoucherDTO resVoucherDTO = this.voucherService.convertToResVoucherDTO(voucher);
        return ResponseEntity.ok(resVoucherDTO);
    }

    @GetMapping("/vouchers/code/{code}")
    @ApiMessage("Get voucher by code")
    public ResponseEntity<resVoucherDTO> getVoucherByCode(@PathVariable("code") String code) throws IdInvalidException {
        resVoucherDTO voucher = voucherService.getVoucherByCode(code);
        if (voucher == null) {
            throw new IdInvalidException("Voucher not found with code: " + code);
        }
        return ResponseEntity.ok(voucher);
    }

    @GetMapping("/vouchers/restaurant/{restaurantId}")
    @ApiMessage("Get vouchers by restaurant id")
    public ResponseEntity<List<resVoucherDTO>> getVouchersByRestaurantId(
            @PathVariable("restaurantId") Long restaurantId) {
        List<resVoucherDTO> vouchers = voucherService.getVouchersByRestaurantId(restaurantId);
        return ResponseEntity.ok(vouchers);
    }

    @GetMapping("/vouchers/available/order/{orderId}")
    @ApiMessage("Get available vouchers for order")
    public ResponseEntity<List<resVoucherDTO>> getAvailableVouchersForOrder(
            @PathVariable("orderId") Long orderId) throws IdInvalidException {
        List<resVoucherDTO> vouchers = voucherService.getAvailableVouchersForOrder(orderId);
        return ResponseEntity.ok(vouchers);
    }

    @DeleteMapping("/vouchers/{id}")
    @ApiMessage("Delete voucher by id")
    public ResponseEntity<Void> deleteVoucher(@PathVariable("id") Long id) throws IdInvalidException {
        Voucher voucher = voucherService.getVoucherById(id);
        if (voucher == null) {
            throw new IdInvalidException("Voucher not found with id: " + id);
        }
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok().body(null);
    }

    @PutMapping("/vouchers/{id}/toggle")
    @ApiMessage("Toggle voucher active status")
    public ResponseEntity<resVoucherDTO> toggleVoucherActive(@PathVariable("id") Long id) throws IdInvalidException {
        resVoucherDTO voucher = voucherService.toggleVoucherActive(id);
        return ResponseEntity.ok(voucher);
    }
}
