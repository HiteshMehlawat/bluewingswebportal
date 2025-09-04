package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.ServiceCategoryDTO;
import com.adspeek.authservice.dto.ServiceSubcategoryDTO;
import com.adspeek.authservice.dto.ServiceItemDTO;

import java.util.List;

public interface ServiceHierarchyService {

    // Category operations
    List<ServiceCategoryDTO> getAllCategories();

    List<ServiceCategoryDTO> getFullHierarchy();

    ServiceCategoryDTO getCategoryById(Long id);

    // Subcategory operations
    List<ServiceSubcategoryDTO> getSubcategoriesByCategory(Long categoryId);

    ServiceSubcategoryDTO getSubcategoryById(Long id);

    // Service item operations
    List<ServiceItemDTO> getServiceItemsBySubcategory(Long subcategoryId);

    ServiceItemDTO getServiceItemById(Long id);

    List<ServiceItemDTO> getAllServiceItems();

    // Admin operations
    ServiceCategoryDTO createCategory(ServiceCategoryDTO categoryDTO);

    ServiceSubcategoryDTO createSubcategory(ServiceSubcategoryDTO subcategoryDTO);

    ServiceItemDTO createServiceItem(ServiceItemDTO itemDTO);

    ServiceCategoryDTO updateCategory(Long id, ServiceCategoryDTO categoryDTO);

    ServiceSubcategoryDTO updateSubcategory(Long id, ServiceSubcategoryDTO subcategoryDTO);

    ServiceItemDTO updateServiceItem(Long id, ServiceItemDTO itemDTO);

    void deleteCategory(Long id);

    void deleteSubcategory(Long id);

    void deleteServiceItem(Long id);
}
