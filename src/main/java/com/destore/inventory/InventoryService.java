package com.destore.inventory;

import com.destore.common.events.StockLowEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
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

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String kafkaTopic;
    private final InventoryRepository inventoryRepository;

    public InventoryService(ApplicationEventPublisher eventPublisher,
                            ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
                            @Value("${destore.kafka.topic:stock-low}") String kafkaTopic,
                            InventoryRepository inventoryRepository) {
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        this.kafkaTopic = kafkaTopic;
        this.inventoryRepository = inventoryRepository;
        seed();
    }

    public Collection<InventoryItemView> listInventory() {
        return inventoryRepository.findAll().stream().map(this::toView).toList();
    }

    public InventoryItemView getItem(String sku) {
        return inventoryRepository.findById(sku).map(this::toView).orElse(null);
    }

    public InventoryItemView adjust(InventoryAdjustmentRequest request) {
        InventoryItemEntity current = inventoryRepository.findById(request.sku())
                .orElse(new InventoryItemEntity(request.sku(), 0, 3));
        current.setStock(current.getStock() + request.delta());
        InventoryItemEntity saved = inventoryRepository.save(current);
        publishIfLow(saved);
        return toView(saved);
    }

    public void syncFromHq(Map<String, Integer> snapshot) {
        snapshot.forEach((sku, stock) -> {
            InventoryItemEntity current = inventoryRepository.findById(sku)
                    .orElse(new InventoryItemEntity(sku, 0, 3));
            current.setStock(stock);
            InventoryItemEntity saved = inventoryRepository.save(current);
            publishIfLow(saved);
        });
    }

    private void publishIfLow(InventoryItemEntity item) {
        if (item.getStock() <= item.getReorderThreshold()) {
            StockLowEvent event = new StockLowEvent(item.getSku(), item.getStock(), item.getReorderThreshold());
            eventPublisher.publishEvent(event); // local/in-memory pathway
            if (kafkaTemplate != null) {
                String payload = "Stock low for %s: %d (threshold %d)".formatted(item.getSku(), item.getStock(), item.getReorderThreshold());
                kafkaTemplate.send(kafkaTopic, payload);
            }
        }
    }

    private InventoryItemView toView(InventoryItemEntity entity) {
        return new InventoryItemView(entity.getSku(), entity.getStock(), entity.getReorderThreshold());
    }

    private void seed() {
        if (inventoryRepository.count() == 0) {
            inventoryRepository.save(new InventoryItemEntity("SKU-100", 12, 5));
            inventoryRepository.save(new InventoryItemEntity("SKU-200", 3, 4));
        }
    }
}
