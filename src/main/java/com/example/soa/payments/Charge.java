package com.example.soa.payments;

import java.util.UUID;

public record Charge(String id, UUID orderId, int amountCents, String currency, ChargeStatus status) {
}
