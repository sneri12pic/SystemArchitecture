package com.example.soa.checkout;

import java.util.UUID;

public record CheckoutResponse(UUID orderId, OrderStatus status, String chargeId, int totalCents, String currency) {
}
