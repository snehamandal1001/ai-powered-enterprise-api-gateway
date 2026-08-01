package com.aigateway.analytics.service;

import com.aigateway.analytics.dto.RequestLogRequest;
import com.aigateway.analytics.dto.SummaryResponse;
import com.aigateway.analytics.model.RequestLog;
import com.aigateway.analytics.repository.RequestLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final RequestLogRepository requestLogRepository;

    public AnalyticsService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Transactional
    public void recordRequest(RequestLogRequest request) {
        RequestLog log = new RequestLog(
                request.getHttpMethod(),
                request.getPath(),
                request.getStatusCode(),
                request.getDurationMs(),
                request.getClientIp()
        );
        requestLogRepository.save(log);
    }

    /**
     * Builds a plain-data summary of traffic over the last N hours.
     * This is exactly the data Step 8's AI endpoint will hand to an
     * LLM so it can answer questions in plain English about it.
     */
    @Transactional(readOnly = true)
    public SummaryResponse getSummary(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);

        SummaryResponse response = new SummaryResponse();
        response.setWindowDescription("Last " + hoursBack + " hour(s)");
        response.setTotalRequests(requestLogRepository.countByOccurredAtAfter(since));
        response.setAverageDurationMs(requestLogRepository.averageDurationSince(since));
        response.setRequestsByPath(toMapList(requestLogRepository.countRequestsByPathSince(since)));
        response.setErrorsByPath(toMapList(requestLogRepository.countErrorsByPathSince(since)));

        return response;
    }

    @Transactional(readOnly = true)
    public List<RequestLog> getRecentLogs(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return requestLogRepository.findByOccurredAtAfter(since);
    }

    private List<Map<String, Object>> toMapList(List<Object[]> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("path", row[0]);
            map.put("count", row[1]);
            return map;
        }).toList();
    }
}
