package com.destore.finance;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FinanceRequest(
        @NotBlank String customerId,
        @Min(1) double amount,
        @Min(1) int termMonths
) {
}
