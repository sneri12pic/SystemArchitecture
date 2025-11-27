package com.destore.inventory;

import com.destore.common.events.StockLowEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile({"monolith", "inventory"})
public class InventoryService {

    private final Map<String, InventoryItem> items = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate; // optional, only active when Kafka is configured

    public InventoryService(ApplicationEventPublisher eventPublisher,
                            ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider) {
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        // Seed example stock so the prototype shows alerts immediately.
        items.put("SKU-100", new InventoryItem("SKU-100", 12, 5));
        items.put("SKU-200", new InventoryItem("SKU-200", 3, 4));
    }

    public Collection<InventoryItem> listInventory() {
        return items.values();
    }

    public InventoryItem getItem(String sku) {
        return items.get(sku);
    }

    public InventoryItem adjust(InventoryAdjustmentRequest request) {
        InventoryItem current = items.getOrDefault(request.sku(), new InventoryItem(request.sku(), 0, 3));
        int newStock = current.stock() + request.delta();
        InventoryItem updated = new InventoryItem(current.sku(), newStock, current.reorderThreshold());
        items.put(updated.sku(), updated);
        publishIfLow(updated);
        return updated;
    }

    public void syncFromHq(Map<String, Integer> snapshot) {
        snapshot.forEach((sku, stock) -> {
            InventoryItem current = items.getOrDefault(sku, new InventoryItem(sku, 0, 3));
            InventoryItem updated = new InventoryItem(sku, stock, current.reorderThreshold());
            items.put(sku, updated);
            publishIfLow(updated);
        });
    }

    private void publishIfLow(InventoryItem item) {
        if (item.stock() <= item.reorderThreshold()) {
            StockLowEvent event = new StockLowEvent(item.sku(), item.stock(), item.reorderThreshold());
            eventPublisher.publishEvent(event); // local/in-memory pathway
            if (kafkaTemplate != null) {
                String payload = "Stock low for %s: %d (threshold %d)".formatted(item.sku(), item.stock(), item.reorderThreshold());
                kafkaTemplate.sendDefault(payload);
            }
        }
    }
}
