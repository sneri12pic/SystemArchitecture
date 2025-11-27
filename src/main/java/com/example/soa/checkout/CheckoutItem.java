package com.example.soa.checkout;

import java.util.UUID;

public record CheckoutItem(UUID productId, int quantity) {
}
