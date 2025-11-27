package com.example.soa.orders;

import com.example.soa.core.DomainException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryOrdersService implements OrdersService {
    private final Map<UUID, Order> orders = new HashMap<>();

    @Override
    public Order createDraft(UUID userId, List<OrderLine> lines, String currency) {
        if (lines.isEmpty()) {
            throw new DomainException("Order must have at least one line");
        }
        UUID orderId = UUID.randomUUID();
        int total = lines.stream().mapToInt(OrderLine::subtotal).sum();
        Order order = new Order(orderId, userId, lines, total, currency, OrderStatus.DRAFT, null, Instant.now());
        orders.put(orderId, order);
        return order;
    }

    @Override
    public Order confirmOrder(UUID orderId, String chargeId) {
        Order order = getOrder(orderId);
        order.confirm(chargeId);
        return order;
    }

    @Override
    public Order markPaymentFailed(UUID orderId) {
        Order order = getOrder(orderId);
        order.markPaymentFailed();
        return order;
    }

    @Override
    public Order getOrder(UUID orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new DomainException("Order not found: " + orderId);
        }
        return order;
    }
}
