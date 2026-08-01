package com.aigateway.analytics.ai;

import com.aigateway.analytics.model.RequestLog;
import com.aigateway.analytics.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A genuine statistical anomaly detector - this is real math, not a
 * marketing claim. It's worth being able to explain exactly how it
 * works in an interview:
 *
 *   1. Split the last N hours into 1-minute buckets.
 *   2. Count how many requests happened in each bucket.
 *   3. Compute the average and standard deviation across all buckets.
 *   4. Flag any bucket whose count is more than 2 standard deviations
 *      above the average - a "z-score" test. That's a genuinely
 *      unusual spike, not just normal minute-to-minute noise.
 */
@Service
public class AnomalyDetectionService {

    private static final double Z_SCORE_THRESHOLD = 2.0;

    private final AnalyticsService analyticsService;

    public AnomalyDetectionService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public List<Map<String, Object>> detectAnomalies(int hoursBack) {
        List<RequestLog> logs = analyticsService.getRecentLogs(hoursBack);

        // Step 1: bucket every log by the minute it happened in
        Map<Instant, Integer> countsByMinute = new LinkedHashMap<>();
        for (RequestLog log : logs) {
            Instant bucket = log.getOccurredAt().truncatedTo(ChronoUnit.MINUTES);
            countsByMinute.merge(bucket, 1, Integer::sum);
        }

        if (countsByMinute.size() < 2) {
            return List.of(); // not enough data yet to say what's "normal"
        }

        // Step 2: mean and standard deviation across all minute-buckets
        double mean = countsByMinute.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double variance = countsByMinute.values().stream()
                .mapToDouble(count -> Math.pow(count - mean, 2))
                .average()
                .orElse(0);

        double stdDev = Math.sqrt(variance);

        // Step 3: flag buckets whose z-score exceeds the threshold
        List<Map<String, Object>> anomalies = new ArrayList<>();
        for (Map.Entry<Instant, Integer> entry : countsByMinute.entrySet()) {
            double zScore = stdDev == 0 ? 0 : (entry.getValue() - mean) / stdDev;
            if (zScore > Z_SCORE_THRESHOLD) {
                Map<String, Object> anomaly = new LinkedHashMap<>();
                anomaly.put("minute", entry.getKey().toString());
                anomaly.put("requestCount", entry.getValue());
                anomaly.put("averageRequestsPerMinute", Math.round(mean * 100.0) / 100.0);
                anomaly.put("zScore", Math.round(zScore * 100.0) / 100.0);
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }
}
