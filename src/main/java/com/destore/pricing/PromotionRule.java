package com.destore.pricing;

public record PromotionRule(
        String id,
        String description,
        PromotionType type,
        double discountValue,
        int minimumQuantity
) {
}
