package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.WalletTransaction;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.walletTransaction.resWalletTransactionDTO;
import com.example.FoodDelivery.repository.WalletTransactionRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;
    private final OrderService orderService;

    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository,
            WalletService walletService,
            @Lazy OrderService orderService) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletService = walletService;
        this.orderService = orderService;
    }

    private resWalletTransactionDTO convertToDTO(WalletTransaction transaction) {
        resWalletTransactionDTO dto = new resWalletTransactionDTO();
        dto.setId(transaction.getId());

        // Map Wallet
        if (transaction.getWallet() != null) {
            Wallet wallet = transaction.getWallet();
            resWalletTransactionDTO.Wallet walletDTO = new resWalletTransactionDTO.Wallet();
            walletDTO.setId(wallet.getId());

            // Map User inside Wallet
            if (wallet.getUser() != null) {
                resWalletTransactionDTO.Wallet.User userDTO = new resWalletTransactionDTO.Wallet.User();
                userDTO.setId(wallet.getUser().getId());
                userDTO.setName(wallet.getUser().getName());
                walletDTO.setUser(userDTO);
            }

            dto.setWallet(walletDTO);
        }

        // Map Order
        if (transaction.getOrder() != null) {
            resWalletTransactionDTO.Order orderDTO = new resWalletTransactionDTO.Order();
            orderDTO.setId(transaction.getOrder().getId());
            dto.setOrder(orderDTO);
        }

        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setTransactionDate(transaction.getTransactionDate());
        return dto;
    }

    public resWalletTransactionDTO getWalletTransactionById(Long id) {
        Optional<WalletTransaction> transactionOpt = this.walletTransactionRepository.findById(id);
        return transactionOpt.map(this::convertToDTO).orElse(null);
    }

    public List<resWalletTransactionDTO> getWalletTransactionsByWalletId(Long walletId) {
        return this.walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<resWalletTransactionDTO> getWalletTransactionsByOrderId(Long orderId) {
        return this.walletTransactionRepository.findByOrderId(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<resWalletTransactionDTO> getWalletTransactionsByWalletIdAndType(Long walletId, String transactionType) {
        return this.walletTransactionRepository.findByWalletIdAndTransactionType(walletId, transactionType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public resWalletTransactionDTO createWalletTransaction(WalletTransaction walletTransaction)
            throws IdInvalidException {
        // check wallet exists
        if (walletTransaction.getWallet() != null) {
            Wallet wallet = this.walletService.getWalletById(walletTransaction.getWallet().getId());
            if (wallet == null) {
                throw new IdInvalidException("Wallet not found with id: " + walletTransaction.getWallet().getId());
            }
            walletTransaction.setWallet(wallet);
        } else {
            throw new IdInvalidException("Wallet is required");
        }

        // check order exists (if provided)
        if (walletTransaction.getOrder() != null) {
            Order order = this.orderService.getOrderById(walletTransaction.getOrder().getId());
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + walletTransaction.getOrder().getId());
            }
            walletTransaction.setOrder(order);
        }

        // validate amount
        if (walletTransaction.getAmount() == null || walletTransaction.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new IdInvalidException("Amount is required and must not be zero");
        }

        // validate transaction type
        if (walletTransaction.getTransactionType() == null) {
            throw new IdInvalidException("Transaction type is required");
        }

        // set default values
        if (walletTransaction.getStatus() == null) {
            walletTransaction.setStatus("PENDING");
        }
        walletTransaction.setCreatedAt(Instant.now());

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(walletTransaction);

        // update wallet balance if transaction is successful
        if ("SUCCESS".equals(walletTransaction.getStatus())) {
            updateWalletBalance(walletTransaction);
        }

        return convertToDTO(savedTransaction);
    }

    @Transactional
    public resWalletTransactionDTO updateWalletTransaction(WalletTransaction walletTransaction)
            throws IdInvalidException {
        // check id
        Optional<WalletTransaction> transactionOpt = this.walletTransactionRepository
                .findById(walletTransaction.getId());
        WalletTransaction currentTransaction = transactionOpt.orElse(null);
        if (currentTransaction == null) {
            throw new IdInvalidException("Wallet transaction not found with id: " + walletTransaction.getId());
        }

        String oldStatus = currentTransaction.getStatus();

        // update fields
        if (walletTransaction.getStatus() != null) {
            currentTransaction.setStatus(walletTransaction.getStatus());
        }
        if (walletTransaction.getDescription() != null) {
            currentTransaction.setDescription(walletTransaction.getDescription());
        }

        WalletTransaction updatedTransaction = walletTransactionRepository.save(currentTransaction);

        // update wallet balance if status changed to SUCCESS
        if (!"SUCCESS".equals(oldStatus) && "SUCCESS".equals(walletTransaction.getStatus())) {
            updateWalletBalance(currentTransaction);
        }

        return convertToDTO(updatedTransaction);
    }

    @Transactional
    public resWalletTransactionDTO depositToWallet(Long walletId, BigDecimal amount, String description)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType("DEPOSIT")
                .description(description != null ? description : "Deposit to wallet")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.addBalance(walletId, amount);

        return convertToDTO(savedTransaction);
    }

    @Transactional
    public resWalletTransactionDTO withdrawFromWallet(Long walletId, BigDecimal amount, String description)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IdInvalidException("Insufficient balance");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount.negate()) // negative amount for withdrawal
                .transactionType("WITHDRAWAL")
                .description(description != null ? description : "Withdrawal from wallet")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.subtractBalance(walletId, amount);

        return convertToDTO(savedTransaction);
    }

    @Transactional
    public resWalletTransactionDTO paymentForOrder(Long walletId, Long orderId, BigDecimal amount)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        Order order = this.orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IdInvalidException("Insufficient balance");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .order(order)
                .amount(amount.negate()) // negative amount for payment
                .transactionType("PAYMENT")
                .description("Payment for order #" + orderId)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.subtractBalance(walletId, amount);

        return convertToDTO(savedTransaction);
    }

    @Transactional
    public resWalletTransactionDTO refundForOrder(Long walletId, Long orderId, BigDecimal amount)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        Order order = this.orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .order(order)
                .amount(amount)
                .transactionType("REFUND")
                .description("Refund for order #" + orderId)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.addBalance(walletId, amount);

        return convertToDTO(savedTransaction);
    }

    private void updateWalletBalance(WalletTransaction transaction) throws IdInvalidException {
        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // positive amount - add to balance
            walletService.addBalance(wallet.getId(), amount);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            // negative amount - subtract from balance
            walletService.subtractBalance(wallet.getId(), amount.negate());
        }
    }

    public ResultPaginationDTO getAllWalletTransactions(Specification<WalletTransaction> spec, Pageable pageable) {
        Page<WalletTransaction> page = this.walletTransactionRepository.findAll(spec, pageable);

        // Convert entities to DTOs
        List<resWalletTransactionDTO> dtoList = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(dtoList);
        return result;
    }

    public void deleteWalletTransaction(Long id) {
        this.walletTransactionRepository.deleteById(id);
    }
}
