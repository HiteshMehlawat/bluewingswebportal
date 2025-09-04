package com.adspeek.authservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Year;

@Service
public interface IdGenerationService {

    /**
     * Generate the next sequential ID for leads in format LEAD-YYYY-NNN
     * 
     * @return The next lead ID (e.g., LEAD-2025-001)
     */
    String generateNextLeadId();

    /**
     * Generate the next sequential ID for service requests in format SR-YYYY-NNN
     * 
     * @return The next service request ID (e.g., SR-2025-001)
     */
    String generateNextServiceRequestId();
}
