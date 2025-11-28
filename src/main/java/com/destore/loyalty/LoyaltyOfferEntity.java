package com.destore.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loyalty_offers")
public class LoyaltyOfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_percentage")
    private double discountPercentage;

    protected LoyaltyOfferEntity() {
        // JPA
    }

    public LoyaltyOfferEntity(String customerId, String description, double discountPercentage) {
        this.customerId = customerId;
        this.description = description;
        this.discountPercentage = discountPercentage;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
