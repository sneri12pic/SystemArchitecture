package com.destore.loyalty;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/loyalty")
@Profile({"monolith", "loyalty"})
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/offers/{customerId}")
    public List<LoyaltyOffer> offers(@PathVariable String customerId) {
        return loyaltyService.offersFor(customerId);
    }

    @PostMapping("/offers/{customerId}")
    public ResponseEntity<Void> overrideOffers(@PathVariable String customerId, @Valid @RequestBody List<LoyaltyOffer> offers) {
        loyaltyService.putOffers(customerId, offers);
        return ResponseEntity.accepted().build();
    }
}
