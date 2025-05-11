package org.example;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {
    private String id;
    private int discount;
    private BigDecimal limit;
}
