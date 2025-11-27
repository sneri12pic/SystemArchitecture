package com.destore.loyalty;

public record LoyaltyOffer(
        String id,
        String description,
        double discountPercentage
) {
}
