package com.adspeek.authservice.service;

import com.adspeek.authservice.service.impl.IdGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdGenerationServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private IdGenerationServiceImpl idGenerationService;

    @Test
    void testGenerateNextLeadId_FirstLeadOfYear() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("LEAD-" + currentYear + "-%")))
                .thenReturn(1);

        // Act
        String leadId = idGenerationService.generateNextLeadId();

        // Assert
        assertNotNull(leadId);
        assertTrue(leadId.matches("^LEAD-" + currentYear + "-\\d{3}$"));
        assertEquals("LEAD-" + currentYear + "-001", leadId);
    }

    @Test
    void testGenerateNextLeadId_SubsequentLeads() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("LEAD-" + currentYear + "-%")))
                .thenReturn(15);

        // Act
        String leadId = idGenerationService.generateNextLeadId();

        // Assert
        assertNotNull(leadId);
        assertTrue(leadId.matches("^LEAD-" + currentYear + "-\\d{3}$"));
        assertEquals("LEAD-" + currentYear + "-015", leadId);
    }

    @Test
    void testGenerateNextServiceRequestId_FirstRequestOfYear() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("SR-" + currentYear + "-%")))
                .thenReturn(1);

        // Act
        String requestId = idGenerationService.generateNextServiceRequestId();

        // Assert
        assertNotNull(requestId);
        assertTrue(requestId.matches("^SR-" + currentYear + "-\\d{3}$"));
        assertEquals("SR-" + currentYear + "-001", requestId);
    }

    @Test
    void testGenerateNextServiceRequestId_SubsequentRequests() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("SR-" + currentYear + "-%")))
                .thenReturn(42);

        // Act
        String requestId = idGenerationService.generateNextServiceRequestId();

        // Assert
        assertNotNull(requestId);
        assertTrue(requestId.matches("^SR-" + currentYear + "-\\d{3}$"));
        assertEquals("SR-" + currentYear + "-042", requestId);
    }

    @Test
    void testGenerateNextLeadId_WhenNoExistingLeads() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("LEAD-" + currentYear + "-%")))
                .thenReturn(null);

        // Act
        String leadId = idGenerationService.generateNextLeadId();

        // Assert
        assertNotNull(leadId);
        assertTrue(leadId.matches("^LEAD-" + currentYear + "-\\d{3}$"));
        assertEquals("LEAD-" + currentYear + "-001", leadId);
    }

    @Test
    void testGenerateNextServiceRequestId_WhenNoExistingRequests() {
        // Arrange
        int currentYear = Year.now().getValue();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("SR-" + currentYear + "-%")))
                .thenReturn(null);

        // Act
        String requestId = idGenerationService.generateNextServiceRequestId();

        // Assert
        assertNotNull(requestId);
        assertTrue(requestId.matches("^SR-" + currentYear + "-\\d{3}$"));
        assertEquals("SR-" + currentYear + "-001", requestId);
    }
}
