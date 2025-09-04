package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.*;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.ClientService;
import com.adspeek.authservice.service.DocumentService;
import com.adspeek.authservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import com.adspeek.authservice.entity.Task;

@RestController
@RequestMapping("/api/clients/me")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('CLIENT')")
public class ClientDashboardController {

    private final ClientService clientService;
    private final TaskService taskService;
    private final DocumentService documentService;
    private final UserRepository userRepository;

    /**
     * Get current client's dashboard statistics
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ClientDashboardStatsDTO> getDashboardStats() {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            ClientDashboardStatsDTO stats = clientService.getClientDashboardStats(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's tasks with pagination
     */
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskDetailDTO>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            Page<TaskDetailDTO> tasks = taskService.getTasksByClientWithDetails(clientId, PageRequest.of(page, size));
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's tasks with filters
     */
    @GetMapping("/tasks/filter")
    public ResponseEntity<Page<TaskDetailDTO>> getMyTasksWithFilters(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            // Convert string parameters to enums
            Task.Status statusEnum = null;
            Task.TaskType taskTypeEnum = null;
            Task.Priority priorityEnum = null;

            if (status != null && !status.isEmpty()) {
                try {
                    statusEnum = Task.Status.valueOf(status);
                } catch (IllegalArgumentException e) {
                    // Invalid status value - will be ignored
                }
            }

            if (taskType != null && !taskType.isEmpty()) {
                try {
                    taskTypeEnum = Task.TaskType.valueOf(taskType);
                } catch (IllegalArgumentException e) {
                    // Invalid taskType value - will be ignored
                }
            }

            if (priority != null && !priority.isEmpty()) {
                try {
                    priorityEnum = Task.Priority.valueOf(priority);
                } catch (IllegalArgumentException e) {
                    // Invalid priority value - will be ignored
                }
            }

            // Always pass the current client's ID to ensure only their tasks are returned
            Page<TaskDetailDTO> tasks = taskService.getTasksWithFiltersAndDetails(
                    statusEnum,
                    taskTypeEnum,
                    priorityEnum,
                    null, // assignedStaffId - not needed for client filtering
                    clientId, // Always use current client's ID
                    searchTerm,
                    PageRequest.of(page, size));

            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's documents with pagination
     */
    @GetMapping("/documents")
    public ResponseEntity<Page<DocumentDTO>> getMyDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            Page<DocumentDTO> documents = documentService.getDocumentsByClientWithPagination(clientId,
                    PageRequest.of(page, size));
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's recent activities
     */
    @GetMapping("/activities")
    public ResponseEntity<List<ClientActivityDTO>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<ClientActivityDTO> activities = clientService.getRecentClientActivities(clientId, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's upcoming deadlines
     */
    @GetMapping("/deadlines")
    public ResponseEntity<List<UpcomingDeadlineDTO>> getUpcomingDeadlines() {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<UpcomingDeadlineDTO> deadlines = taskService.getUpcomingDeadlinesByClient(clientId);
            return ResponseEntity.ok(deadlines);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientDetailDTO> getMyProfile() {
        try {
            Long clientId = getCurrentClientId();

            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<ClientDetailDTO> profile = clientService.getClientById(clientId);

            return profile.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update current client's profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientDTO> updateMyProfile(@RequestBody Map<String, Object> profileUpdates) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            // Use the new method that only updates provided fields
            ClientDTO updated = clientService.updateClientProfile(clientId, profileUpdates);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Upload document for current client
     */
    @PostMapping("/documents/upload")
    public ResponseEntity<DocumentDTO> uploadMyDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam("documentType") String documentType) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            DocumentDTO uploadedDocument = documentService.uploadDocument(file, clientId, taskId, documentType);
            return ResponseEntity.ok(uploadedDocument);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download document for current client
     */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadMyDocument(
            @PathVariable Long documentId) {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verify the document belongs to the current client
            DocumentDTO document = documentService.getDocumentById(documentId);
            if (document == null || !document.getClientId().equals(clientId)) {
                return ResponseEntity.notFound().build();
            }

            return documentService.downloadDocumentWithResponse(documentId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's task statistics
     */
    @GetMapping("/task-statistics")
    public ResponseEntity<TaskStatisticsDTO> getMyTaskStatistics() {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            TaskStatisticsDTO stats = taskService.getTaskStatisticsByClient(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's document statistics
     */
    @GetMapping("/document-statistics")
    public ResponseEntity<DocumentStatisticsDTO> getMyDocumentStatistics() {
        try {
            Long clientId = getCurrentClientId();
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }

            DocumentStatisticsDTO stats = documentService.getDocumentStatisticsByClient(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper method to get current client ID from authentication
     */
    private Long getCurrentClientId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return null;
            }

            User user = userOpt.get();

            if (user.getRole() != User.Role.CLIENT) {
                return null;
            }

            // Get client ID from the user
            return clientService.getClientIdByUserId(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
