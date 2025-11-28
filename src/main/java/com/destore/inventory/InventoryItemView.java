package com.destore.inventory;

public record InventoryItemView(
        String sku,
        int stock,
        int reorderThreshold
) {
}
