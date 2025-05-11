package org.example;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PaymentOption {
    private String id;
    private Map<String, BigDecimal> payments;
    private BigDecimal discount;
    private BigDecimal pointsUsed;

    public PaymentOption() {
        this.payments = new HashMap<>();
        this.discount = BigDecimal.ZERO;
        this.pointsUsed = BigDecimal.ZERO;
    }
}
