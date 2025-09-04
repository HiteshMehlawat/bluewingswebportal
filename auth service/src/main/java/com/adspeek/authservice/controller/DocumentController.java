package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.DocumentDTO;
import com.adspeek.authservice.dto.DocumentStatisticsDTO;
import com.adspeek.authservice.dto.LatestDocumentUploadDTO;
import com.adspeek.authservice.service.DocumentService;
import com.adspeek.authservice.service.ClientService;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final ClientService clientService;
    private final UserRepository userRepository;

    // Get current user's client ID
    private Long getCurrentClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return clientService.getClientIdByUserId(user.getId());
        }
        throw new RuntimeException("User not authenticated");
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam("documentType") String documentType) {
        try {
            DocumentDTO uploadedDocument = documentService.uploadDocument(file, clientId, taskId, documentType);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByClient(@PathVariable Long clientId) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByClient(clientId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByTask(@PathVariable Long taskId) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByTask(taskId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<DocumentDTO> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<DocumentDTO>> getDocumentsWithFilters(
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "isVerified", required = false) String isVerified,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<DocumentDTO> documents = documentService.getDocumentsWithFilters(
                    clientId, taskId, documentType, isVerified, searchTerm, page, size);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DocumentDTO> updateDocumentVerification(
            @PathVariable Long id,
            @RequestParam("isVerified") Boolean isVerified,
            @RequestParam(value = "verifiedBy", required = false) String verifiedBy) {
        try {
            DocumentDTO updatedDocument = documentService.updateDocumentVerification(id, isVerified, verifiedBy);
            return ResponseEntity.ok(updatedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DocumentDTO> rejectDocument(
            @PathVariable Long id,
            @RequestParam("rejectionReason") String rejectionReason) {
        try {
            DocumentDTO rejectedDocument = documentService.rejectDocument(id, rejectionReason);
            return ResponseEntity.ok(rejectedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long id) {
        try {
            byte[] fileContent = documentService.downloadDocument(id);
            DocumentDTO document = documentService.getDocumentById(id);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DocumentStatisticsDTO> getDocumentStatistics() {
        try {
            DocumentStatisticsDTO statistics = documentService.getDocumentStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{documentType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByType(@PathVariable String documentType) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByType(documentType);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/verification/{isVerified}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByVerificationStatus(@PathVariable Boolean isVerified) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByVerificationStatus(isVerified);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(@RequestParam("term") String searchTerm) {
        try {
            List<DocumentDTO> documents = documentService.searchDocuments(searchTerm);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<Long> getDocumentCountByClient(@PathVariable Long clientId) {
        try {
            Long count = documentService.getDocumentCountByClient(clientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}/verified-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<Long> getVerifiedDocumentCountByClient(@PathVariable Long clientId) {
        try {
            Long count = documentService.getVerifiedDocumentCountByClient(clientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/latest")
    public Page<LatestDocumentUploadDTO> getLatestUploads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return documentService.getLatestUploads(PageRequest.of(page, size));
    }

    // ========== CLIENT-SPECIFIC ENDPOINTS ==========

    @GetMapping("/my-documents")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<DocumentDTO>> getMyDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "status", required = false) String status) {
        try {
            Long clientId = getCurrentClientId();
            Page<DocumentDTO> documents = documentService.getDocumentsByClientWithPaginationAndFilters(
                    clientId, PageRequest.of(page, size), documentType, status);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error fetching client documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-documents/statistics")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DocumentStatisticsDTO> getMyDocumentStatistics() {
        try {
            Long clientId = getCurrentClientId();
            DocumentStatisticsDTO statistics = documentService.getDocumentStatisticsByClient(clientId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error fetching client document statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/my-documents/upload")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DocumentDTO> uploadMyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        try {
            Long clientId = getCurrentClientId();
            DocumentDTO uploadedDocument = documentService.uploadDocument(file, clientId, taskId, documentType);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
        } catch (Exception e) {
            log.error("Error uploading client document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/my-documents/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DocumentDTO> getMyDocumentById(@PathVariable Long id) {
        try {
            Long clientId = getCurrentClientId();
            DocumentDTO document = documentService.getDocumentById(id);

            // Verify the document belongs to the current client
            if (!document.getClientId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(document);
        } catch (Exception e) {
            log.error("Error fetching client document: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-documents/{id}/download")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ByteArrayResource> downloadMyDocument(@PathVariable Long id) {
        try {
            Long clientId = getCurrentClientId();
            DocumentDTO document = documentService.getDocumentById(id);

            // Verify the document belongs to the current client
            if (!document.getClientId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] fileContent = documentService.downloadDocument(id);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading client document: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-documents/search")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<DocumentDTO>> searchMyDocuments(@RequestParam("term") String searchTerm) {
        try {
            Long clientId = getCurrentClientId();
            List<DocumentDTO> documents = documentService.searchDocumentsByClient(clientId, searchTerm);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error searching client documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-documents/type/{documentType}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<DocumentDTO>> getMyDocumentsByType(@PathVariable String documentType) {
        try {
            Long clientId = getCurrentClientId();
            List<DocumentDTO> documents = documentService.getDocumentsByClientAndType(clientId, documentType);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error fetching client documents by type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== STAFF-SPECIFIC ENDPOINTS ==========

    @GetMapping("/staff-documents")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Page<DocumentDTO>> getStaffDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        try {
            Page<DocumentDTO> documents;
            Pageable pageable = PageRequest.of(page, size);

            // Use the comprehensive filter method that handles all combinations
            documents = documentService.getDocumentsByAssignedStaffWithFilters(
                    searchTerm, status, documentType, clientId, taskId, pageable);

            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error fetching staff documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/staff-documents/statistics")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DocumentStatisticsDTO> getStaffDocumentStatistics() {
        try {
            DocumentStatisticsDTO statistics = documentService.getDocumentStatisticsByAssignedStaff();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error fetching staff document statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/staff-documents/upload")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DocumentDTO> uploadStaffDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        try {
            // Staff can upload documents for clients they are assigned to
            DocumentDTO uploadedDocument = documentService.uploadDocument(file, clientId, taskId, documentType);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
        } catch (Exception e) {
            log.error("Error uploading staff document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/staff-documents/{id}/verify")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DocumentDTO> verifyStaffDocument(
            @PathVariable Long id,
            @RequestParam("isVerified") Boolean isVerified,
            @RequestParam(value = "verifiedBy", required = false) String verifiedBy) {
        try {
            DocumentDTO updatedDocument = documentService.updateDocumentVerification(id, isVerified, verifiedBy);
            return ResponseEntity.ok(updatedDocument);
        } catch (Exception e) {
            log.error("Error verifying staff document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/staff-documents/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DocumentDTO> rejectStaffDocument(
            @PathVariable Long id,
            @RequestParam("rejectionReason") String rejectionReason) {
        try {
            DocumentDTO updatedDocument = documentService.rejectDocument(id, rejectionReason);
            return ResponseEntity.ok(updatedDocument);
        } catch (Exception e) {
            log.error("Error rejecting staff document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/staff-documents/{id}/download")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ByteArrayResource> downloadStaffDocument(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            byte[] fileContent = documentService.downloadDocument(id);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading staff document: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}