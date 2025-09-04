package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.LeadCommentDTO;
import com.adspeek.authservice.service.LeadCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lead-comments")
@RequiredArgsConstructor
@Slf4j
public class LeadCommentController {

    private final LeadCommentService leadCommentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createComment(@RequestBody LeadCommentDTO commentDTO) {
        try {
            LeadCommentDTO comment = leadCommentService.createComment(commentDTO);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error creating lead comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating comment: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getCommentById(@PathVariable Long id) {
        try {
            LeadCommentDTO comment = leadCommentService.getCommentById(id);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error fetching lead comment: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getCommentsByLeadId(@PathVariable Long leadId) {
        try {
            List<LeadCommentDTO> comments = leadCommentService.getCommentsByLeadId(leadId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching lead comments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lead/{leadId}/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getCommentsByLeadIdPaginated(
            @PathVariable Long leadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeadCommentDTO> comments = leadCommentService.getCommentsByLeadId(leadId, page, size);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching lead comments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody LeadCommentDTO commentDTO) {
        try {
            LeadCommentDTO comment = leadCommentService.updateComment(id, commentDTO);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error updating lead comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating comment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            leadCommentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting lead comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting comment: " + e.getMessage());
        }
    }
}
