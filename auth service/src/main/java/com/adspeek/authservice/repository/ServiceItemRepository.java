package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findBySubcategoryIdAndIsActiveTrue(Long subcategoryId);

    @Query("SELECT si FROM ServiceItem si " +
            "JOIN si.subcategory ss " +
            "JOIN ss.category sc " +
            "WHERE si.isActive = true " +
            "ORDER BY sc.name, ss.name, si.name")
    List<ServiceItem> findAllActiveWithCategoryAndSubcategory();
}
