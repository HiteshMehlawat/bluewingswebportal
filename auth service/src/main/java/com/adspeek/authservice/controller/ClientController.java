package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.ClientDTO;
import com.adspeek.authservice.dto.ClientDetailDTO;
import com.adspeek.authservice.dto.ClientActivityDTO;
import com.adspeek.authservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public Page<ClientDTO> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {
        return clientService.getAllClients(PageRequest.of(page, size), search, statusFilter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDetailDTO> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        try {
            ClientDTO created = clientService.createClient(clientDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        ClientDTO updated = clientService.updateClient(id, clientDTO);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{clientId}/assign-staff/{staffId}")
    public ResponseEntity<Void> assignStaffToClient(@PathVariable Long clientId, @PathVariable Long staffId) {
        clientService.assignStaffToClient(clientId, staffId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clientId}/toggle-status")
    public ResponseEntity<Void> toggleClientStatus(@PathVariable Long clientId) {
        try {
            clientService.toggleClientStatus(clientId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{clientId}/documents")
    public Page<Object[]> getClientDocuments(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientService.getClientDocuments(clientId, PageRequest.of(page, size));
    }

    @GetMapping("/{clientId}/activity")
    public Page<ClientActivityDTO> getClientActivity(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientService.getClientActivity(clientId, PageRequest.of(page, size));
    }

    @GetMapping("/{clientId}/tasks")
    public Page<Object[]> getClientTasks(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientService.getClientTasks(clientId, PageRequest.of(page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getClientStats() {
        return ResponseEntity.ok(clientService.getClientStats());
    }
}