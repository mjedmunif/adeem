package org.example.adeem.Service;

import org.example.adeem.API.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;


public class MoyasarApiService {

    @Value("${moyasar.secret-key}")
    private String secretKey;

    private final RestClient restClient = RestClient.create();

    // ==================== أنشئ Invoice (صفحة دفع جاهزة) ====================
    public Map<String, String> createInvoice(BigDecimal amount, String description, String callbackUrl) {

        String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        // Moyasar يتطلب المبلغ بأصغر وحدة عملة (هللة) - يعني نضرب بـ 100
        int amountInHalalas = amount.multiply(BigDecimal.valueOf(100)).intValue();

        Map<String, Object> requestBody = Map.of(
                "amount", amountInHalalas,
                "currency", "SAR",
                "description", description,
                "callback_url", callbackUrl
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("https://api.moyasar.com/v1/invoices")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String invoiceId = (String) response.get("id");
            String checkoutUrl = (String) response.get("url");
            String status = (String) response.get("status");

            return Map.of(
                    "invoiceId", invoiceId,
                    "checkoutUrl", checkoutUrl,
                    "status", status
            );

        } catch (Exception e) {
            throw new ExternalServiceException("Failed to create Moyasar invoice", e);
        }
    }

    // ==================== تحقق من حالة Invoice ====================
    public String getInvoiceStatus(String invoiceId) {

        String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://api.moyasar.com/v1/invoices/" + invoiceId)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("status");

        } catch (Exception e) {
            throw new ExternalServiceException("Failed to fetch Moyasar invoice status", e);
        }
    }
}