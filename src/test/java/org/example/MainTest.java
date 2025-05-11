package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeEach
    void resetPointsMethod() {
        Main.pointsMethod = null;
    }

    @Test
    void optimizePayments_noOrders_returnsMapWithNothingSpent() {
        List<Order> orders = new ArrayList<>();
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("Test", 5, new BigDecimal("50.00")));

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("Test"));
        assertEquals(BigDecimal.ZERO, result.get("Test"));
    }

    @Test
    void optimizePayments_noPaymentMethods_returnsEmptyMap() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100"), null));
        List<PaymentMethod> paymentMethods = new ArrayList<>();

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertTrue(result.isEmpty());
    }

    @Test
    void optimizePayments_orderWithPromotionPayment() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100"), List.of("Test")));
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("Test", 20, new BigDecimal("100.00")));

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("Test"));
        assertEquals(new BigDecimal("80.00"), result.get("Test"));
    }

    @Test
    void optimizePayments_orderWithFullPointsPayment() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100"), null));
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        Main.pointsMethod = new PaymentMethod("PUNKTY", 20, new BigDecimal("100.00"));
        paymentMethods.add(Main.pointsMethod);

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("PUNKTY"));
        assertEquals(new BigDecimal("80.00"), result.get("PUNKTY"));
    }

    @Test
    void optimizePayments_orderWithPartialPointsPayment() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100"), List.of("Test")));
        Main.pointsMethod = new PaymentMethod("PUNKTY", 10, new BigDecimal("20.00"));
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("Test", 0, new BigDecimal("200.00")));
        paymentMethods.add(Main.pointsMethod);

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("PUNKTY"));
        assertTrue(result.containsKey("Test"));
        assertEquals(new BigDecimal("80.00"), result.get("Test"));
        assertEquals(new BigDecimal("10.00"), result.get("PUNKTY"));
    }

    @Test
    void optimizePayments_orderWIthNoDiscountPayment() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100.00"), null));
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("Test", 20, new BigDecimal("100.00")));

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("Test"));
        assertEquals(new BigDecimal("100.00"), result.get("Test"));
    }

    @Test
    void optimizePayments_pointsAndCardEqualDiscounts_preferPoints() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORDER", new BigDecimal("100"), List.of("Test")));
        Main.pointsMethod = new PaymentMethod("PUNKTY", 20, new BigDecimal("100.00"));
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("Test", 20, new BigDecimal("100.00")));
        paymentMethods.add(Main.pointsMethod);

        Map<String, BigDecimal> result = Main.optimizePayments(orders, paymentMethods);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("PUNKTY"));
        assertEquals(new BigDecimal("80.00"), result.get("PUNKTY"));
    }
}