package com.example.FoodDelivery.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.example.FoodDelivery.domain.Permission;
import com.example.FoodDelivery.domain.Role;
import com.example.FoodDelivery.repository.PermissionRepository;
import com.example.FoodDelivery.repository.RoleRepository;
import com.example.FoodDelivery.service.PermissionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(1) // Run first, before AdminInitializer
@Slf4j
public class PermissionInitializer implements CommandLineRunner {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final PermissionService permissionService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionInitializer(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping,
            PermissionService permissionService,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.permissionService = permissionService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========== Starting Permission Initialization ==========");

        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        List<Permission> permissionsToCreate = new ArrayList<>();
        int totalEndpoints = 0;
        int newPermissions = 0;
        int existingPermissions = 0;

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // Get API paths
            Set<String> patterns = info.getPatternValues();

            // Get HTTP methods
            Set<String> methods = info.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());

            // Get controller class name as module
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String module = controllerName.replace("Controller", "").toUpperCase();

            // Get method name as permission name
            String methodName = handlerMethod.getMethod().getName();
            String permissionName = convertMethodNameToReadable(methodName);

            // Create permission for each combination of path and method
            for (String pattern : patterns) {
                // Skip Spring Boot default endpoints
                if (pattern.startsWith("/error") || pattern.startsWith("/actuator")) {
                    continue;
                }

                for (String method : methods) {
                    totalEndpoints++;

                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    permission.setApiPath(pattern);
                    permission.setMethod(method);
                    permission.setModule(module);

                    // Check if permission already exists
                    boolean exists = permissionService.checkPermissionExists(permission);

                    if (!exists) {
                        permissionsToCreate.add(permission);
                        newPermissions++;
                        log.info("New permission found: {} {} - {} ({})",
                                method, pattern, permissionName, module);
                    } else {
                        existingPermissions++;
                        log.debug("Permission already exists: {} {} - {}", method, pattern, permissionName);
                    }
                }
            }
        }

        // Batch create new permissions
        if (!permissionsToCreate.isEmpty()) {
            for (Permission permission : permissionsToCreate) {
                try {
                    permissionService.createPermission(permission);
                } catch (Exception e) {
                    log.error("Error creating permission: {} {} - {}",
                            permission.getMethod(), permission.getApiPath(), e.getMessage());
                }
            }
        }

        // Always update roles with all permissions (including DRIVER, CUSTOMER,
        // RESTAURANT)
        updateAllRolesPermissions();

        log.info("========== Permission Initialization Complete ==========");
        log.info("Total endpoints scanned: {}", totalEndpoints);
        log.info("New permissions created: {}", newPermissions);
        log.info("Existing permissions: {}", existingPermissions);
        log.info("=======================================================");
    }

    /**
     * Update all roles (ADMIN, DRIVER, CUSTOMER, RESTAURANT) with all permissions
     * Only updates if roles exist - role creation is handled by DataInitializer
     */
    private void updateAllRolesPermissions() {
        List<Permission> allPermissions = permissionRepository.findAll();

        // Update all 4 roles with full permissions
        String[] rolesToUpdate = { "ADMIN", "DRIVER", "CUSTOMER", "RESTAURANT" };
        for (String roleName : rolesToUpdate) {
            Role role = roleRepository.findByName(roleName);
            if (role != null) {
                role.setPermissions(allPermissions);
                roleRepository.save(role);
                log.info("✅ {} role updated with {} permissions", roleName, allPermissions.size());
            } else {
                log.info("{} role not found yet, will be created by DataInitializer", roleName);
            }
        }
    }

    /**
     * Convert camelCase method name to readable format
     * Example: getAllUsers -> Get All Users
     */
    private String convertMethodNameToReadable(String methodName) {
        // Add space before uppercase letters and capitalize first letter
        String result = methodName.replaceAll("([A-Z])", " $1").trim();
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }
}
