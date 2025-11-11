package com.example.FoodDelivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    // Additional query methods can be defined here if needed
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    List<Permission> findByIdIn(List<Long> ids);
}
