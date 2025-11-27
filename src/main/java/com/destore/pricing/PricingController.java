package com.destore.pricing;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/pricing")
@Profile({"monolith", "pricing"})
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/rules")
    public Collection<PromotionRule> rules() {
        return pricingService.listRules();
    }

    @PostMapping("/rules")
    public ResponseEntity<PromotionRule> addRule(@Valid @RequestBody PromotionRule rule) {
        return ResponseEntity.ok(pricingService.addRule(rule));
    }

    @PostMapping("/price")
    public ResponseEntity<PriceResponse> calculate(@Valid @RequestBody PriceRequest request) {
        return ResponseEntity.ok(pricingService.calculatePrice(request));
    }
}
