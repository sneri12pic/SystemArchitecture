package com.destore.finance;

public record FinanceDecision(
        boolean approved,
        String message,
        String externalReference
) {
}
