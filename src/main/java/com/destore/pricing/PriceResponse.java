package com.destore.pricing;

public record PriceResponse(
        double totalPrice,
        double savings,
        String appliedRuleId
) {
}
