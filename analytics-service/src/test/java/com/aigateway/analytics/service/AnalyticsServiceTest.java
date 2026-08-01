package com.aigateway.analytics.service;

import com.aigateway.analytics.dto.RequestLogRequest;
import com.aigateway.analytics.model.RequestLog;
import com.aigateway.analytics.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private RequestLogRepository requestLogRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void recordRequest_savesLogWithCorrectFields() {
        RequestLogRequest request = new RequestLogRequest();
        request.setHttpMethod("GET");
        request.setPath("/api/products");
        request.setStatusCode(200);
        request.setDurationMs(42L);
        request.setClientIp("127.0.0.1");

        analyticsService.recordRequest(request);

        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
    }

    @Test
    void getSummary_returnsTotalRequestCountFromRepository() {
        when(requestLogRepository.countByOccurredAtAfter(any())).thenReturn(42L);
        when(requestLogRepository.averageDurationSince(any())).thenReturn(15.5);

        var summary = analyticsService.getSummary(1);

        assertEquals(42L, summary.getTotalRequests());
        assertEquals(15.5, summary.getAverageDurationMs());
    }
}
