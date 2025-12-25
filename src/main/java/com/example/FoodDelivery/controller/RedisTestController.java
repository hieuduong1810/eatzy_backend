package com.example.FoodDelivery.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodDelivery.service.RedisCacheService;
import com.example.FoodDelivery.service.RedisGeoService;
import com.example.FoodDelivery.service.RedisSessionService;
import com.example.FoodDelivery.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/redis")
@Tag(name = "Redis Demo", description = "Demo Redis features: Cache, Session, Pub/Sub")
public class RedisTestController {

        private final RedisCacheService redisCacheService;
        private final RedisSessionService redisSessionService;
        private final RedisGeoService redisGeoService;

        public RedisTestController(
                        RedisCacheService redisCacheService,
                        RedisSessionService redisSessionService,
                        RedisGeoService redisGeoService) {
                this.redisCacheService = redisCacheService;
                this.redisSessionService = redisSessionService;
                this.redisGeoService = redisGeoService;
        }

        // ============ CACHE DEMO ============

        @GetMapping("/cache/{key}")
        @ApiMessage("Get cached value")
        @Operation(summary = "Get value from cache by key")
        public ResponseEntity<?> getCacheValue(@PathVariable String key) {
                Object value = redisCacheService.get(key);
                if (value == null) {
                        return ResponseEntity.ok(Map.of(
                                        "key", key,
                                        "value", "null (not found)",
                                        "cached", false));
                }
                return ResponseEntity.ok(Map.of(
                                "key", key,
                                "value", value,
                                "cached", true));
        }

        @PostMapping("/cache")
        @ApiMessage("Set cache value")
        @Operation(summary = "Set value to cache with TTL")
        public ResponseEntity<?> setCacheValue(@RequestBody Map<String, Object> request) {
                String key = (String) request.get("key");
                Object value = request.get("value");
                Integer ttlMinutes = request.get("ttl") != null ? (Integer) request.get("ttl") : 5;

                redisCacheService.set(key, value, ttlMinutes, java.util.concurrent.TimeUnit.MINUTES);

                return ResponseEntity.ok(Map.of(
                                "message", "Cached successfully",
                                "key", key,
                                "value", value,
                                "ttl", ttlMinutes + " minutes"));
        }

        @DeleteMapping("/cache/{key}")
        @ApiMessage("Delete cache")
        @Operation(summary = "Delete cache by key")
        public ResponseEntity<?> deleteCacheValue(@PathVariable String key) {
                redisCacheService.delete(key);
                return ResponseEntity.ok(Map.of("message", "Cache deleted", "key", key));
        }

        // ============ SESSION DEMO ============

        @PostMapping("/session/{userId}")
        @ApiMessage("Create user session")
        @Operation(summary = "Create a new user session")
        public ResponseEntity<?> createSession(
                        @PathVariable Long userId,
                        @RequestBody Map<String, Object> sessionData) {

                String token = "token_" + System.currentTimeMillis();
                redisSessionService.createSession(userId, token, sessionData);

                return ResponseEntity.ok(Map.of(
                                "message", "Session created",
                                "userId", userId,
                                "token", token,
                                "data", sessionData,
                                "ttl", "2 hours"));
        }

        @GetMapping("/session/{userId}")
        @ApiMessage("Get user session")
        @Operation(summary = "Get user session data")
        public ResponseEntity<?> getSession(@PathVariable Long userId) {
                Map<String, Object> session = redisSessionService.getSession(userId);

                if (session == null) {
                        return ResponseEntity.ok(Map.of(
                                        "message", "Session not found or expired",
                                        "userId", userId));
                }

                return ResponseEntity.ok(Map.of(
                                "message", "Session found",
                                "userId", userId,
                                "session", session));
        }

        @DeleteMapping("/session/{userId}")
        @ApiMessage("Delete user session (logout)")
        @Operation(summary = "Delete user session")
        public ResponseEntity<?> deleteSession(@PathVariable Long userId) {
                redisSessionService.deleteSession(userId);
                return ResponseEntity.ok(Map.of(
                                "message", "Session deleted (user logged out)",
                                "userId", userId));
        }

        // ============ GEO LOCATION DEMO ============

        @PostMapping("/driver/location")
        @ApiMessage("Update driver location")
        @Operation(summary = "Update driver location in Redis GEO")
        public ResponseEntity<?> updateDriverLocation(@RequestBody Map<String, Object> request) {
                Long driverId = Long.valueOf(request.get("driverId").toString());
                java.math.BigDecimal latitude = new java.math.BigDecimal(request.get("latitude").toString());
                java.math.BigDecimal longitude = new java.math.BigDecimal(request.get("longitude").toString());

                redisGeoService.updateDriverLocation(driverId, latitude, longitude);

                return ResponseEntity.ok(Map.of(
                                "message", "Driver location updated",
                                "driverId", driverId,
                                "latitude", latitude,
                                "longitude", longitude,
                                "ttl", "24 hours"));
        }

        @GetMapping("/driver/location/{driverId}")
        @ApiMessage("Get driver location")
        @Operation(summary = "Get driver location from Redis GEO")
        public ResponseEntity<?> getDriverLocation(@PathVariable Long driverId) {
                org.springframework.data.geo.Point location = redisGeoService.getDriverLocation(driverId);

                if (location == null) {
                        return ResponseEntity.ok(Map.of(
                                        "message", "Driver location not found",
                                        "driverId", driverId));
                }

                return ResponseEntity.ok(Map.of(
                                "message", "Driver location found",
                                "driverId", driverId,
                                "longitude", location.getX(),
                                "latitude", location.getY()));
        }

        // ============ INFO ============

        @GetMapping("/info")
        @ApiMessage("Redis feature info")
        @Operation(summary = "Get information about Redis features")
        public ResponseEntity<?> getInfo() {
                Map<String, Object> info = new HashMap<>();

                info.put("cache", Map.of(
                                "description", "Store search results, nearby restaurants",
                                "ttl", "5 minutes",
                                "endpoints", Map.of(
                                                "get", "GET /api/v1/redis/cache/{key}",
                                                "set", "POST /api/v1/redis/cache",
                                                "delete", "DELETE /api/v1/redis/cache/{key}")));

                info.put("session", Map.of(
                                "description", "User session storage",
                                "ttl", "2 hours",
                                "endpoints", Map.of(
                                                "create", "POST /api/v1/redis/session/{userId}",
                                                "get", "GET /api/v1/redis/session/{userId}",
                                                "delete", "DELETE /api/v1/redis/session/{userId}")));

                info.put("pubsub", Map.of(
                                "description", "Real-time event broadcasting",
                                "channels", Map.of(
                                                "orderStatus", "order-status-updates",
                                                "driverLocation", "driver-location-updates"),
                                "endpoints", Map.of(
                                                "publishOrderStatus", "POST /api/v1/redis/publish/order-status",
                                                "publishDriverLocation", "POST /api/v1/redis/publish/driver-location",
                                                "publishCustom", "POST /api/v1/redis/publish/custom?channel={name}")));

                info.put("geo", Map.of(
                                "description", "Driver location tracking with geospatial queries",
                                "ttl", "24 hours",
                                "endpoints", Map.of(
                                                "updateLocation", "POST /api/v1/redis/driver/location",
                                                "getLocation", "GET /api/v1/redis/driver/location/{driverId}",
                                                "findNearby",
                                                "GET /api/v1/redis/drivers/nearby?latitude={lat}&longitude={lng}&radiusKm={radius}")));

                return ResponseEntity.ok(info);
        }
}
