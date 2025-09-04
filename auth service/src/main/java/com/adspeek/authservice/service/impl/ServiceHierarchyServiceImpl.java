package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.ServiceCategoryDTO;
import com.adspeek.authservice.dto.ServiceSubcategoryDTO;
import com.adspeek.authservice.dto.ServiceItemDTO;
import com.adspeek.authservice.entity.ServiceCategory;
import com.adspeek.authservice.entity.ServiceSubcategory;
import com.adspeek.authservice.entity.ServiceItem;
import com.adspeek.authservice.repository.ServiceCategoryRepository;
import com.adspeek.authservice.repository.ServiceSubcategoryRepository;
import com.adspeek.authservice.repository.ServiceItemRepository;
import com.adspeek.authservice.service.ServiceHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceHierarchyServiceImpl implements ServiceHierarchyService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceSubcategoryRepository subcategoryRepository;
    private final ServiceItemRepository itemRepository;

    // Category operations
    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getAllCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getFullHierarchy() {
        return categoryRepository.findAllActiveWithSubcategories()
                .stream()
                .map(this::convertToCategoryDTOWithSubcategories)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceCategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToCategoryDTO)
                .orElse(null);
    }

    // Subcategory operations
    @Override
    @Transactional(readOnly = true)
    public List<ServiceSubcategoryDTO> getSubcategoriesByCategory(Long categoryId) {
        return subcategoryRepository.findByCategoryIdWithItems(categoryId)
                .stream()
                .map(this::convertToSubcategoryDTOWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceSubcategoryDTO getSubcategoryById(Long id) {
        return subcategoryRepository.findById(id)
                .map(this::convertToSubcategoryDTO)
                .orElse(null);
    }

    // Service item operations
    @Override
    @Transactional(readOnly = true)
    public List<ServiceItemDTO> getServiceItemsBySubcategory(Long subcategoryId) {
        return itemRepository.findBySubcategoryIdAndIsActiveTrue(subcategoryId)
                .stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceItemDTO getServiceItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::convertToItemDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceItemDTO> getAllServiceItems() {
        return itemRepository.findAllActiveWithCategoryAndSubcategory()
                .stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    // Admin operations - Create
    @Override
    public ServiceCategoryDTO createCategory(ServiceCategoryDTO categoryDTO) {
        ServiceCategory category = ServiceCategory.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .isActive(true)
                .build();

        ServiceCategory saved = categoryRepository.save(category);
        return convertToCategoryDTO(saved);
    }

    @Override
    public ServiceSubcategoryDTO createSubcategory(ServiceSubcategoryDTO subcategoryDTO) {
        ServiceCategory category = categoryRepository.findById(subcategoryDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ServiceSubcategory subcategory = ServiceSubcategory.builder()
                .name(subcategoryDTO.getName())
                .description(subcategoryDTO.getDescription())
                .category(category)
                .isActive(true)
                .build();

        ServiceSubcategory saved = subcategoryRepository.save(subcategory);
        return convertToSubcategoryDTO(saved);
    }

    @Override
    public ServiceItemDTO createServiceItem(ServiceItemDTO itemDTO) {
        ServiceSubcategory subcategory = subcategoryRepository.findById(itemDTO.getSubcategoryId())
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        ServiceItem item = ServiceItem.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .estimatedHours(itemDTO.getEstimatedHours())
                .subcategory(subcategory)
                .isActive(true)
                .build();

        ServiceItem saved = itemRepository.save(item);
        return convertToItemDTO(saved);
    }

    // Admin operations - Update
    @Override
    public ServiceCategoryDTO updateCategory(Long id, ServiceCategoryDTO categoryDTO) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(categoryDTO.isActive());

        ServiceCategory saved = categoryRepository.save(category);
        return convertToCategoryDTO(saved);
    }

    @Override
    public ServiceSubcategoryDTO updateSubcategory(Long id, ServiceSubcategoryDTO subcategoryDTO) {
        ServiceSubcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        subcategory.setName(subcategoryDTO.getName());
        subcategory.setDescription(subcategoryDTO.getDescription());
        subcategory.setActive(subcategoryDTO.isActive());

        ServiceSubcategory saved = subcategoryRepository.save(subcategory);
        return convertToSubcategoryDTO(saved);
    }

    @Override
    public ServiceItemDTO updateServiceItem(Long id, ServiceItemDTO itemDTO) {
        ServiceItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service item not found"));

        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setEstimatedHours(itemDTO.getEstimatedHours());
        item.setActive(itemDTO.isActive());

        ServiceItem saved = itemRepository.save(item);
        return convertToItemDTO(saved);
    }

    // Admin operations - Delete
    @Override
    public void deleteCategory(Long id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    public void deleteSubcategory(Long id) {
        ServiceSubcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));
        subcategory.setActive(false);
        subcategoryRepository.save(subcategory);
    }

    @Override
    public void deleteServiceItem(Long id) {
        ServiceItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service item not found"));
        item.setActive(false);
        itemRepository.save(item);
    }

    // Conversion methods
    private ServiceCategoryDTO convertToCategoryDTO(ServiceCategory category) {
        return ServiceCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private ServiceCategoryDTO convertToCategoryDTOWithSubcategories(ServiceCategory category) {
        ServiceCategoryDTO dto = convertToCategoryDTO(category);
        if (category.getSubcategories() != null) {
            dto.setSubcategories(category.getSubcategories().stream()
                    .map(this::convertToSubcategoryDTOWithItems)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ServiceSubcategoryDTO convertToSubcategoryDTO(ServiceSubcategory subcategory) {
        return ServiceSubcategoryDTO.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .description(subcategory.getDescription())
                .isActive(subcategory.isActive())
                .createdAt(subcategory.getCreatedAt())
                .updatedAt(subcategory.getUpdatedAt())
                .categoryId(subcategory.getCategory().getId())
                .build();
    }

    private ServiceSubcategoryDTO convertToSubcategoryDTOWithItems(ServiceSubcategory subcategory) {
        ServiceSubcategoryDTO dto = convertToSubcategoryDTO(subcategory);
        if (subcategory.getItems() != null) {
            dto.setItems(subcategory.getItems().stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ServiceItemDTO convertToItemDTO(ServiceItem item) {
        return ServiceItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .estimatedHours(item.getEstimatedHours())
                .isActive(item.isActive())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .subcategoryId(item.getSubcategory().getId())
                .subcategoryName(item.getSubcategory().getName())
                .categoryId(item.getSubcategory().getCategory().getId())
                .categoryName(item.getSubcategory().getCategory().getName())
                .build();
    }
}
