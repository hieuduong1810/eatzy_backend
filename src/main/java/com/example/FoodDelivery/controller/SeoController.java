package com.example.FoodDelivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.req.seo.ReqUpdateSeoDTO;
import com.example.FoodDelivery.service.SeoService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/seo")
public class SeoController {

    private final SeoService seoService;

    public SeoController(SeoService seoService) {
        this.seoService = seoService;
    }

    // ============ RESTAURANT SEO ============

    @GetMapping("/restaurants/slug/{slug}")
    @ApiMessage("Get restaurant by SEO slug")
    public ResponseEntity<Restaurant> getRestaurantBySlug(@PathVariable String slug) throws IdInvalidException {
        Restaurant restaurant = seoService.getRestaurantBySlug(slug);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with slug: " + slug);
        }
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/restaurants/{id}")
    @ApiMessage("Update restaurant SEO metadata")
    public ResponseEntity<Restaurant> updateRestaurantSeo(
            @PathVariable Long id,
            @RequestBody ReqUpdateSeoDTO dto) throws IdInvalidException {
        Restaurant restaurant = seoService.updateRestaurantSeo(
                id,
                dto.getSlug(),
                dto.getMetaTitle(),
                dto.getMetaDescription(),
                dto.getMetaKeywords(),
                dto.getOgImage());
        return ResponseEntity.ok(restaurant);
    }

    // ============ DISH SEO ============

    @GetMapping("/dishes/slug/{slug}")
    @ApiMessage("Get dish by SEO slug")
    public ResponseEntity<Dish> getDishBySlug(@PathVariable String slug) throws IdInvalidException {
        Dish dish = seoService.getDishBySlug(slug);
        if (dish == null) {
            throw new IdInvalidException("Dish not found with slug: " + slug);
        }
        return ResponseEntity.ok(dish);
    }

    @PutMapping("/dishes/{id}")
    @ApiMessage("Update dish SEO metadata")
    public ResponseEntity<Dish> updateDishSeo(
            @PathVariable Long id,
            @RequestBody ReqUpdateSeoDTO dto) throws IdInvalidException {
        Dish dish = seoService.updateDishSeo(
                id,
                dto.getSlug(),
                dto.getMetaTitle(),
                dto.getMetaDescription(),
                dto.getMetaKeywords(),
                dto.getOgImage());
        return ResponseEntity.ok(dish);
    }
}
