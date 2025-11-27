package com.destore.common.events;

public record StockLowEvent(String sku, int currentStock, int threshold) {
}
