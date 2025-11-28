package com.destore.pricing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"monolith","test"})
class PricingServiceTest {

    @Autowired
    private PricingService pricingService;

    @Test
    void calculatesThreeForTwoSavings() {
        PriceRequest request = new PriceRequest("SKU-1", 10.0, 3, "R-342");
        PriceResponse response = pricingService.calculatePrice(request);
        assertThat(response.savings()).isEqualTo(10.0);
        assertThat(response.totalPrice()).isEqualTo(20.0);
    }
}
