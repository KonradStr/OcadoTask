package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;
}
