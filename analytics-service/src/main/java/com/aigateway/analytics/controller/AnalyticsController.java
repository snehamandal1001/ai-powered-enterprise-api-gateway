package com.aigateway.analytics.controller;

import com.aigateway.analytics.dto.RequestLogRequest;
import com.aigateway.analytics.dto.SummaryResponse;
import com.aigateway.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * The gateway calls this after every single request it handles.
     * This is the "logging pipeline" - every request anywhere in the
     * system eventually lands here.
     */
    @PostMapping("/logs")
    public ResponseEntity<Void> recordRequest(@Valid @RequestBody RequestLogRequest request) {
        analyticsService.recordRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Plain numeric summary - no AI here yet, just real aggregated
     * data. Step 8 builds the natural-language layer on top of this.
     */
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam(defaultValue = "1") int hoursBack) {
        return ResponseEntity.ok(analyticsService.getSummary(hoursBack));
    }
}
