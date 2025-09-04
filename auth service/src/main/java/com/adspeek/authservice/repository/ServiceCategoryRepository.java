package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findByIsActiveTrue();

    @Query("SELECT sc FROM ServiceCategory sc " +
            "LEFT JOIN FETCH sc.subcategories s " +
            "WHERE sc.isActive = true " +
            "ORDER BY sc.name")
    List<ServiceCategory> findAllActiveWithSubcategories();
}
