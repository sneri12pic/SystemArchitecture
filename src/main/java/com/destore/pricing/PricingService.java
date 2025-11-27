package com.destore.pricing;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile({"monolith", "pricing"})
public class PricingService {

    private final Map<String, PromotionRule> rules = new ConcurrentHashMap<>();

    public PricingService() {
        // Seed a couple of example rules so the prototype has behaviour out of the box.
        addRule(new PromotionRule("R-BOGOF", "Buy one get one free", PromotionType.BOGOF, 0.0, 2));
        addRule(new PromotionRule("R-342", "3 for 2 mix-and-match", PromotionType.THREE_FOR_TWO, 0.0, 3));
        addRule(new PromotionRule("R-LOYALTY10", "10% off for loyalty customers", PromotionType.PERCENTAGE_DISCOUNT, 10.0, 1));
        addRule(new PromotionRule("R-FREE-DELIVERY", "Free delivery over threshold", PromotionType.FREE_DELIVERY, 5.0, 1));
    }

    public PromotionRule addRule(PromotionRule rule) {
        rules.put(rule.id(), rule);
        return rule;
    }

    public Collection<PromotionRule> listRules() {
        return rules.values();
    }

    public PriceResponse calculatePrice(PriceRequest request) {
        double base = request.unitPrice() * request.quantity();
        PromotionRule rule = resolveRule(request.promotionRuleId());
        double savings = calculateSavings(rule, request.quantity(), request.unitPrice());
        double total = Math.max(base - savings, 0.0);
        return new PriceResponse(total, savings, rule != null ? rule.id() : null);
    }

    private PromotionRule resolveRule(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            return null;
        }
        return rules.get(ruleId);
    }

    private double calculateSavings(PromotionRule rule, int quantity, double unitPrice) {
        if (rule == null || quantity < rule.minimumQuantity()) {
            return 0.0;
        }

        return switch (rule.type()) {
            case BOGOF -> (quantity / 2) * unitPrice;
            case THREE_FOR_TWO -> (quantity / 3) * unitPrice;
            case PERCENTAGE_DISCOUNT -> (unitPrice * quantity) * (rule.discountValue() / 100.0);
            case FREE_DELIVERY -> rule.discountValue();
            case NONE -> 0.0;
        };
    }
}
