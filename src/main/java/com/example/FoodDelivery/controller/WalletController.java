package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.wallet.resWalletDTO;
import com.example.FoodDelivery.service.WalletService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PutMapping("/wallets")
    @ApiMessage("Update wallet")
    public ResponseEntity<Wallet> updateWallet(@RequestBody Wallet wallet) throws IdInvalidException {
        Wallet updatedWallet = walletService.updateWallet(wallet);
        return ResponseEntity.ok(updatedWallet);
    }

    @PutMapping("/wallets/{id}/add-balance")
    @ApiMessage("Add balance to wallet")
    public ResponseEntity<Wallet> addBalance(@PathVariable("id") Long id, @RequestBody BigDecimal amount)
            throws IdInvalidException {
        Wallet wallet = walletService.addBalance(id, amount);
        return ResponseEntity.ok(wallet);
    }

    @PutMapping("/wallets/{id}/subtract-balance")
    @ApiMessage("Subtract balance from wallet")
    public ResponseEntity<Wallet> subtractBalance(@PathVariable("id") Long id, @RequestBody BigDecimal amount)
            throws IdInvalidException {
        Wallet wallet = walletService.subtractBalance(id, amount);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/wallets")
    @ApiMessage("Get all wallets")
    public ResponseEntity<ResultPaginationDTO> getAllWallets(
            @Filter Specification<Wallet> spec, Pageable pageable) {
        ResultPaginationDTO result = walletService.getAllWallets(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/wallets/{id}")
    @ApiMessage("Get wallet by id")
    public ResponseEntity<Wallet> getWalletById(@PathVariable("id") Long id) throws IdInvalidException {
        Wallet wallet = walletService.getWalletById(id);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + id);
        }
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/wallets/my-wallet")
    @ApiMessage("Get current user's wallet")
    public ResponseEntity<resWalletDTO> getMyWallet() throws IdInvalidException {
        resWalletDTO wallet = walletService.getMyWallet();
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/wallets/user/{userId}")
    @ApiMessage("Get wallet by user id")
    public ResponseEntity<resWalletDTO> getWalletByUserId(@PathVariable("userId") Long userId)
            throws IdInvalidException {
        resWalletDTO wallet = walletService.getWalletDTOByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
}
