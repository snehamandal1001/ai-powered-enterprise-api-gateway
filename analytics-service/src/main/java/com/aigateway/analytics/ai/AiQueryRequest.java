package com.aigateway.analytics.ai;

import jakarta.validation.constraints.NotBlank;

public class AiQueryRequest {

    @NotBlank(message = "Question is required")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
