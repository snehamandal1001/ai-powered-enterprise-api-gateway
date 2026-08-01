package com.aigateway.analytics.ai;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/ai")
public class AiController {

    private final AiQueryService aiQueryService;
    private final AnomalyDetectionService anomalyDetectionService;

    public AiController(AiQueryService aiQueryService, AnomalyDetectionService anomalyDetectionService) {
        this.aiQueryService = aiQueryService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    /**
     * Ask a plain-English question about your real traffic data.
     * Example: {"question": "which endpoint got the most traffic?"}
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(
            @Valid @RequestBody AiQueryRequest request,
            @RequestParam(defaultValue = "24") int hoursBack) {
        String answer = aiQueryService.answerQuestion(request.getQuestion(), hoursBack);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    /**
     * Real statistical spike detection - see AnomalyDetectionService
     * for exactly how this works.
     */
    @GetMapping("/anomalies")
    public ResponseEntity<List<Map<String, Object>>> anomalies(
            @RequestParam(defaultValue = "1") int hoursBack) {
        return ResponseEntity.ok(anomalyDetectionService.detectAnomalies(hoursBack));
    }
}
