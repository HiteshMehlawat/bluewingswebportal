package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.ServiceSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceSubcategoryRepository extends JpaRepository<ServiceSubcategory, Long> {

    List<ServiceSubcategory> findByCategoryIdAndIsActiveTrue(Long categoryId);

    @Query("SELECT ss FROM ServiceSubcategory ss " +
            "LEFT JOIN FETCH ss.items " +
            "WHERE ss.category.id = :categoryId AND ss.isActive = true " +
            "ORDER BY ss.name")
    List<ServiceSubcategory> findByCategoryIdWithItems(Long categoryId);
}
