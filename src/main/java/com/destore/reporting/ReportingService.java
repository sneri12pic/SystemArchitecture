package com.destore.reporting;

import com.destore.inventory.InventoryService;
import com.destore.loyalty.LoyaltyService;
import com.destore.pricing.PricingService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"monolith"})
public class ReportingService {

    private final PricingService pricingService;
    private final InventoryService inventoryService;
    private final LoyaltyService loyaltyService;

    public ReportingService(PricingService pricingService,
                            InventoryService inventoryService,
                            LoyaltyService loyaltyService) {
        this.pricingService = pricingService;
        this.inventoryService = inventoryService;
        this.loyaltyService = loyaltyService;
    }

    public ReportSnapshot snapshot() {
        int lowStock = (int) inventoryService.listInventory().stream()
                .filter(item -> item.stock() <= item.reorderThreshold())
                .count();

        return new ReportSnapshot(
                pricingService.listRules().size(),
                inventoryService.listInventory().size(),
                lowStock,
                loyaltyService.offersFor("default").size()
        );
    }
}
