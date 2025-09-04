package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.service.IdGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdGenerationServiceImpl implements IdGenerationService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public String generateNextLeadId() {
        int currentYear = Year.now().getValue();
        String yearPrefix = String.valueOf(currentYear);

        // First, try to fix any old format IDs for the current year
        fixOldLeadIdsForYear(currentYear);

        // Get the next sequence number for the current year
        String sql = """
                SELECT CAST(COALESCE(MAX(
                	CASE
                		WHEN lead_id REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$'
                		THEN CAST(SUBSTRING_INDEX(lead_id, '-', -1) AS UNSIGNED)
                		ELSE 0
                	END
                ), 0) + 1 AS SIGNED)
                FROM leads
                WHERE lead_id LIKE ?
                """;

        Long nextNumberLong = jdbcTemplate.queryForObject(sql, Long.class, "LEAD-" + yearPrefix + "-%");

        int nextNumber = (nextNumberLong == null || nextNumberLong <= 0L) ? 1 : nextNumberLong.intValue();

        String formattedNumber = String.format("%03d", nextNumber);
        String leadId = "LEAD-" + yearPrefix + "-" + formattedNumber;

        log.info("Generated new Lead ID: {}", leadId);
        return leadId;
    }

    @Override
    @Transactional
    public String generateNextServiceRequestId() {
        int currentYear = Year.now().getValue();
        String yearPrefix = String.valueOf(currentYear);

        // First, try to fix any old format IDs for the current year
        fixOldServiceRequestIdsForYear(currentYear);

        // Get the next sequence number for the current year
        String sql = """
                SELECT CAST(COALESCE(MAX(
                	CASE
                		WHEN request_id REGEXP '^SR-[0-9]{4}-[0-9]{3}$'
                		THEN CAST(SUBSTRING_INDEX(request_id, '-', -1) AS UNSIGNED)
                		ELSE 0
                	END
                ), 0) + 1 AS SIGNED)
                FROM service_requests
                WHERE request_id LIKE ?
                """;

        Long nextNumberLong = jdbcTemplate.queryForObject(sql, Long.class, "SR-" + yearPrefix + "-%");

        int nextNumber = (nextNumberLong == null || nextNumberLong <= 0L) ? 1 : nextNumberLong.intValue();

        String formattedNumber = String.format("%03d", nextNumber);
        String requestId = "SR-" + yearPrefix + "-" + formattedNumber;

        log.info("Generated new Service Request ID: {}", requestId);
        return requestId;
    }

    /**
     * Fix old format lead IDs for a specific year
     */
    private void fixOldLeadIdsForYear(int year) {
        try {
            String yearPrefix = String.valueOf(year);
            String updateSql = """
                    UPDATE leads
                    SET lead_id = CONCAT('LEAD-', ?, '-', LPAD(id, 3, '0'))
                    WHERE lead_id LIKE 'LEAD%'
                      AND lead_id NOT LIKE 'LEAD-%-%'
                      AND lead_id NOT REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$'
                      AND YEAR(created_at) = ?
                    """;

            int updated = jdbcTemplate.update(updateSql, yearPrefix, year);
            if (updated > 0) {
                log.info("Fixed {} old format lead IDs for year {}", updated, year);
            }
        } catch (Exception e) {
            log.warn("Failed to fix old lead IDs for year {}: {}", year, e.getMessage());
        }
    }

    /**
     * Fix old format service request IDs for a specific year
     */
    private void fixOldServiceRequestIdsForYear(int year) {
        try {
            String yearPrefix = String.valueOf(year);
            String updateSql = """
                    UPDATE service_requests
                    SET request_id = CONCAT('SR-', ?, '-', LPAD(id, 3, '0'))
                    WHERE request_id LIKE 'SR%'
                      AND request_id NOT LIKE 'SR-%-%'
                      AND request_id NOT REGEXP '^SR-[0-9]{4}-[0-9]{3}$'
                      AND YEAR(created_at) = ?
                    """;

            int updated = jdbcTemplate.update(updateSql, yearPrefix, year);
            if (updated > 0) {
                log.info("Fixed {} old format service request IDs for year {}", updated, year);
            }
        } catch (Exception e) {
            log.warn("Failed to fix old service request IDs for year {}: {}", year, e.getMessage());
        }
    }
}
