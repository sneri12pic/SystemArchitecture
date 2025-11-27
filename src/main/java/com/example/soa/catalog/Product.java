package com.example.soa.catalog;

import java.util.UUID;

public record Product(UUID id, String sku, String name, int priceCents, String currency) {
}
