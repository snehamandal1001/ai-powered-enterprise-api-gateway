package com.aigateway.analytics.ai;

import com.aigateway.analytics.dto.SummaryResponse;
import com.aigateway.analytics.service.AnalyticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * This is the actual "AI" in "AI-Powered Enterprise API Gateway."
 *
 * To be precise about what it does (worth understanding, not just
 * copying): it does NOT run its own machine learning model. It takes
 * your REAL traffic data out of Postgres, packages it as plain text,
 * and asks Anthropic's Claude API to read that data and answer a
 * question about it in plain English. That's exactly how real
 * products build "ask your data a question" features.
 */
@Service
public class AiQueryService {

    private final AnalyticsService analyticsService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public AiQueryService(
            AnalyticsService analyticsService,
            WebClient.Builder webClientBuilder,
            @Value("${anthropic.api-key:}") String apiKey,
            @Value("${anthropic.model:claude-haiku-4-5-20251001}") String model) {
        this.analyticsService = analyticsService;
        this.webClient = webClientBuilder.baseUrl("https://api.anthropic.com/v1").build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String answerQuestion(String question, int hoursBack) {
        if (apiKey == null || apiKey.isBlank()) {
            return "AI query is not configured yet. Set the ANTHROPIC_API_KEY environment "
                    + "variable and restart analytics-service to enable this feature.";
        }

        SummaryResponse summary = analyticsService.getSummary(hoursBack);
        String dataAsText = describeSummaryAsText(summary);

        String prompt = "You are analyzing real API gateway traffic data for a small "
                + "microservices project. Answer the question ONLY using the data below. "
                + "Be concise - 2 to 4 sentences. If the data doesn't contain the answer, say so.\n\n"
                + "TRAFFIC DATA:\n" + dataAsText + "\n\n"
                + "QUESTION: " + question;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 300,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        try {
            String rawResponse = webClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractAnswerText(rawResponse);
        } catch (Exception ex) {
            return "Could not reach the AI service: " + ex.getMessage();
        }
    }

    private String describeSummaryAsText(SummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time window: ").append(summary.getWindowDescription()).append("\n");
        sb.append("Total requests: ").append(summary.getTotalRequests()).append("\n");
        sb.append("Average response time (ms): ").append(summary.getAverageDurationMs()).append("\n");
        sb.append("Requests per endpoint: ").append(summary.getRequestsByPath()).append("\n");
        sb.append("Errors per endpoint: ").append(summary.getErrorsByPath()).append("\n");
        return sb.toString();
    }

    private String extractAnswerText(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode contentArray = root.path("content");
            if (contentArray.isArray() && !contentArray.isEmpty()) {
                return contentArray.get(0).path("text").asText();
            }
            return "AI response was empty or in an unexpected format.";
        } catch (Exception ex) {
            return "Could not parse the AI response: " + ex.getMessage();
        }
    }
}
