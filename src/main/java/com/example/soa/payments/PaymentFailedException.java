package com.example.soa.payments;

import com.example.soa.core.DomainException;

public class PaymentFailedException extends DomainException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
