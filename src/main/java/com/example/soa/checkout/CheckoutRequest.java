package com.example.soa.checkout;

import java.util.List;
import java.util.UUID;

public record CheckoutRequest(UUID userId, List<CheckoutItem> items, String cardToken, String idempotencyKey) {
}
