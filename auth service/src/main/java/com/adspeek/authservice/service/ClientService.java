package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.ClientDTO;
import com.adspeek.authservice.dto.ClientDetailDTO;
import com.adspeek.authservice.dto.ClientActivityDTO;
import com.adspeek.authservice.dto.ClientDashboardStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClientService {
    Page<ClientDTO> getAllClients(Pageable pageable, String search, String statusFilter);

    Optional<ClientDetailDTO> getClientById(Long id);

    ClientDTO createClient(ClientDTO clientDTO);

    ClientDTO updateClient(Long id, ClientDTO clientDTO);

    void deleteClient(Long id);

    void assignStaffToClient(Long clientId, Long staffId);

    Page<Object[]> getClientDocuments(Long clientId, Pageable pageable);

    Page<ClientActivityDTO> getClientActivity(Long clientId, Pageable pageable);

    Page<Object[]> getClientTasks(Long clientId, Pageable pageable);

    Map<String, Object> getClientStats();

    void toggleClientStatus(Long clientId);

    // Client Dashboard specific methods
    ClientDashboardStatsDTO getClientDashboardStats(Long clientId);

    List<ClientActivityDTO> getRecentClientActivities(Long clientId, int limit);

    Long getClientIdByUserId(Long userId);

    // Client profile update method - only updates provided fields
    ClientDTO updateClientProfile(Long clientId, Map<String, Object> profileUpdates);
}