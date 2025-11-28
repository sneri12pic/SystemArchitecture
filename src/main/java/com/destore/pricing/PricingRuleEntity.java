package com.destore.pricing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pricing_rules")
public class PricingRuleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PromotionType type;

    @Column(name = "discount_value")
    private double discountValue;

    @Column(name = "minimum_quantity")
    private int minimumQuantity;

    protected PricingRuleEntity() {
        // JPA
    }

    public PricingRuleEntity(String id, String description, PromotionType type, double discountValue, int minimumQuantity) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.discountValue = discountValue;
        this.minimumQuantity = minimumQuantity;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public PromotionType getType() {
        return type;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }
}
