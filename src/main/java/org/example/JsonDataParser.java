package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JsonDataParser {
    static ObjectMapper mapper = new ObjectMapper();

    public static List<Order> parseOrders(String filePath) throws IOException {
        return Arrays.asList(mapper.readValue(new File(filePath), Order[].class));
    }

    public static List<PaymentMethod> parsePaymentMethods(String filePath) throws IOException {
        return Arrays.asList(mapper.readValue(new File(filePath), PaymentMethod[].class));
    }
}
