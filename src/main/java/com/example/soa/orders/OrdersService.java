package com.example.soa.orders;

import java.util.List;
import java.util.UUID;

public interface OrdersService {
    Order createDraft(UUID userId, List<OrderLine> lines, String currency);

    Order confirmOrder(UUID orderId, String chargeId);

    Order markPaymentFailed(UUID orderId);

    Order getOrder(UUID orderId);
}
