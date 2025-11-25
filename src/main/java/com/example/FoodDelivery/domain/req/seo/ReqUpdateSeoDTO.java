package com.example.FoodDelivery.domain.req.seo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateSeoDTO {
    private String slug;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogImage;
}
