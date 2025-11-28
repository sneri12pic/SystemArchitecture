package com.destore.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyOfferRepository extends JpaRepository<LoyaltyOfferEntity, Long> {
    List<LoyaltyOfferEntity> findByCustomerId(String customerId);
    void deleteByCustomerId(String customerId);
}
