package com.organicnow.backend.repository;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // ✅ สินค้าของห้อง (ไม่เอา deleted)
    @Query("""
        SELECT new com.organicnow.backend.dto.AssetDto(
            a.id, a.assetName, ag.assetGroupName, ag.id, r.roomFloor, r.roomNumber, a.status
        )
        FROM Asset a
        JOIN a.assetGroup ag
        JOIN RoomAsset ra ON a.id = ra.asset.id
        JOIN ra.room r
        WHERE r.id = :roomId
          AND a.status <> 'deleted'
    """)
    List<AssetDto> findAssetsByRoomId(@Param("roomId") Long roomId);

    // ✅ ดูสินค้าทั้งหมด (ไม่เอา deleted)
    @Query("""
        SELECT new com.organicnow.backend.dto.AssetDto(
            a.id, a.assetName, ag.assetGroupName, ag.id, r.roomFloor, r.roomNumber, a.status
        )
        FROM Asset a
        JOIN a.assetGroup ag
        LEFT JOIN RoomAsset ra ON a.id = ra.asset.id
        LEFT JOIN ra.room r
        WHERE a.status <> 'deleted'
    """)
    List<AssetDto> findAllAssetOptions();

    // ✅ เลือกเฉพาะของว่าง (available) ตาม id
    @Query("""
        SELECT a FROM Asset a
        WHERE a.id = :assetId AND a.status = 'available'
    """)
    Asset findAvailableById(@Param("assetId") Long assetId);

    // ✅ ใช้ใน AssetGroupService
    List<Asset> findByAssetGroupId(Long assetGroupId);

    // ✅ ดึงเฉพาะ asset ที่ยังว่าง (ยังไม่ assign เข้าห้อง)
    @Query("""
        SELECT DISTINCT new com.organicnow.backend.dto.AssetDto(
            a.id,
            a.assetName,
            ag.assetGroupName,
            ag.id,
            null,
            null,
            a.status
        )
        FROM Asset a
        JOIN a.assetGroup ag
        LEFT JOIN RoomAsset ra ON ra.asset.id = a.id
        WHERE LOWER(a.status) = 'available'
          AND ra.id IS NULL
        ORDER BY a.assetName ASC
    """)
    List<AssetDto> findAvailableAssets();

    // ✅ ดึงเฉพาะ asset ที่ใช้งานอยู่ในห้อง
    @Query("""
        SELECT new com.organicnow.backend.dto.AssetDto(
            a.id, a.assetName, ag.assetGroupName, ag.id, r.roomFloor, r.roomNumber, a.status
        )
        FROM Asset a
        JOIN a.assetGroup ag
        JOIN RoomAsset ra ON a.id = ra.asset.id
        JOIN ra.room r
        WHERE a.status = 'in_use'
    """)
    List<AssetDto> findInUseAssets();
}