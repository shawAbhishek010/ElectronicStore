package com.lcwd.electronicStore.ElectronicStore.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantChatResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.AssistantProductContextDto;
import com.lcwd.electronicStore.ElectronicStore.services.AssistantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/*
Purpose:
Keeps the Groq API key on the backend while the frontend handles product context selection.
*/
@Service
public class GroqAssistantService implements AssistantService {

    private static final String SYSTEM_PROMPT = """
            You are the SparkGadget shopping assistant.
            Help customers choose electronics from the provided product context only.
            Recommend specific products when they match the user's need, mention price and useful tradeoffs, and keep the answer under 120 words.
            If the context does not contain a matching product, say that clearly and suggest how to search the catalog.
            Do not invent stock, price, warranty, delivery, or payment details.
            """;

    private final RestClient restClient;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.model:openai/gpt-oss-20b}")
    private String groqModel;

    public GroqAssistantService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public AssistantChatResponse chat(AssistantChatRequest request) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Assistant is not configured yet.");
        }

        Map<String, Object> payload = Map.of(
                "model", groqModel,
                "temperature", 0.35,
                "max_completion_tokens", 220,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", buildUserPrompt(request))
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri(groqApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            String answer = extractAnswer(response);
            if (answer.isBlank()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Assistant returned an empty answer.");
            }

            return new AssistantChatResponse(
                    answer,
                    "Groq",
                    response.path("model").asText(groqModel),
                    response.path("id").asText("")
            );
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Assistant is temporarily unavailable.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Assistant service could not be reached.");
        }
    }

    private String buildUserPrompt(AssistantChatRequest request) {
        List<AssistantProductContextDto> products = request.getProducts() == null ? List.of() : request.getProducts();

        String productContext = products.stream()
                .filter(Objects::nonNull)
                .map(this::formatProduct)
                .collect(Collectors.joining("\n"));

        if (productContext.isBlank()) {
            productContext = "No product context was provided.";
        }

        return """
                Customer question:
                %s

                Available product context:
                %s
                """.formatted(request.getQuestion().trim(), productContext);
    }

    private String formatProduct(AssistantProductContextDto product) {
        String signals = product.getSignals() == null || product.getSignals().isEmpty()
                ? "none"
                : String.join(", ", product.getSignals());

        return "- %s | Category: %s | Price: INR %d | Original: INR %d | Discount: %d%% | Stock: %s | Quantity: %d | Signals: %s | Description: %s"
                .formatted(
                        safeText(product.getTitle()),
                        safeText(product.getCategory()),
                        product.getPrice(),
                        product.getOriginalPrice(),
                        product.getDiscountPercent(),
                        product.isStock() ? "in stock" : "out of stock",
                        product.getQuantity(),
                        signals,
                        safeText(product.getDescription())
                );
    }

    private String extractAnswer(JsonNode response) {
        if (response == null) {
            return "";
        }

        return response.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("")
                .trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
