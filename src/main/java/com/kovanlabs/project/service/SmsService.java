package com.kovanlabs.project.service;

import com.kovanlabs.project.model.Bill;
import com.kovanlabs.project.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final Environment env;
    private final HttpClient httpClient;

    public SmsService(Environment env) {
        this.env = env;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public void sendBillSms(Customer customer, Bill bill) {
        if (customer == null || customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            logger.warn("Skipping SMS: customer phone is empty");
            return;
        }

        String enabled = env.getProperty("sms.enabled", "false");
        if (!Boolean.parseBoolean(enabled)) {
            logger.info("SMS disabled by config (sms.enabled=false). Bill SMS skipped.");
            return;
        }

        String apiUrl = env.getProperty("sms.api.url");
        String apiKey = env.getProperty("sms.api.key");
        String senderId = env.getProperty("sms.sender.id", "Ventorie");

        if (isBlank(apiUrl) || isBlank(apiKey)) {
            logger.warn("SMS config incomplete (sms.api.url/sms.api.key). Bill SMS skipped.");
            return;
        }

        String message = buildBillMessage(customer, bill);

        try {
            String formData = "to=" + encode(customer.getPhone())
                    + "&from=" + encode(senderId)
                    + "&message=" + encode(message);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Bill SMS sent successfully to {}", customer.getPhone());
            } else {
                logger.warn("Bill SMS failed. status={}, body={}", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Bill SMS send error for phone={}: {}", customer.getPhone(), e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String buildBillMessage(Customer customer, Bill bill) {
        Long orderId = bill.getOrder() != null ? bill.getOrder().getId() : null;
        return String.format(
                "Hi %s, your order %s bill: %s x%d, total %.2f. Thank you for choosing Ventorie.",
                safe(customer.getName()),
                orderId == null ? "-" : orderId,
                safe(bill.getProductName()),
                bill.getQuantity() == null ? 0 : bill.getQuantity(),
                bill.getTotalAmount() == null ? 0.0 : bill.getTotalAmount()
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

