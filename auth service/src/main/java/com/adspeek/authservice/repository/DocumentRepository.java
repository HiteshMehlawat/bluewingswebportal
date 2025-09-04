package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find document by ID with all related entities loaded
    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.client " +
            "LEFT JOIN FETCH d.uploadedBy " +
            "LEFT JOIN FETCH d.verifiedBy " +
            "LEFT JOIN FETCH d.rejectedBy " +
            "LEFT JOIN FETCH d.createdBy " +
            "LEFT JOIN FETCH d.updatedBy " +
            "LEFT JOIN FETCH d.task " +
            "WHERE d.id = :id")
    java.util.Optional<Document> findByIdWithDetails(@Param("id") Long id);

    // Find documents by client
    Page<Document> findByClientId(Long clientId, Pageable pageable);

    // Find documents by task
    List<Document> findByTaskId(Long taskId);

    // Find documents by document type
    List<Document> findByDocumentType(Document.DocumentType documentType);

    // Find documents by status
    List<Document> findByStatus(Document.DocumentStatus status);

    // Find documents by client and status
    List<Document> findByClientIdAndStatus(Long clientId, Document.DocumentStatus status);

    // Find documents by uploaded by user
    List<Document> findByUploadedById(Long uploadedById);

    // Find documents by client and document type
    List<Document> findByClientIdAndDocumentType(Long clientId, Document.DocumentType documentType);

    // Find documents by client and search term (filename)
    List<Document> findByClientIdAndOriginalFileNameContainingIgnoreCase(Long clientId, String searchTerm);

    // Count documents by client
    Long countByClientId(Long clientId);

    // Count documents by status
    Long countByStatus(Document.DocumentStatus status);

    // Count documents by client and status
    Long countByClientIdAndStatus(Long clientId, Document.DocumentStatus status);

    @Query(value = """
              SELECT
              d.id,
              d.file_name,
              d.original_file_name,
              d.upload_date,
              d.file_type,
              d.file_size,
              d.document_type,
              d.status,
              u.id as uploaded_by_id,
              COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploaded_by_name,
              u.role as uploaded_by_role,
              c.id as client_id,
              COALESCE(c.company_name, 'N/A') as client_name,
              COALESCE(t.title, 'N/A') as task_name,
              v.id as verified_by_id,
              COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verified_by_name,
              d.verified_at,
              r.id as rejected_by_id,
              COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejected_by_name,
              d.rejected_at,
              d.rejection_reason
            FROM documents d
            JOIN users u ON d.uploaded_by = u.id
            JOIN clients c ON d.client_id = c.id
            LEFT JOIN tasks t ON d.task_id = t.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            ORDER BY d.upload_date DESC
            """, countQuery = "SELECT COUNT(*) FROM documents", nativeQuery = true)
    Page<Object[]> findLatestUploads(Pageable pageable);

    // Find documents with client and user information using native query
    @Query(value = """
            SELECT
                d.id,
                d.task_id,
                d.client_id,
                COALESCE(c.company_name, 'N/A') as clientName,
                d.uploaded_by,
                COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                d.file_name,
                d.original_file_name,
                d.file_path,
                d.file_size,
                d.file_type,
                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            WHERE d.client_id = :clientId
            ORDER BY d.upload_date DESC
            """, nativeQuery = true)
    List<Object[]> findDocumentsWithDetailsByClientId(@Param("clientId") Long clientId);

    // Find documents with details by task ID
    @Query(value = """
            SELECT
                d.id,
                d.task_id,
                d.client_id,
                COALESCE(c.company_name, 'N/A') as clientName,
                d.uploaded_by,
                COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                d.file_name,
                d.original_file_name,
                d.file_path,
                d.file_size,
                d.file_type,
                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            WHERE d.task_id = :taskId
            ORDER BY d.upload_date DESC
            """, nativeQuery = true)
    List<Object[]> findDocumentsWithDetailsByTaskId(@Param("taskId") Long taskId);

    // Find all documents with details for admin view
    @Query(value = """
                  SELECT
                      d.id,
                      d.task_id,
                      d.client_id,
                      COALESCE(c.company_name, 'N/A') as clientName,
                      d.uploaded_by,
                      COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                      d.file_name,
                      d.original_file_name,
                      d.file_path,
                      d.file_size,
                      d.file_type,
                                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            ORDER BY d.upload_date DESC
                  """, nativeQuery = true)
    List<Object[]> findAllDocumentsWithDetails();

    // Client Dashboard specific methods
    @Query(value = """
            SELECT
                d.id,
                d.task_id,
                d.client_id,
                COALESCE(c.company_name, 'N/A') as clientName,
                d.uploaded_by,
                COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                d.file_name,
                d.original_file_name,
                d.file_path,
                d.file_size,
                d.file_type,
                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            WHERE d.client_id = :clientId
            ORDER BY d.upload_date DESC
            """, countQuery = "SELECT COUNT(*) FROM documents d WHERE d.client_id = :clientId", nativeQuery = true)
    Page<Object[]> findDocumentsWithDetailsByClientIdWithPagination(@Param("clientId") Long clientId,
            Pageable pageable);

    // Find documents by client ID (simple list)
    List<Document> findByClientId(Long clientId);

    // Find documents by client ID with filters
    @Query(value = """
            SELECT
                d.id,
                d.task_id,
                d.client_id,
                COALESCE(c.company_name, 'N/A') as clientName,
                d.uploaded_by,
                COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                d.file_name,
                d.original_file_name,
                d.file_path,
                d.file_size,
                d.file_type,
                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            WHERE d.client_id = :clientId
            AND (:documentType IS NULL OR d.document_type = :documentType)
            AND (:status IS NULL OR d.status = :status)
            ORDER BY d.upload_date DESC
            """, countQuery = """
            SELECT COUNT(*) FROM documents d
            WHERE d.client_id = :clientId
            AND (:documentType IS NULL OR d.document_type = :documentType)
            AND (:status IS NULL OR d.status = :status)
            """, nativeQuery = true)
    Page<Object[]> findDocumentsWithDetailsByClientIdWithPaginationAndFilters(
            @Param("clientId") Long clientId,
            Pageable pageable,
            @Param("documentType") String documentType,
            @Param("status") String status);

    // ========== STAFF-SPECIFIC METHODS ==========

    // Get documents for tasks assigned to staff with comprehensive filters
    // (including client and task)
    @Query(value = """
            SELECT
                d.id,
                d.task_id,
                d.client_id,
                COALESCE(c.company_name, 'N/A') as clientName,
                d.uploaded_by,
                COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'N/A') as uploadedByName,
                d.file_name,
                d.original_file_name,
                d.file_path,
                d.file_size,
                d.file_type,
                d.document_type,
                d.status,
                d.verified_by,
                COALESCE(CONCAT(v.first_name, ' ', v.last_name), 'N/A') as verifiedByName,
                d.verified_at,
                d.rejected_by,
                COALESCE(CONCAT(r.first_name, ' ', r.last_name), 'N/A') as rejectedByName,
                d.rejected_at,
                d.rejection_reason,
                d.upload_date,
                d.created_by,
                COALESCE(CONCAT(cr.first_name, ' ', cr.last_name), 'N/A') as createdByName,
                d.updated_by,
                COALESCE(CONCAT(up.first_name, ' ', up.last_name), 'N/A') as updatedByName,
                d.created_at,
                d.updated_at,
                t.title as taskTitle,
                t.status as taskStatus,
                t.due_date as taskDueDate
            FROM documents d
            LEFT JOIN clients c ON d.client_id = c.id
            LEFT JOIN users u ON d.uploaded_by = u.id
            LEFT JOIN users v ON d.verified_by = v.id
            LEFT JOIN users r ON d.rejected_by = r.id
            LEFT JOIN users cr ON d.created_by = cr.id
            LEFT JOIN users up ON d.updated_by = up.id
            INNER JOIN tasks t ON d.task_id = t.id
            WHERE t.assigned_staff_id = :staffId
            AND d.task_id IS NOT NULL
            AND (:searchTerm IS NULL OR (LOWER(d.original_file_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                 OR LOWER(c.company_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                 OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))
            AND (:status IS NULL OR d.status = :status)
            AND (:documentType IS NULL OR d.document_type = :documentType)
            AND (:clientId IS NULL OR d.client_id = :clientId)
            AND (:taskId IS NULL OR d.task_id = :taskId)
            ORDER BY d.upload_date DESC
            """, countQuery = """
            SELECT COUNT(*) FROM documents d
            INNER JOIN tasks t ON d.task_id = t.id
            LEFT JOIN clients c ON d.client_id = c.id
            WHERE t.assigned_staff_id = :staffId
            AND d.task_id IS NOT NULL
            AND (:searchTerm IS NULL OR (LOWER(d.original_file_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                 OR LOWER(c.company_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                 OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))
            AND (:status IS NULL OR d.status = :status)
            AND (:documentType IS NULL OR d.document_type = :documentType)
            AND (:clientId IS NULL OR d.client_id = :clientId)
            AND (:taskId IS NULL OR d.task_id = :taskId)
            """, nativeQuery = true)
    Page<Object[]> findDocumentsByAssignedStaffWithFilters(@Param("staffId") Long staffId,
            @Param("searchTerm") String searchTerm, @Param("status") String status,
            @Param("documentType") String documentType, @Param("clientId") Long clientId,
            @Param("taskId") Long taskId, Pageable pageable);
}