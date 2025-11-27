package com.example.soa.payments;

import java.util.UUID;

public interface PaymentsService {
    Charge charge(UUID orderId, int amountCents, String currency, String idempotencyKey, String cardToken);
}
