package com.example.soa.payments;

import com.example.soa.core.DomainException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryPaymentsService implements PaymentsService {
    private final Map<String, Charge> idempotentCharges = new HashMap<>();

    @Override
    public Charge charge(UUID orderId, int amountCents, String currency, String idempotencyKey, String cardToken) {
        if (idempotencyKey != null && idempotentCharges.containsKey(idempotencyKey)) {
            Charge existing = idempotentCharges.get(idempotencyKey);
            if (!existing.orderId().equals(orderId)) {
                throw new DomainException("Idempotency key reused for different order");
            }
            return existing;
        }
        ChargeStatus status = cardToken != null && cardToken.startsWith("fail") ? ChargeStatus.DECLINED : ChargeStatus.SUCCEEDED;
        Charge charge = new Charge(UUID.randomUUID().toString(), orderId, amountCents, currency, status);
        if (idempotencyKey != null) {
            idempotentCharges.put(idempotencyKey, charge);
        }
        return charge;
    }
}
