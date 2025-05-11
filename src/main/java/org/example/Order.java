package org.example;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;
}
