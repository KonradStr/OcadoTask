package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PaymentMethod {
    private String id;
    private int discount;
    private BigDecimal limit;
}
