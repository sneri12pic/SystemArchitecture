package com.destore.inventory;

import com.destore.common.events.StockLowEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles({"monolith","test"})
@RecordApplicationEvents
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ApplicationEvents events;

    @Test
    void adjustsAndEmitsLowStock() {
        inventoryService.adjust(new InventoryAdjustmentRequest("SKU-999", -5));
        long count = events.stream(StockLowEvent.class).count();
        assertThat(count).isGreaterThanOrEqualTo(1);
        InventoryItemView item = inventoryService.getItem("SKU-999");
        assertThat(item).isNotNull();
        assertThat(item.stock()).isEqualTo(-5);
    }
}
