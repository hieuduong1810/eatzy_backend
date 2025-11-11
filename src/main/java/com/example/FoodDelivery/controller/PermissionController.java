package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.Permission;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.PermissionService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Create new permission")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission)
            throws IdInvalidException {
        boolean isPermissionExist = this.permissionService.checkPermissionExists(permission);
        if (isPermissionExist) {
            throw new IdInvalidException("Permission already exists: " + permission.getName());
        }
        Permission createdPermission = permissionService.createPermission(permission);
        return ResponseEntity.ok(createdPermission);
    }

    @PutMapping("/permissions")
    @ApiMessage("Update permission")
    public ResponseEntity<Permission> updatePermission(@RequestBody Permission permission)
            throws IdInvalidException {
        Permission updatedPermission = permissionService.updatePermission(permission);
        return ResponseEntity.ok(updatedPermission);
    }

    @GetMapping("/permissions")
    @ApiMessage("Get all permissions")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(
            @Filter Specification<Permission> spec, Pageable pageable) {
        ResultPaginationDTO result = permissionService.getAllPermissions(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete permission by id")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") Long id) throws IdInvalidException {
        Permission permission = permissionService.getPermissionById(id);
        if (permission == null) {
            throw new IdInvalidException("Permission not found with id: " + id);
        }
        permissionService.deletePermission(id);
        return ResponseEntity.ok().body(null);
    }

}
