package com.destore.loyalty;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile({"monolith", "loyalty"})
public class LoyaltyService {

    private final Map<String, List<LoyaltyOffer>> offers = new ConcurrentHashMap<>();

    public LoyaltyService() {
        offers.put("default", List.of(
                new LoyaltyOffer("L-5", "5% off everything for loyalty members", 5.0),
                new LoyaltyOffer("L-DELIVERY", "Free delivery for loyalty members", 100.0)
        ));
    }

    public List<LoyaltyOffer> offersFor(String customerId) {
        return offers.getOrDefault(customerId, offers.getOrDefault("default", List.of()));
    }

    public void putOffers(String customerId, List<LoyaltyOffer> newOffers) {
        offers.put(customerId, newOffers);
    }
}
