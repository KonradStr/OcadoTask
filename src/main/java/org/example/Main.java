package org.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    static PaymentMethod pointsMethod = null;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar app.jar <orders json file> <payment methods json file>");
            System.exit(1);
        }

        try {
            List<Order> orders = JsonDataParser.parseOrders(args[0]);
            List<PaymentMethod> paymentMethods = JsonDataParser.parsePaymentMethods(args[1]);

            Map<String, BigDecimal> results = optimizePayments(orders, paymentMethods);

            for (Map.Entry<String, BigDecimal> entry : results.entrySet()) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println(entry.getKey() + ": " + entry.getValue().setScale(2, RoundingMode.HALF_UP));
                }
            }

        } catch (IOException e) {
            System.err.println("Error: " + e);
            System.exit(1);
        }
    }

    public static Map<String, BigDecimal> optimizePayments(List<Order> orders, List<PaymentMethod> paymentMethods) {
        Map<String, BigDecimal> methodsRemainingAmount = new HashMap<>();
        Map<String, BigDecimal> methodsSpentAmount = new HashMap<>();
        Map<String, PaymentMethod> paymentMethodsMap = new HashMap<>();

        for (PaymentMethod method : paymentMethods) {
            methodsRemainingAmount.put(method.getId(), method.getLimit());
            methodsSpentAmount.put(method.getId(), BigDecimal.ZERO);

            paymentMethodsMap.put(method.getId(), method);
            if (method.getId().equals("PUNKTY")) {
                pointsMethod = method;
            }
        }

        for (Order order : orders) {
            processOrder(order, paymentMethodsMap, methodsRemainingAmount, methodsSpentAmount);
        }

        return methodsSpentAmount;
    }

    private static void processOrder(Order order, Map<String, PaymentMethod> paymentMethodsMap, Map<String, BigDecimal> methodsRemainingAmount, Map<String, BigDecimal> methodsSpentAmount) {
        List<PaymentOption> options = createPaymentOptions(order, paymentMethodsMap, methodsRemainingAmount);

        Collections.sort(options, (o1, o2) -> {
            int discountComparison = o2.getDiscount().compareTo(o1.getDiscount());
            if (discountComparison != 0) {
                return discountComparison;
            }

            return o2.getPointsUsed().compareTo(o1.getPointsUsed());
        });

        if(!options.isEmpty()) {
            useBestOptionForOrder(options.getFirst(), methodsSpentAmount, methodsRemainingAmount);
        } else {
            System.err.println("No payment options found for order " + order.getId());
        }
    }

    private static void useBestOptionForOrder(PaymentOption paymentOption, Map<String, BigDecimal> methodsSpentAmount, Map<String, BigDecimal> methodsRemainingAmount) {
        for(Map.Entry<String, BigDecimal> entry : paymentOption.getPayments().entrySet()) {
            String methodId = entry.getKey();
            BigDecimal value = entry.getValue();

            methodsSpentAmount.put(methodId, methodsSpentAmount.get(methodId).add(value));
            methodsRemainingAmount.put(methodId, methodsRemainingAmount.get(methodId).subtract(value));
        }
    }

    private static List<PaymentOption> createPaymentOptions(Order order, Map<String, PaymentMethod> paymentMethodsMap, Map<String, BigDecimal> methodsRemainingAmount) {
        List<PaymentOption> options = new ArrayList<>();

        // Opcja 1 - największym rabatem
        if (order.getPromotions() != null) {
            for (String promotion : order.getPromotions()) {
                if (!promotion.equals("PUNKTY")) {
                    PaymentOption bestPromotion = createBestPromotionOption(order, paymentMethodsMap.get(promotion), methodsRemainingAmount);
                    if (bestPromotion != null) {
                        options.add(bestPromotion);
                    }
                }
            }
        }

        // Opcja 2 - całość punktami
        if (pointsMethod != null && methodsRemainingAmount.get("PUNKTY").compareTo(BigDecimal.ZERO) > 0) {
            PaymentOption pointsOnly = createPointsOnlyOption(order, methodsRemainingAmount);
            if (pointsOnly != null) {
                options.add(pointsOnly);
            }
        }

        // Opcja 3 - 10% punktami
        if (pointsMethod != null && methodsRemainingAmount.get("PUNKTY").compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal minAmount = new BigDecimal(String.valueOf(order.getValue().multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.CEILING)));

            if (minAmount.compareTo(methodsRemainingAmount.get("PUNKTY")) <= 0) {
                for (PaymentMethod method : paymentMethodsMap.values()) {
                    if (!method.getId().equals("PUNKTY")) {
                        PaymentOption partlyPoints = createPartlyPointsOption(order, method, minAmount, methodsRemainingAmount);
                        if (partlyPoints != null) {
                            options.add(partlyPoints);
                        }
                    }
                }
            }
        }

        // Opcja 4 - bez rabatów
        for (PaymentMethod method : paymentMethodsMap.values()) {
            if (!method.getId().equals("PUNKTY")) {
                PaymentOption withoutPromotion = createWithoutPromotionOption(order, method, methodsRemainingAmount);
                if (withoutPromotion != null) {
                    options.add(withoutPromotion);
                }
            }
        }

        return options;
    }

    // Opcja 1
    private static PaymentOption createBestPromotionOption(Order order, PaymentMethod method, Map<String, BigDecimal> methodsRemainingAmount) {
        BigDecimal orderCost = new BigDecimal(String.valueOf(order.getValue()));
        BigDecimal discountRate = new BigDecimal(method.getDiscount()).divide(new BigDecimal("100"));

        BigDecimal discount = orderCost.multiply(BigDecimal.ONE.subtract(discountRate).setScale(2, RoundingMode.HALF_UP));

        if (discount.compareTo(methodsRemainingAmount.get(method.getId())) <= 0) {
            PaymentOption option = new PaymentOption();
            option.setId(order.getId());
            option.getPayments().put(method.getId(), discount);
            option.setDiscount(orderCost.subtract(discount));
            return option;
        }

        return null;
    }

    // Opcja 2
    private static PaymentOption createPointsOnlyOption(Order order, Map<String, BigDecimal> methodsRemainingAmount) {
        BigDecimal orderCost = new BigDecimal(String.valueOf(order.getValue()));
        BigDecimal discountRate = new BigDecimal(pointsMethod.getDiscount()).divide(new BigDecimal("100"));

        BigDecimal discount = orderCost.multiply(BigDecimal.ONE.subtract(discountRate).setScale(2, RoundingMode.HALF_UP));

        if (discount.compareTo(methodsRemainingAmount.get("PUNKTY")) <= 0) {
            PaymentOption option = new PaymentOption();
            option.setId(order.getId());
            option.getPayments().put("PUNKTY", discount);
            option.setDiscount(orderCost.subtract(discount));
            option.setPointsUsed(discount);
            return option;
        }

        return null;
    }

    // Opcja 3
    private static PaymentOption createPartlyPointsOption(Order order, PaymentMethod method, BigDecimal minAmount, Map<String, BigDecimal> methodsRemainingAmount) {
        BigDecimal orderCost = new BigDecimal(String.valueOf(order.getValue()));
        BigDecimal pointsLimit = methodsRemainingAmount.get("PUNKTY");
        BigDecimal pointsAmount = minAmount;

        // Za mało punktów, żeby było 10%
        if (pointsLimit.compareTo(minAmount) < 0) {
            return null;
        }

        // Jak rabat całkowity >10% to używa więcej punktów
        if (pointsLimit.compareTo(minAmount) > 0 && new BigDecimal(pointsMethod.getDiscount()).compareTo(new BigDecimal("10")) > 0) {
            BigDecimal pointsWorthUsing = orderCost;
            if (pointsWorthUsing.compareTo(pointsLimit) > 0) {
                pointsWorthUsing = pointsLimit;
            }
            pointsAmount = pointsWorthUsing;
        }

        BigDecimal discount = orderCost.multiply(BigDecimal.ONE.subtract(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP));
        BigDecimal cardAmount = discount.subtract(pointsAmount).setScale(2, RoundingMode.HALF_UP);

        if (cardAmount.compareTo(methodsRemainingAmount.get(method.getId())) <= 0 && cardAmount.compareTo(BigDecimal.ZERO) > 0) {
            PaymentOption option = new PaymentOption();
            option.setId(order.getId());
            option.getPayments().put("PUNKTY", pointsAmount);
            option.getPayments().put(method.getId(), cardAmount);
            option.setDiscount(orderCost.subtract(discount));
            option.setPointsUsed(pointsAmount);
            return option;
        }

        return null;
    }

    // Opcja 4
    private static PaymentOption createWithoutPromotionOption(Order order, PaymentMethod method, Map<String, BigDecimal> methodsRemainingAmount) {
        BigDecimal orderCost = new BigDecimal(String.valueOf(order.getValue()));

        if (orderCost.compareTo(methodsRemainingAmount.get(method.getId())) <= 0) {
            PaymentOption option = new PaymentOption();
            option.setId(order.getId());
            option.getPayments().put(method.getId(), orderCost);
            option.setDiscount(BigDecimal.ZERO);
            return option;
        }

        return null;
    }
}