package com.destore.inventory;

import jakarta.validation.constraints.NotBlank;

public record InventoryAdjustmentRequest(
        @NotBlank String sku,
        int delta
) {
}
