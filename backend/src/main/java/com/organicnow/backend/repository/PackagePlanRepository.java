package com.organicnow.backend.repository;

import com.organicnow.backend.model.PackagePlan;
import com.organicnow.backend.model.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackagePlanRepository extends JpaRepository<PackagePlan, Long> {
    // ปิดตัวเก่าด้วย contractType เดียวกันที่ยัง active
    List<PackagePlan> findByContractType_NameAndIsActive(String name, Integer isActive);

    // กันซ้ำ (contractType + roomSize)
    boolean existsByContractType_IdAndRoomSize(Long contractTypeId, Integer roomSize);

    // ดึงแพ็กเกจแอคทีฟด้วย (roomSize, contractTypeId)
    Optional<PackagePlan> findByRoomSizeAndContractType_IdAndIsActive(
            Integer roomSize, Long contractTypeId, Integer isActive);

    // หาแพ็กเกจทั้งหมดของ contractType เดียวกันที่ยัง active
    List<PackagePlan> findByContractType_IdAndIsActive(Long contractTypeId, Integer isActive);

    // ปิด active ทั้งหมดสำหรับคู่ (contractTypeId, roomSize)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PackagePlan p set p.isActive = 0 " +
            "where p.contractType.id = :ctId and p.roomSize = :roomSize and p.isActive = 1")
    int deactivateActiveForPair(@Param("ctId") Long contractTypeId, @Param("roomSize") Integer roomSize);
}
