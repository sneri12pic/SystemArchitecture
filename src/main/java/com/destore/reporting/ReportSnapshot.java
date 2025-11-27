package com.destore.reporting;

public record ReportSnapshot(
        int promotionRuleCount,
        int inventoryItems,
        int lowStockItems,
        int defaultLoyaltyOffers
) {
}
