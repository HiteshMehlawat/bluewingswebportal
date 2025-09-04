package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.DocumentDTO;
import com.adspeek.authservice.dto.LatestDocumentUploadDTO;
import com.adspeek.authservice.dto.DocumentStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

        // Upload document
        DocumentDTO uploadDocument(MultipartFile file, Long clientId, Long taskId, String documentType);

        // Get document by ID
        DocumentDTO getDocumentById(Long id);

        // Get documents by client
        List<DocumentDTO> getDocumentsByClient(Long clientId);

        // Get documents by task
        List<DocumentDTO> getDocumentsByTask(Long taskId);

        // Get all documents (admin view)
        List<DocumentDTO> getAllDocuments();

        // Get documents with filters
        Page<DocumentDTO> getDocumentsWithFilters(Long clientId, Long taskId, String documentType, String isVerified,
                        String searchTerm, int page, int size);

        Page<LatestDocumentUploadDTO> getLatestUploads(Pageable pageable);

        // Get documents with pagination
        Page<DocumentDTO> getDocumentsWithPagination(Pageable pageable);

        // Update document verification status
        DocumentDTO updateDocumentVerification(Long documentId, Boolean isVerified, String verifiedBy);

        // Reject document
        DocumentDTO rejectDocument(Long documentId, String rejectionReason);

        // Delete document
        void deleteDocument(Long id);

        // Download document (deprecated - use downloadDocumentWithResponse instead)
        @Deprecated
        byte[] downloadDocument(Long id);

        // Get document statistics
        DocumentStatisticsDTO getDocumentStatistics();

        // Get documents by type
        List<DocumentDTO> getDocumentsByType(String documentType);

        // Get documents by verification status
        List<DocumentDTO> getDocumentsByVerificationStatus(Boolean isVerified);

        // Search documents
        List<DocumentDTO> searchDocuments(String searchTerm);

        // Get document count by client
        Long getDocumentCountByClient(Long clientId);

        // Get verified document count by client
        Long getVerifiedDocumentCountByClient(Long clientId);

        // Client Dashboard specific methods
        Page<DocumentDTO> getDocumentsByClientWithPagination(Long clientId, Pageable pageable);

        Page<DocumentDTO> getDocumentsByClientWithPaginationAndFilters(Long clientId, Pageable pageable,
                        String documentType, String status);

        DocumentStatisticsDTO getDocumentStatisticsByClient(Long clientId);

        // Download document with proper response
        org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadDocumentWithResponse(
                        Long id);

        // Client-specific search methods
        List<DocumentDTO> searchDocumentsByClient(Long clientId, String searchTerm);

        List<DocumentDTO> getDocumentsByClientAndType(Long clientId, String documentType);

        // ========== STAFF-SPECIFIC METHODS ==========

        Page<DocumentDTO> getDocumentsByAssignedStaffWithFilters(String searchTerm, String status,
                        String documentType, Long clientId, Long taskId, Pageable pageable);

        // Get document statistics for staff (documents from assigned tasks)
        DocumentStatisticsDTO getDocumentStatisticsByAssignedStaff();
}