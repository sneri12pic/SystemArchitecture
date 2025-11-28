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
    private final LoyaltyOfferRepository repository;

    public LoyaltyService(LoyaltyOfferRepository repository) {
        this.repository = repository;
        seedDefaults();
    }

    public List<LoyaltyOffer> offersFor(String customerId) {
        loadIfEmpty(customerId);
        return offers.getOrDefault(customerId, offers.getOrDefault("default", List.of()));
    }

    public void putOffers(String customerId, List<LoyaltyOffer> newOffers) {
        offers.put(customerId, newOffers);
        repository.deleteByCustomerId(customerId);
        newOffers.forEach(o -> repository.save(new LoyaltyOfferEntity(customerId, o.description(), o.discountPercentage())));
    }

    private void seedDefaults() {
        if (repository.count() == 0) {
            repository.save(new LoyaltyOfferEntity("default", "5% off everything for loyalty members", 5.0));
            repository.save(new LoyaltyOfferEntity("default", "Free delivery for loyalty members", 100.0));
        }
        repository.findByCustomerId("default")
                .forEach(e -> offers.computeIfAbsent("default", k -> new java.util.ArrayList<>())
                        .add(new LoyaltyOffer("L-" + e.getId(), e.getDescription(), e.getDiscountPercentage())));
    }

    private void loadIfEmpty(String customerId) {
        if (offers.containsKey(customerId)) {
            return;
        }
        List<LoyaltyOffer> loaded = repository.findByCustomerId(customerId).stream()
                .map(e -> new LoyaltyOffer("L-" + e.getId(), e.getDescription(), e.getDiscountPercentage()))
                .toList();
        if (!loaded.isEmpty()) {
            offers.put(customerId, loaded);
        }
    }
}
