package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.ServiceCategoryDTO;
import com.adspeek.authservice.dto.ServiceSubcategoryDTO;
import com.adspeek.authservice.dto.ServiceItemDTO;
import com.adspeek.authservice.service.ServiceHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ServiceHierarchyController {

    private final ServiceHierarchyService serviceHierarchyService;

    // Public endpoints for service selection
    @GetMapping("/hierarchy")
    public ResponseEntity<List<ServiceCategoryDTO>> getFullHierarchy() {
        try {
            List<ServiceCategoryDTO> hierarchy = serviceHierarchyService.getFullHierarchy();
            return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ServiceCategoryDTO>> getAllCategories() {
        try {
            List<ServiceCategoryDTO> categories = serviceHierarchyService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<List<ServiceSubcategoryDTO>> getSubcategoriesByCategory(@PathVariable Long categoryId) {
        try {
            List<ServiceSubcategoryDTO> subcategories = serviceHierarchyService.getSubcategoriesByCategory(categoryId);
            return ResponseEntity.ok(subcategories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/subcategories/{subcategoryId}/items")
    public ResponseEntity<List<ServiceItemDTO>> getServiceItemsBySubcategory(@PathVariable Long subcategoryId) {
        try {
            List<ServiceItemDTO> items = serviceHierarchyService.getServiceItemsBySubcategory(subcategoryId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<ServiceItemDTO>> getAllServiceItems() {
        try {
            List<ServiceItemDTO> items = serviceHierarchyService.getAllServiceItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ServiceItemDTO> getServiceItemById(@PathVariable Long itemId) {
        try {
            ServiceItemDTO item = serviceHierarchyService.getServiceItemById(itemId);
            if (item != null) {
                return ResponseEntity.ok(item);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Admin endpoints for managing services
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCategoryDTO> createCategory(@RequestBody ServiceCategoryDTO categoryDTO) {
        try {
            ServiceCategoryDTO created = serviceHierarchyService.createCategory(categoryDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/subcategories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceSubcategoryDTO> createSubcategory(@RequestBody ServiceSubcategoryDTO subcategoryDTO) {
        try {
            ServiceSubcategoryDTO created = serviceHierarchyService.createSubcategory(subcategoryDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceItemDTO> createServiceItem(@RequestBody ServiceItemDTO itemDTO) {
        try {
            ServiceItemDTO created = serviceHierarchyService.createServiceItem(itemDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCategoryDTO> updateCategory(@PathVariable Long id,
            @RequestBody ServiceCategoryDTO categoryDTO) {
        try {
            ServiceCategoryDTO updated = serviceHierarchyService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/subcategories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceSubcategoryDTO> updateSubcategory(@PathVariable Long id,
            @RequestBody ServiceSubcategoryDTO subcategoryDTO) {
        try {
            ServiceSubcategoryDTO updated = serviceHierarchyService.updateSubcategory(id, subcategoryDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceItemDTO> updateServiceItem(@PathVariable Long id,
            @RequestBody ServiceItemDTO itemDTO) {
        try {
            ServiceItemDTO updated = serviceHierarchyService.updateServiceItem(id, itemDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            serviceHierarchyService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/subcategories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        try {
            serviceHierarchyService.deleteSubcategory(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteServiceItem(@PathVariable Long id) {
        try {
            serviceHierarchyService.deleteServiceItem(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
