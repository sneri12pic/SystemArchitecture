package com.destore.inventory;

public record InventoryItem(
        String sku,
        int stock,
        int reorderThreshold
) {
}
