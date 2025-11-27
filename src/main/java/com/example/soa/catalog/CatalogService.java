package com.example.soa.catalog;

import java.util.UUID;

public interface CatalogService {
    Product getProduct(UUID productId);

    StockReservation reserveStock(UUID productId, int quantity);

    void releaseReservation(UUID reservationId);
}
