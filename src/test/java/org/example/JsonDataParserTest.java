package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonDataParserTest {
    @TempDir
    Path tempDir;

    @Test
    void parseOrders_correctJsonFile_returnsListOfOrders() throws IOException {
        Path file = tempDir.resolve("orders.json");
        String json = """
                [
                  {
                    "id": "ORDER1",
                    "value": "100.00",
                    "promotions": [
                      "mZysk"
                    ]
                  },
                  {
                    "id": "ORDER2",
                    "value": "50.00"
                  }
                ]
                """;
        Files.writeString(file, json);

        List<Order> orders = JsonDataParser.parseOrders(file.toString());

        assertEquals(2, orders.size());

        assertEquals("ORDER1", orders.get(0).getId());
        assertEquals(new BigDecimal("100.00"), orders.get(0).getValue());
        assertEquals("mZysk", orders.get(0).getPromotions().get(0));

        assertEquals("ORDER2", orders.get(1).getId());
        assertEquals(new BigDecimal("50.00"), orders.get(1).getValue());
        assertNull(orders.get(1).getPromotions());
    }

    @Test
    void parsePaymentMethods_correctJsonFile_returnsListOfPaymentMethods() throws IOException {
        Path file = tempDir.resolve("paymentmethods.json");
        String json = """
                [
                  {
                    "id": "PUNKTY",
                    "discount": "15",
                    "limit": "100.00"
                  },
                  {
                    "id": "mZysk",
                    "discount": "10",
                    "limit": "180.00"
                  },
                  {
                    "id": "BosBankrut",
                    "discount": "5",
                    "limit": "200.00"
                  }
                ]
                """;
        Files.writeString(file, json);

        List<PaymentMethod> paymentMethods = JsonDataParser.parsePaymentMethods(file.toString());

        assertEquals(3, paymentMethods.size());
        assertEquals("PUNKTY", paymentMethods.get(0).getId());
        assertEquals(10, paymentMethods.get(1).getDiscount());
        assertEquals(new BigDecimal("200.00"), paymentMethods.get(2).getLimit());
    }

    @Test
    void parseOrders_fileNotFound_throwsIOException() {
        assertThrows(IOException.class, () -> JsonDataParser.parseOrders("test.json"));
    }

    @Test
    void parsePaymentMethods_fileNotFound_throwsIOException() {
        assertThrows(IOException.class, () -> JsonDataParser.parseOrders("test.json"));
    }
}