package com.destore.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, String> {
}
