package com.example.soa.catalog;

import com.example.soa.core.DomainException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryCatalogService implements CatalogService {
    private final Map<UUID, Product> products = new HashMap<>();
    private final Map<UUID, Integer> stockOnHand = new HashMap<>();
    private final Map<UUID, StockReservation> reservations = new HashMap<>();

    public void addProduct(Product product, int stock) {
        products.put(product.id(), product);
        stockOnHand.put(product.id(), stock);
    }

    @Override
    public Product getProduct(UUID productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new DomainException("Product not found: " + productId);
        }
        return product;
    }

    @Override
    public StockReservation reserveStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        Product product = getProduct(productId);
        int available = stockOnHand.getOrDefault(productId, 0);
        if (available < quantity) {
            throw new DomainException("Insufficient stock for product: " + product.name());
        }
        stockOnHand.put(productId, available - quantity);
        UUID reservationId = UUID.randomUUID();
        StockReservation reservation = new StockReservation(reservationId, productId, quantity, Instant.now().plus(10, ChronoUnit.MINUTES), ReservationStatus.ACTIVE);
        reservations.put(reservationId, reservation);
        return reservation;
    }

    @Override
    public void releaseReservation(UUID reservationId) {
        StockReservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            return;
        }
        if (reservation.status() == ReservationStatus.RELEASED) {
            return;
        }
        int available = stockOnHand.getOrDefault(reservation.productId(), 0);
        stockOnHand.put(reservation.productId(), available + reservation.quantity());
        reservations.put(reservationId, new StockReservation(reservation.id(), reservation.productId(), reservation.quantity(), reservation.expiresAt(), ReservationStatus.RELEASED));
    }

    public int stockLevel(UUID productId) {
        return stockOnHand.getOrDefault(productId, 0);
    }
}
