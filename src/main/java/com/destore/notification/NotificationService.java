package com.destore.notification;

import com.destore.common.events.StockLowEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Profile({"monolith", "inventory", "notifications"})
public class NotificationService {

    private final List<NotificationMessage> messages = new CopyOnWriteArrayList<>();

    @EventListener
    public void onStockLow(StockLowEvent event) {
        String text = "Stock low for %s: %d (threshold %d)".formatted(event.sku(), event.currentStock(), event.threshold());
        messages.add(new NotificationMessage("STOCK_LOW", text, Instant.now()));
    }

    @KafkaListener(topics = "${destore.kafka.topic:stock-low}", groupId = "notifications",
            autoStartup = "${destore.kafka.enabled:false}")
    public void onStockLowKafka(String payload) {
        messages.add(new NotificationMessage("STOCK_LOW_KAFKA", payload, Instant.now()));
    }

    public List<NotificationMessage> listMessages() {
        return messages;
    }
}
