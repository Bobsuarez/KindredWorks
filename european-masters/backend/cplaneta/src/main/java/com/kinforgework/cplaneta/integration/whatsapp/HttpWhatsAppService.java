package com.kinforgework.cplaneta.integration.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP-based WhatsApp integration.
 * Replace the payload structure according to your provider's API specification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpWhatsAppService implements WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${app.whatsapp.api-url}")
    private String apiUrl;

    @Value("${app.whatsapp.api-key}")
    private String apiKey;

    @Value("${app.whatsapp.from-number}")
    private String fromNumber;

    @Override
    public void sendTextMessage(String phoneNumber, String message) {
        try {
            HttpHeaders headers = buildHeaders();

            // Ajustado para coincidir con los requerimientos de la API: numero y mensaje
            Map<String, Object> payload = Map.of(
                    "numero", phoneNumber,
                    "mensaje", message
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "/send-message", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp message sent successfully to '{}'", phoneNumber);
            } else {
                log.warn(
                        "WhatsApp API returned non-2xx status {} for '{}'",
                        response.getStatusCode(), phoneNumber
                );
                throw new RuntimeException(
                        "WhatsApp API error: HTTP " + response.getStatusCode());
            }

        } catch (Exception ex) {
            log.error("Failed to send WhatsApp message to '{}': {}", phoneNumber, ex.getMessage());
            throw new RuntimeException("WhatsApp delivery failed for: " + phoneNumber, ex);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
        return headers;
    }
}
