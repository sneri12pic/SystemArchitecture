package com.example.soa.orders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID userId;
    private final List<OrderLine> lines;
    private final int totalCents;
    private final String currency;
    private OrderStatus status;
    private String chargeId;
    private final Instant createdAt;

    public Order(UUID id, UUID userId, List<OrderLine> lines, int totalCents, String currency, OrderStatus status, String chargeId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.lines = List.copyOf(lines);
        this.totalCents = totalCents;
        this.currency = currency;
        this.status = status;
        this.chargeId = chargeId;
        this.createdAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public int totalCents() {
        return totalCents;
    }

    public String currency() {
        return currency;
    }

    public OrderStatus status() {
        return status;
    }

    public String chargeId() {
        return chargeId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void confirm(String chargeIdentifier) {
        if (status == OrderStatus.CONFIRMED) {
            return;
        }
        this.status = OrderStatus.CONFIRMED;
        this.chargeId = chargeIdentifier;
    }

    public void markPaymentFailed() {
        this.status = OrderStatus.FAILED_PAYMENT;
    }
}
