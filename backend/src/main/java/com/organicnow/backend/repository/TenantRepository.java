package com.organicnow.backend.repository;

import com.organicnow.backend.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByNationalId(String nationalId);

    // 🔍 fuzzy search (สะกดคลาดเคลื่อนได้ ≤2 ตัว)
    @Query(value = """
    SELECT * FROM tenant
    WHERE 
        similarity(first_name, :keyword) > 
            CASE 
                WHEN length(:keyword) < 3 THEN 0.15
                WHEN length(:keyword) < 5 THEN 0.2
                ELSE 0.3
            END
     OR similarity(last_name, :keyword) > 
            CASE 
                WHEN length(:keyword) < 3 THEN 0.15
                WHEN length(:keyword) < 5 THEN 0.2
                ELSE 0.3
            END
     OR first_name ILIKE CONCAT('%', :keyword, '%')
     OR last_name ILIKE CONCAT('%', :keyword, '%')
    ORDER BY GREATEST(similarity(first_name, :keyword), similarity(last_name, :keyword)) DESC
""", nativeQuery = true)
    List<Tenant> searchFuzzy(@Param("keyword") String keyword);
}