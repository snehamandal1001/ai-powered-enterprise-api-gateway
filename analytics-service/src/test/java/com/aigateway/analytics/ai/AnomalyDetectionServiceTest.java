package com.aigateway.analytics.ai;

import com.aigateway.analytics.model.RequestLog;
import com.aigateway.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    @Test
    void detectAnomalies_flagsAnObviousTrafficSpike() throws Exception {
        List<RequestLog> logs = new ArrayList<>();
        Instant baseMinute = Instant.parse("2026-08-01T10:00:00Z");

        // Nine quiet minutes: 2 requests each (normal baseline)
        for (int minute = 0; minute < 9; minute++) {
            for (int i = 0; i < 2; i++) {
                logs.add(buildLog(baseMinute.plusSeconds(minute * 60L)));
            }
        }

        // One spike minute: 30 requests (clearly abnormal)
        for (int i = 0; i < 30; i++) {
            logs.add(buildLog(baseMinute.plusSeconds(9 * 60L)));
        }

        when(analyticsService.getRecentLogs(1)).thenReturn(logs);

        List<Map<String, Object>> anomalies = anomalyDetectionService.detectAnomalies(1);

        assertEquals(1, anomalies.size());
        assertTrue((Integer) anomalies.get(0).get("requestCount") >= 30);
    }

    /**
     * RequestLog sets occurredAt via @PrePersist, which only runs
     * through JPA - not when we build objects directly in a test.
     * This helper sets it via reflection so the test can control
     * exact timestamps.
     */
    private RequestLog buildLog(Instant occurredAt) throws Exception {
        RequestLog log = new RequestLog("GET", "/api/products", 200, 10L, "127.0.0.1");
        Field field = RequestLog.class.getDeclaredField("occurredAt");
        field.setAccessible(true);
        field.set(log, occurredAt);
        return log;
    }
}
