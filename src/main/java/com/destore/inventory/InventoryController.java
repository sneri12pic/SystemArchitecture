package com.destore.inventory;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@Profile({"monolith", "inventory"})
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Collection<InventoryItemView> list() {
        return inventoryService.listInventory();
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryItemView> get(@PathVariable String sku) {
        InventoryItemView item = inventoryService.getItem(sku);
        return item == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(item);
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryItemView> adjust(@Valid @RequestBody InventoryAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjust(request));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncFromHq(@RequestBody Map<String, Integer> snapshot) {
        inventoryService.syncFromHq(snapshot);
        return ResponseEntity.accepted().build();
    }
}
