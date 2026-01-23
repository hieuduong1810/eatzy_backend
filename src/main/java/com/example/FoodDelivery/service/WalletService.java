package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.wallet.resWalletDTO;
import com.example.FoodDelivery.repository.WalletRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserService userService;

    public WalletService(WalletRepository walletRepository, UserService userService) {
        this.walletRepository = walletRepository;
        this.userService = userService;
    }

    private resWalletDTO convertToDTO(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        resWalletDTO dto = new resWalletDTO();
        dto.setId(wallet.getId());
        dto.setBalance(wallet.getBalance());

        if (wallet.getUser() != null) {
            resWalletDTO.User userDTO = new resWalletDTO.User();
            userDTO.setId(wallet.getUser().getId());
            userDTO.setName(wallet.getUser().getName());
            userDTO.setEmail(wallet.getUser().getEmail());
            dto.setUser(userDTO);
        }

        return dto;
    }

    public resWalletDTO getMyWallet() throws IdInvalidException {
        String currentUserEmail = com.example.FoodDelivery.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User user = this.userService.handleGetUserByUsername(currentUserEmail);
        if (user == null) {
            throw new IdInvalidException("User not found with email: " + currentUserEmail);
        }

        Wallet wallet = getWalletByUserId(user.getId());
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found for current user");
        }

        return convertToDTO(wallet);
    }

    public resWalletDTO getWalletDTOByUserId(Long userId) throws IdInvalidException {
        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found for user id: " + userId);
        }
        return convertToDTO(wallet);
    }

    public boolean existsByUserId(Long userId) {
        return walletRepository.existsByUserId(userId);
    }

    public Wallet getWalletById(Long id) {
        Optional<Wallet> walletOpt = this.walletRepository.findById(id);
        return walletOpt.orElse(null);
    }

    public Wallet getWalletByUserId(Long userId) {
        Optional<Wallet> walletOpt = this.walletRepository.findByUserId(userId);
        return walletOpt.orElse(null);
    }

    @Transactional
    public Wallet createWalletForUser(User user) {
        // check if wallet already exists
        if (this.existsByUserId(user.getId())) {
            return this.getWalletByUserId(user.getId());
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet updateWallet(Wallet wallet) throws IdInvalidException {
        // check id
        Wallet currentWallet = getWalletById(wallet.getId());
        if (currentWallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + wallet.getId());
        }

        // update balance
        if (wallet.getBalance() != null) {
            currentWallet.setBalance(wallet.getBalance());
        }

        return walletRepository.save(currentWallet);
    }

    @Transactional
    public Wallet addBalance(Long walletId, BigDecimal amount) throws IdInvalidException {
        Wallet wallet = getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet subtractBalance(Long walletId, BigDecimal amount) throws IdInvalidException {
        Wallet wallet = getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IdInvalidException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        return walletRepository.save(wallet);
    }

    public ResultPaginationDTO getAllWallets(Specification<Wallet> spec, Pageable pageable) {
        Page<Wallet> page = this.walletRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteWallet(Long id) {
        this.walletRepository.deleteById(id);
    }
}
