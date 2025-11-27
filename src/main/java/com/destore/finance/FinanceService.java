package com.destore.finance;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile({"monolith", "finance"})
public class FinanceService {

    public FinanceDecision submit(FinanceRequest request) {
        // Simplified stub that mimics calling Enabling and applying a trivial rule.
        boolean approved = request.amount() <= 2000;
        String message = approved ? "Approved by Enabling" : "Pending manual review by Enabling";
        String externalRef = "EN-" + UUID.randomUUID();
        return new FinanceDecision(approved, message, externalRef);
    }
}
