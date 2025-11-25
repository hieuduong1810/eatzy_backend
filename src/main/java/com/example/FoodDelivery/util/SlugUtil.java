package com.example.FoodDelivery.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    /**
     * Generate SEO-friendly slug from Vietnamese text
     * Example: "Bún Bò Huế Đặc Biệt" -> "bun-bo-hue-dac-biet"
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Convert Vietnamese characters to Latin
        String slug = removeVietnameseTones(input);
        
        // Convert to lowercase
        slug = slug.toLowerCase(Locale.ENGLISH);
        
        // Replace whitespace with hyphens
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        
        // Remove all non-latin characters except hyphens
        slug = NONLATIN.matcher(slug).replaceAll("");
        
        // Remove hyphens from start and end
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        
        // Replace multiple consecutive hyphens with single hyphen
        slug = slug.replaceAll("-+", "-");
        
        return slug;
    }

    /**
     * Remove Vietnamese tones and convert to Latin characters
     */
    private static String removeVietnameseTones(String text) {
        // Vietnamese character mappings
        text = text.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        text = text.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        text = text.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        text = text.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        text = text.replaceAll("[ìíịỉĩ]", "i");
        text = text.replaceAll("[ÌÍỊỈĨ]", "I");
        text = text.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        text = text.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        text = text.replaceAll("[ùúụủũưừứựửữ]", "u");
        text = text.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        text = text.replaceAll("[ỳýỵỷỹ]", "y");
        text = text.replaceAll("[ỲÝỴỶỸ]", "Y");
        text = text.replaceAll("đ", "d");
        text = text.replaceAll("Đ", "D");
        
        // Normalize remaining characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    /**
     * Generate unique slug by appending number if exists
     * Example: "bun-bo-hue" -> "bun-bo-hue-2"
     */
    public static String generateUniqueSlug(String baseSlug, int counter) {
        if (counter <= 1) {
            return baseSlug;
        }
        return baseSlug + "-" + counter;
    }

    /**
     * Truncate text to specific length for meta tags
     */
    public static String truncateForMeta(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        
        // Try to truncate at last space before maxLength
        int lastSpace = text.lastIndexOf(' ', maxLength);
        if (lastSpace > 0) {
            return text.substring(0, lastSpace) + "...";
        }
        
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Generate meta description from full description
     * Max 160 characters for SEO
     */
    public static String generateMetaDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return truncateForMeta(description, 157); // 157 + "..." = 160
    }

    /**
     * Generate meta title from name
     * Max 70 characters for SEO
     */
    public static String generateMetaTitle(String name, String suffix) {
        if (name == null || name.isEmpty()) {
            return suffix != null ? suffix : "";
        }
        
        String metaTitle = name;
        if (suffix != null && !suffix.isEmpty()) {
            metaTitle = name + " - " + suffix;
        }
        
        return truncateForMeta(metaTitle, 70);
    }
}
