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
        String enabled = env.getProperty("sms.enabled", "false");
        logger.info("Entering sendBillSms. Customer: {} | Phone: {} | Config Enabled: {}",
                customer.getName(), customer.getPhone(), enabled);

        if (customer == null || customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            logger.warn("Skipping SMS: customer phone is empty");
            return;
        }

        if (!Boolean.parseBoolean(enabled) || "WALKIN".equalsIgnoreCase(customer.getPhone())) {
            logger.info("SMS bypassed (sms.enabled=false or walk-in). No bill sent to {}", customer.getPhone());
            return;
        }

        String apiUrl = env.getProperty("sms.api.url");
        String apiKey = env.getProperty("sms.api.key");
        String route = env.getProperty("sms.route", "q");
        String language = env.getProperty("sms.language", "english");

        String message = buildBillMessage(customer, bill);

        if (isBlank(apiUrl) || isBlank(apiKey) || apiKey.contains("YOUR_API_KEY")) {
            logger.info("📱 [SIMULATOR] API key missing or default. Bill SMS logged: {}", message);
            return;
        }

        try {
            // India route 'q' expects exactly 10 digits.
            String cleanNumber = customer.getPhone().replaceAll("\\D", "");
            if (cleanNumber.length() == 12 && cleanNumber.startsWith("91")) {
                cleanNumber = cleanNumber.substring(2);
            }

            String fullUrl = String.format("%s?authorization=%s&message=%s&language=%s&route=%s&numbers=%s",
                    apiUrl,
                    apiKey,
                    encode(message),
                    language,
                    route,
                    cleanNumber
            );

            // Log diagnostic info (masking the key)
            logger.info("Fast2SMS Dispatch: url={}...", apiUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                logger.info("Fast2SMS Success! Response: {}", response.body());
            } else {
                logger.error("Fast2SMS Rejected! Status: {}, Details: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.error("SMS Pipeline Failure: {}", e.getMessage(), e);
        }
    }

    private String buildBillMessage(Customer customer, Bill bill) {
        return String.format(
                "Hi %s, your order bill: %s x%d, total Rs. %.2f. Thank you!",
                safe(customer.getName()),
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

