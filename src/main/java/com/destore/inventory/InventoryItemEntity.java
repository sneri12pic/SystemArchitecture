package com.destore.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class InventoryItemEntity {

    @Id
    @Column(name = "sku", nullable = false, updatable = false)
    private String sku;

    @Column(name = "stock")
    private int stock;

    @Column(name = "reorder_threshold")
    private int reorderThreshold;

    protected InventoryItemEntity() {   
    }

    public InventoryItemEntity(String sku, int stock, int reorderThreshold) {
        this.sku = sku;
        this.stock = stock;
        this.reorderThreshold = reorderThreshold;
    }

    public String getSku() {
        return sku;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }
}
