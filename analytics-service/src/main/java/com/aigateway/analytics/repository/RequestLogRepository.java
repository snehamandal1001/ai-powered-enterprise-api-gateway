package com.aigateway.analytics.repository;

import com.aigateway.analytics.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByOccurredAtAfter(Instant since);

    @Query("SELECT r.path, COUNT(r) FROM RequestLog r WHERE r.occurredAt > :since GROUP BY r.path ORDER BY COUNT(r) DESC")
    List<Object[]> countRequestsByPathSince(@Param("since") Instant since);

    @Query("SELECT r.path, COUNT(r) FROM RequestLog r WHERE r.occurredAt > :since AND r.statusCode >= 400 GROUP BY r.path ORDER BY COUNT(r) DESC")
    List<Object[]> countErrorsByPathSince(@Param("since") Instant since);

    @Query("SELECT AVG(r.durationMs) FROM RequestLog r WHERE r.occurredAt > :since")
    Double averageDurationSince(@Param("since") Instant since);

    long countByOccurredAtAfter(Instant since);
}
