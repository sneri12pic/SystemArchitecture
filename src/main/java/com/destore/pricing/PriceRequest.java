package com.destore.pricing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PriceRequest(
        @NotBlank String sku,
        @Min(0) double unitPrice,
        @Min(1) int quantity,
        String promotionRuleId
) {
}
