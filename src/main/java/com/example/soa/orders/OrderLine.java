package com.example.soa.orders;

import java.util.UUID;

public record OrderLine(UUID productId, int quantity, int unitPriceCents) {
    public int subtotal() {
        return quantity * unitPriceCents;
    }
}
