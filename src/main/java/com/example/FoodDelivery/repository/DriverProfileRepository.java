package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.DriverProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository
                extends JpaRepository<DriverProfile, Long>, JpaSpecificationExecutor<DriverProfile> {
        Optional<DriverProfile> findByUserId(Long userId);

        boolean existsByUserId(Long userId);

        /**
         * Find driver profiles by user IDs with COD limit check
         * Used after Redis GEO search to validate business rules
         */
        @Query("SELECT dp FROM DriverProfile dp " +
                        "INNER JOIN dp.user u " +
                        "INNER JOIN Wallet w ON w.user.id = u.id " +
                        "WHERE u.id IN :userIds " +
                        "AND dp.codLimit >= :amount " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0")
        List<DriverProfile> findByUserIdsWithCodLimit(
                        @Param("userIds") List<Long> userIds,
                        @Param("amount") BigDecimal amount);

        /**
         * Find driver profiles by user IDs (no COD limit check)
         * Used after Redis GEO search for non-COD orders
         */
        @Query("SELECT dp FROM DriverProfile dp " +
                        "INNER JOIN dp.user u " +
                        "INNER JOIN Wallet w ON w.user.id = u.id " +
                        "WHERE u.id IN :userIds " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0")
        List<DriverProfile> findByUserIds(@Param("userIds") List<Long> userIds);

        /**
         * Count drivers by status
         * Used for supply/demand calculation in dynamic pricing
         */
        long countByStatus(String status);
}
