package com.example.soa.catalog;

import java.time.Instant;
import java.util.UUID;

public record StockReservation(UUID id, UUID productId, int quantity, Instant expiresAt, ReservationStatus status) {
}
