package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.DocumentDTO;
import com.adspeek.authservice.dto.DocumentStatisticsDTO;
import com.adspeek.authservice.dto.LatestDocumentUploadDTO;
import com.adspeek.authservice.entity.Client;
import com.adspeek.authservice.entity.Document;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.repository.ClientRepository;
import com.adspeek.authservice.repository.DocumentRepository;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.service.DocumentService;
import com.adspeek.authservice.service.AuditLogService;
import com.adspeek.authservice.service.NotificationService;
import com.adspeek.authservice.service.StaffActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ClientRepository clientRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final StaffActivityService staffActivityService;

    private static final String UPLOAD_DIR = "uploads/documents/";

    @Override
    public DocumentDTO uploadDocument(MultipartFile file, Long clientId, Long taskId, String documentType) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Current user not found");
            }

            // Validate client exists
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            // Validate task exists if provided
            Task task = null;
            if (taskId != null) {
                task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new RuntimeException("Task not found"));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            String filePath = UPLOAD_DIR + uniqueFilename;

            // Save file to disk
            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath);

            // Create document entity
            Document document = Document.builder()
                    .client(client)
                    .task(task)
                    .uploadedBy(currentUser)
                    .fileName(uniqueFilename)
                    .originalFileName(originalFilename)
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .documentType(Document.DocumentType.valueOf(documentType))
                    .status(Document.DocumentStatus.PENDING)
                    .uploadDate(LocalDateTime.now())
                    .createdBy(currentUser)
                    .updatedBy(currentUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Document savedDocument = documentRepository.save(document);

            // Log the activity
            auditLogService.logActivityForCurrentUser(
                    "DOCUMENT_UPLOADED",
                    "DOCUMENT",
                    savedDocument.getId(),
                    null,
                    "{\"fileName\":\"" + savedDocument.getOriginalFileName() + "\",\"documentType\":\""
                            + savedDocument.getDocumentType() + "\"}",
                    null,
                    null);

            // Log staff activity for document upload
            Staff currentStaff = getCurrentStaff();
            if (currentStaff != null) {
                try {
                    staffActivityService.createActivity(
                            com.adspeek.authservice.dto.StaffActivityDTO.builder()
                                    .staffId(currentStaff.getId())
                                    .activityType("DOCUMENT_UPLOADED")
                                    .taskDescription("Uploaded document: " + savedDocument.getOriginalFileName())
                                    .workStatus("COMPLETED")
                                    .logDate(java.time.LocalDate.now())
                                    .taskId(taskId)
                                    .clientId(clientId)
                                    .build());
                } catch (Exception e) {
                    System.err.println("Error logging staff activity for document upload: " + e.getMessage());
                }
            }

            // Send notification for document upload
            // Check if current user is staff and send appropriate notification
            if (currentStaff != null) {
                // Staff uploaded document for client
                notificationService.notifyDocumentUploadedByStaff(savedDocument.getId(), client.getUser().getId(),
                        currentStaff.getId());
            } else {
                // Client uploaded document
                notificationService.notifyDocumentUploaded(savedDocument.getId(), client.getUser().getId());
            }

            // Fetch the document with all related entities to avoid
            // LazyInitializationException
            Document documentWithDetails = documentRepository.findByIdWithDetails(savedDocument.getId())
                    .orElse(savedDocument);

            return toDTO(documentWithDetails);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Override
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toDTO(document);
    }

    @Override
    public List<DocumentDTO> getDocumentsByClient(Long clientId) {
        List<Object[]> documents = documentRepository.findDocumentsWithDetailsByClientId(clientId);
        return documents.stream()
                .map(this::mapToDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> getDocumentsByTask(Long taskId) {
        List<Object[]> documents = documentRepository.findDocumentsWithDetailsByTaskId(taskId);
        return documents.stream()
                .map(this::mapToDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> getAllDocuments() {
        try {
            List<Object[]> documents = documentRepository.findAllDocumentsWithDetails();
            System.out.println("Found " + documents.size() + " documents from repository");

            List<DocumentDTO> result = documents.stream()
                    .map(this::mapToDocumentDTO)
                    .filter(dto -> dto != null) // Filter out null results
                    .collect(Collectors.toList());

            System.out.println("Successfully mapped " + result.size() + " documents to DTOs");
            return result;
        } catch (Exception e) {
            System.err.println("Error in getAllDocuments: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Page<DocumentDTO> getDocumentsWithFilters(Long clientId, Long taskId, String documentType, String isVerified,
            String searchTerm, int page, int size) {
        try {
            // For now, we'll implement basic filtering by getting all documents and
            // filtering in memory
            // In a production environment, you'd want to implement this with native queries
            List<DocumentDTO> allDocuments = getAllDocuments();

            // Apply filters
            List<DocumentDTO> filteredDocuments = allDocuments.stream()
                    .filter(doc -> clientId == null || doc.getClientId().equals(clientId))
                    .filter(doc -> taskId == null || (doc.getTaskId() != null && doc.getTaskId().equals(taskId)))
                    .filter(doc -> documentType == null || doc.getDocumentType().toString().equals(documentType))
                    .filter(doc -> {
                        if (isVerified == null || isVerified.isEmpty())
                            return true;
                        // Handle new status-based filtering
                        if ("VERIFIED".equals(isVerified)) {
                            return "VERIFIED".equals(doc.getStatus().toString());
                        } else if ("PENDING".equals(isVerified)) {
                            return "PENDING".equals(doc.getStatus().toString());
                        } else if ("REJECTED".equals(isVerified)) {
                            return "REJECTED".equals(doc.getStatus().toString());
                        }
                        // Fallback for old boolean-based filtering (for backward compatibility)
                        if ("true".equals(isVerified)) {
                            return "VERIFIED".equals(doc.getStatus().toString());
                        } else if ("false".equals(isVerified)) {
                            return "PENDING".equals(doc.getStatus().toString());
                        }
                        return true;
                    })
                    .filter(doc -> searchTerm == null || searchTerm.isEmpty() ||
                            doc.getOriginalFileName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            doc.getClientName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            doc.getDocumentType().toString().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, filteredDocuments.size());
            List<DocumentDTO> pagedDocuments = filteredDocuments.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(
                    pagedDocuments,
                    org.springframework.data.domain.PageRequest.of(page, size),
                    filteredDocuments.size());
        } catch (Exception e) {
            System.err.println("Error in getDocumentsWithFilters: " + e.getMessage());
            e.printStackTrace();
            return new org.springframework.data.domain.PageImpl<>(new ArrayList<>());
        }
    }

    @Override
    public Page<DocumentDTO> getDocumentsWithPagination(Pageable pageable) {
        // This would need to be implemented with a custom query for pagination
        // For now, returning all documents
        List<DocumentDTO> allDocuments = getAllDocuments();
        // This is a simplified implementation - in production, you'd want proper
        // pagination
        return Page.empty(pageable);
    }

    @Override
    public DocumentDTO updateDocumentVerification(Long documentId, Boolean isVerified, String verifiedBy) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        // If document is currently VERIFIED, set it to PENDING (unverify)
        // If document is currently PENDING or REJECTED, set it to VERIFIED
        if (document.getStatus() == Document.DocumentStatus.VERIFIED) {
            // Unverify: set to PENDING
            document.setStatus(Document.DocumentStatus.PENDING);
            document.setVerifiedBy(null);
            document.setVerifiedAt(null);
        } else {
            // Verify: set to VERIFIED
            document.setStatus(Document.DocumentStatus.VERIFIED);
            document.setVerifiedBy(currentUser);
            document.setVerifiedAt(LocalDateTime.now());
            // Clear rejection fields if document is being verified
            document.setRejectedBy(null);
            document.setRejectedAt(null);
            document.setRejectionReason(null);
        }

        document.setUpdatedBy(currentUser);
        document.setUpdatedAt(LocalDateTime.now());

        Document savedDocument = documentRepository.save(document);

        // Send notification for document verification
        if (savedDocument.getStatus() == Document.DocumentStatus.VERIFIED) {
            notificationService.notifyDocumentVerified(savedDocument.getId(), currentUser.getId());
        }

        // Log staff activity for document verification
        Staff currentStaff = getCurrentStaff();
        if (currentStaff != null) {
            try {
                String activityDescription = savedDocument.getStatus() == Document.DocumentStatus.VERIFIED
                        ? "Verified document: " + savedDocument.getOriginalFileName()
                        : "Unverified document: " + savedDocument.getOriginalFileName();

                staffActivityService.createActivity(
                        com.adspeek.authservice.dto.StaffActivityDTO.builder()
                                .staffId(currentStaff.getId())
                                .activityType("DOCUMENT_VERIFIED")
                                .taskDescription(activityDescription)
                                .workStatus("COMPLETED")
                                .logDate(java.time.LocalDate.now())
                                .taskId(savedDocument.getTask() != null ? savedDocument.getTask().getId() : null)
                                .clientId(savedDocument.getClient().getId())
                                .build());
            } catch (Exception e) {
                System.err.println("Error logging staff activity for document verification: " + e.getMessage());
            }
        }

        // Fetch the document with all related entities to avoid
        // LazyInitializationException
        Document documentWithDetails = documentRepository.findByIdWithDetails(documentId)
                .orElse(savedDocument);

        return toDTO(documentWithDetails);
    }

    @Override
    public DocumentDTO rejectDocument(Long documentId, String rejectionReason) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        // If document is currently REJECTED, set it to PENDING (unreject)
        // If document is currently PENDING or VERIFIED, set it to REJECTED
        if (document.getStatus() == Document.DocumentStatus.REJECTED) {
            // Unreject: set to PENDING
            document.setStatus(Document.DocumentStatus.PENDING);
            document.setRejectedBy(null);
            document.setRejectedAt(null);
            document.setRejectionReason(null);
        } else {
            // Reject: set to REJECTED
            document.setStatus(Document.DocumentStatus.REJECTED);
            document.setRejectedBy(currentUser);
            document.setRejectedAt(LocalDateTime.now());
            document.setRejectionReason(rejectionReason);
            // Clear verification fields if document is being rejected
            document.setVerifiedBy(null);
            document.setVerifiedAt(null);
        }

        document.setUpdatedBy(currentUser);
        document.setUpdatedAt(LocalDateTime.now());

        Document savedDocument = documentRepository.save(document);

        // Send notification for document rejection
        if (savedDocument.getStatus() == Document.DocumentStatus.REJECTED) {
            notificationService.notifyDocumentRejected(savedDocument.getId(), currentUser.getId(), rejectionReason);
        }

        // Log staff activity for document rejection
        Staff currentStaff = getCurrentStaff();
        if (currentStaff != null) {
            try {
                String activityDescription = savedDocument.getStatus() == Document.DocumentStatus.REJECTED
                        ? "Rejected document: " + savedDocument.getOriginalFileName() + " - Reason: " + rejectionReason
                        : "Unrejected document: " + savedDocument.getOriginalFileName();

                staffActivityService.createActivity(
                        com.adspeek.authservice.dto.StaffActivityDTO.builder()
                                .staffId(currentStaff.getId())
                                .activityType("DOCUMENT_REJECTED")
                                .taskDescription(activityDescription)
                                .workStatus("COMPLETED")
                                .logDate(java.time.LocalDate.now())
                                .taskId(savedDocument.getTask() != null ? savedDocument.getTask().getId() : null)
                                .clientId(savedDocument.getClient().getId())
                                .build());
            } catch (Exception e) {
                System.err.println("Error logging staff activity for document rejection: " + e.getMessage());
            }
        }

        // Fetch the document with all related entities to avoid
        // LazyInitializationException
        Document documentWithDetails = documentRepository.findByIdWithDetails(documentId)
                .orElse(savedDocument);

        return toDTO(documentWithDetails);
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Delete file from disk
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + document.getFilePath());
        }

        documentRepository.delete(document);
    }

    @Override
    public byte[] downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document file", e);
        }
    }

    @Override
    public DocumentStatisticsDTO getDocumentStatistics() {
        // Get current date for today's calculations
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // Get all documents for calculations
        List<Document> allDocuments = documentRepository.findAll();

        // Calculate documents uploaded today
        long documentsUploadedToday = allDocuments.stream()
                .filter(doc -> doc.getUploadDate() != null &&
                        doc.getUploadDate().toLocalDate().equals(today))
                .count();

        // Calculate documents uploaded this week
        long documentsUploadedThisWeek = allDocuments.stream()
                .filter(doc -> doc.getUploadDate() != null &&
                        !doc.getUploadDate().toLocalDate().isBefore(startOfWeek))
                .count();

        // Calculate documents uploaded this month
        long documentsUploadedThisMonth = allDocuments.stream()
                .filter(doc -> doc.getUploadDate() != null &&
                        !doc.getUploadDate().toLocalDate().isBefore(startOfMonth))
                .count();

        // Calculate total file size
        long totalFileSize = allDocuments.stream()
                .mapToLong(Document::getFileSize)
                .sum();

        // Calculate average file size
        double averageFileSize = allDocuments.isEmpty() ? 0 : (double) totalFileSize / allDocuments.size();

        return DocumentStatisticsDTO.builder()
                .totalDocuments((long) allDocuments.size())
                .verifiedDocuments(documentRepository.countByStatus(Document.DocumentStatus.VERIFIED))
                .pendingDocuments(documentRepository.countByStatus(Document.DocumentStatus.PENDING))
                .rejectedDocuments(documentRepository.countByStatus(Document.DocumentStatus.REJECTED))
                .documentsUploadedToday(documentsUploadedToday)
                .documentsUploadedThisWeek(documentsUploadedThisWeek)
                .documentsUploadedThisMonth(documentsUploadedThisMonth)
                .totalFileSize(totalFileSize)
                .averageFileSize((long) averageFileSize)
                .build();
    }

    @Override
    public List<DocumentDTO> getDocumentsByType(String documentType) {
        Document.DocumentType type = Document.DocumentType.valueOf(documentType);
        List<Document> documents = documentRepository.findByDocumentType(type);
        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> getDocumentsByVerificationStatus(Boolean isVerified) {
        Document.DocumentStatus status = isVerified ? Document.DocumentStatus.VERIFIED
                : Document.DocumentStatus.PENDING;
        List<Document> documents = documentRepository.findByStatus(status);
        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> searchDocuments(String searchTerm) {
        // This would need to be implemented with a custom search query
        // For now, returning all documents
        return getAllDocuments();
    }

    @Override
    public Long getDocumentCountByClient(Long clientId) {
        return documentRepository.countByClientId(clientId);
    }

    @Override
    public Long getVerifiedDocumentCountByClient(Long clientId) {
        return documentRepository.countByClientIdAndStatus(clientId, Document.DocumentStatus.VERIFIED);
    }

    @Override
    public Page<LatestDocumentUploadDTO> getLatestUploads(Pageable pageable) {
        Page<Object[]> page = documentRepository.findLatestUploads(pageable);
        return page.map(row -> {
            String status = row[7] != null ? row[7].toString() : null;
            Boolean isVerified = null;
            if (status != null) {
                isVerified = "VERIFIED".equals(status);
            }

            return LatestDocumentUploadDTO.builder()
                    .documentId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .documentName((String) row[1])
                    .originalFileName((String) row[2])
                    .uploadDateTime(row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime() : null)
                    .fileType((String) row[4])
                    .fileSize(row[5] != null ? ((Number) row[5]).longValue() : null)
                    .documentType(row[6] != null ? row[6].toString() : null)
                    .isVerified(isVerified)
                    .status(status)
                    .uploadedById(row[8] != null ? ((Number) row[8]).longValue() : null)
                    .uploadedByName((String) row[9])
                    .uploadedByRole(row[10] != null ? row[10].toString() : null)
                    .clientId(row[11] != null ? ((Number) row[11]).longValue() : null)
                    .clientName((String) row[12])
                    .taskName((String) row[13])
                    .verifiedById(row[14] != null ? ((Number) row[14]).longValue() : null)
                    .verifiedByName((String) row[15])
                    .verifiedAt(row[16] != null ? ((java.sql.Timestamp) row[16]).toLocalDateTime() : null)
                    .rejectedById(row[17] != null ? ((Number) row[17]).longValue() : null)
                    .rejectedByName((String) row[18])
                    .rejectedAt(row[19] != null ? ((java.sql.Timestamp) row[19]).toLocalDateTime() : null)
                    .rejectionReason((String) row[20])
                    .build();
        });
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private DocumentDTO toDTO(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .taskId(document.getTask() != null ? document.getTask().getId() : null)
                .clientId(document.getClient().getId())
                .clientName(document.getClient().getCompanyName())
                .uploadedById(document.getUploadedBy().getId())
                .uploadedByName(document.getUploadedBy().getFirstName() + " " + document.getUploadedBy().getLastName())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .fileType(document.getFileType())
                .documentType(document.getDocumentType())
                .status(document.getStatus())
                .verifiedById(document.getVerifiedBy() != null ? document.getVerifiedBy().getId() : null)
                .verifiedByName(document.getVerifiedBy() != null
                        ? document.getVerifiedBy().getFirstName() + " " + document.getVerifiedBy().getLastName()
                        : null)
                .verifiedAt(document.getVerifiedAt())
                .rejectedById(document.getRejectedBy() != null ? document.getRejectedBy().getId() : null)
                .rejectedByName(document.getRejectedBy() != null
                        ? document.getRejectedBy().getFirstName() + " " + document.getRejectedBy().getLastName()
                        : null)
                .rejectedAt(document.getRejectedAt())
                .rejectionReason(document.getRejectionReason())
                .uploadDate(document.getUploadDate())
                .createdById(document.getCreatedBy() != null ? document.getCreatedBy().getId() : null)
                .createdByName(document.getCreatedBy() != null
                        ? document.getCreatedBy().getFirstName() + " " + document.getCreatedBy().getLastName()
                        : null)
                .updatedById(document.getUpdatedBy() != null ? document.getUpdatedBy().getId() : null)
                .updatedByName(document.getUpdatedBy() != null
                        ? document.getUpdatedBy().getFirstName() + " " + document.getUpdatedBy().getLastName()
                        : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private DocumentDTO mapToDocumentDTO(Object[] row) {
        if (row == null || row.length < 27) {
            System.err.println("Row is null or has insufficient columns. Length: " + (row != null ? row.length : 0));
            return null;
        }

        try {
            return DocumentDTO.builder()
                    .id(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskId(row[1] != null ? ((Number) row[1]).longValue() : null)
                    .clientId(row[2] != null ? ((Number) row[2]).longValue() : null)
                    .clientName(row[3] != null ? (String) row[3] : "N/A")
                    .uploadedById(row[4] != null ? ((Number) row[4]).longValue() : null)
                    .uploadedByName(row[5] != null ? (String) row[5] : "N/A")
                    .fileName(row[6] != null ? (String) row[6] : "")
                    .originalFileName(row[7] != null ? (String) row[7] : "")
                    .filePath(row[8] != null ? (String) row[8] : "")
                    .fileSize(row[9] != null ? ((Number) row[9]).longValue() : 0L)
                    .fileType(row[10] != null ? (String) row[10] : "")
                    .documentType(row[11] != null ? Document.DocumentType.valueOf((String) row[11])
                            : Document.DocumentType.OTHER)
                    .status(row[12] != null ? Document.DocumentStatus.valueOf((String) row[12])
                            : Document.DocumentStatus.PENDING)
                    .verifiedById(row[13] != null ? ((Number) row[13]).longValue() : null)
                    .verifiedByName(row[14] != null ? (String) row[14] : null)
                    .verifiedAt(row[15] != null ? ((java.sql.Timestamp) row[15]).toLocalDateTime() : null)
                    .rejectedById(row[16] != null ? ((Number) row[16]).longValue() : null)
                    .rejectedByName(row[17] != null ? (String) row[17] : null)
                    .rejectedAt(row[18] != null ? ((java.sql.Timestamp) row[18]).toLocalDateTime() : null)
                    .rejectionReason(row[19] != null ? (String) row[19] : null)
                    .uploadDate(row[20] != null ? ((java.sql.Timestamp) row[20]).toLocalDateTime() : null)
                    .createdById(row[21] != null ? ((Number) row[21]).longValue() : null)
                    .createdByName(row[22] != null ? (String) row[22] : null)
                    .updatedById(row[23] != null ? ((Number) row[23]).longValue() : null)
                    .updatedByName(row[24] != null ? (String) row[24] : null)
                    .createdAt(row[25] != null ? ((java.sql.Timestamp) row[25]).toLocalDateTime() : null)
                    .updatedAt(row[26] != null ? ((java.sql.Timestamp) row[26]).toLocalDateTime() : null)
                    .build();
        } catch (Exception e) {
            System.err.println("Error mapping document row: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Page<DocumentDTO> getDocumentsByClientWithPagination(Long clientId, Pageable pageable) {
        Page<Object[]> page = documentRepository.findDocumentsWithDetailsByClientIdWithPagination(clientId, pageable);
        return page.map(this::mapToDocumentDTO);
    }

    @Override
    public Page<DocumentDTO> getDocumentsByClientWithPaginationAndFilters(Long clientId, Pageable pageable,
            String documentType, String status) {
        Page<Object[]> page = documentRepository.findDocumentsWithDetailsByClientIdWithPaginationAndFilters(clientId,
                pageable, documentType, status);
        return page.map(this::mapToDocumentDTO);
    }

    @Override
    public DocumentStatisticsDTO getDocumentStatisticsByClient(Long clientId) {
        List<Document> clientDocuments = documentRepository.findByClientId(clientId);

        long totalDocuments = clientDocuments.size();
        long verifiedDocuments = clientDocuments.stream()
                .filter(doc -> doc.getStatus() == Document.DocumentStatus.VERIFIED)
                .count();
        long pendingDocuments = clientDocuments.stream()
                .filter(doc -> doc.getStatus() == Document.DocumentStatus.PENDING)
                .count();
        long rejectedDocuments = clientDocuments.stream()
                .filter(doc -> doc.getStatus() == Document.DocumentStatus.REJECTED)
                .count();

        return DocumentStatisticsDTO.builder()
                .totalDocuments(totalDocuments)
                .verifiedDocuments(verifiedDocuments)
                .pendingDocuments(pendingDocuments)
                .rejectedDocuments(rejectedDocuments)
                .build();
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadDocumentWithResponse(
            Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            Path filePath = Paths.get(document.getFilePath());
            byte[] fileContent = Files.readAllBytes(filePath);

            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(
                    fileContent);

            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document file", e);
        }
    }

    @Override
    public List<DocumentDTO> searchDocumentsByClient(Long clientId, String searchTerm) {
        List<Document> documents = documentRepository.findByClientIdAndOriginalFileNameContainingIgnoreCase(clientId,
                searchTerm);
        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> getDocumentsByClientAndType(Long clientId, String documentType) {
        try {
            Document.DocumentType type = Document.DocumentType.valueOf(documentType.toUpperCase());
            List<Document> documents = documentRepository.findByClientIdAndDocumentType(clientId, type);
            return documents.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid document type: " + documentType);
            return new ArrayList<>();
        }
    }

    // ========== STAFF-SPECIFIC METHODS ==========

    @Override
    public Page<DocumentDTO> getDocumentsByAssignedStaffWithFilters(String searchTerm, String status,
            String documentType, Long clientId, Long taskId, Pageable pageable) {
        Staff currentStaff = getCurrentStaff();
        if (currentStaff == null) {
            return Page.empty(pageable);
        }

        Page<Object[]> page = documentRepository.findDocumentsByAssignedStaffWithFilters(currentStaff.getId(),
                searchTerm, status, documentType, clientId, taskId, pageable);
        return page.map(this::mapToDocumentDTO);
    }

    @Override
    public DocumentStatisticsDTO getDocumentStatisticsByAssignedStaff() {
        Staff currentStaff = getCurrentStaff();
        if (currentStaff == null) {
            return DocumentStatisticsDTO.builder()
                    .totalDocuments(0L)
                    .verifiedDocuments(0L)
                    .pendingDocuments(0L)
                    .rejectedDocuments(0L)
                    .build();
        }

        // Get all documents for tasks assigned to this staff member
        Page<Object[]> allDocuments = documentRepository.findDocumentsByAssignedStaffWithFilters(currentStaff.getId(),
                null, null, null, null, null, Pageable.unpaged());

        long totalDocuments = allDocuments.getTotalElements();
        long verifiedDocuments = allDocuments.getContent().stream()
                .filter(row -> "VERIFIED".equals(row[12])) // status is at index 12
                .count();
        long pendingDocuments = allDocuments.getContent().stream()
                .filter(row -> "PENDING".equals(row[12])) // status is at index 12
                .count();
        long rejectedDocuments = allDocuments.getContent().stream()
                .filter(row -> "REJECTED".equals(row[12])) // status is at index 12
                .count();

        return DocumentStatisticsDTO.builder()
                .totalDocuments(totalDocuments)
                .verifiedDocuments(verifiedDocuments)
                .pendingDocuments(pendingDocuments)
                .rejectedDocuments(rejectedDocuments)
                .build();
    }

    // Helper method to get current staff member
    private Staff getCurrentStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == User.Role.STAFF) {
                return staffRepository.findByUserId(user.getId()).orElse(null);
            }
        }
        return null;
    }
}