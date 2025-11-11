package com.organicnow.backend.service;

import com.organicnow.backend.dto.AssetGroupDropdownDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetGroupService {

    private final AssetGroupRepository assetGroupRepository;
    private final AssetRepository assetRepository;

    // ✅ สำหรับ Dropdown (ใช้เฉพาะบางหน้า)
    @Transactional(readOnly = true)
    public List<AssetGroupDropdownDto> getAllGroupsForDropdown() {
        return assetGroupRepository.findAll().stream()
                .map(group -> AssetGroupDropdownDto.builder()
                        .id(group.getId())
                        .name(group.getAssetGroupName())
                        .threshold(5)
                        .monthlyAddonFee(group.getMonthlyAddonFee())
                        .oneTimeDamageFee(group.getOneTimeDamageFee())
                        .freeReplacement(group.getFreeReplacement())
                        .updatedAt(group.getUpdatedAt())
                        .build())
                .toList();
    }

    // ✅ ดึงข้อมูลทั้งหมด (ใช้ในหน้า asset management)
    public List<AssetGroupDropdownDto> getAllGroups() {
        return assetGroupRepository.findAll().stream()
                .map(g -> AssetGroupDropdownDto.builder()
                        .id(g.getId())
                        .name(g.getAssetGroupName())
                        .threshold(5)
                        .monthlyAddonFee(g.getMonthlyAddonFee())
                        .oneTimeDamageFee(g.getOneTimeDamageFee())
                        .freeReplacement(g.getFreeReplacement())
                        .updatedAt(g.getUpdatedAt())
                        .build())
                .toList();
    }

    // ✅ ดึง Entity ทั้งหมด (ไม่ผ่าน DTO)
    public List<AssetGroup> getAllAssetGroups() {
        return assetGroupRepository.findAll();
    }

    // ✅ Create
    public AssetGroup createAssetGroup(AssetGroup assetGroup) {
        if (assetGroupRepository.existsByAssetGroupName(assetGroup.getAssetGroupName())) {
            throw new RuntimeException("duplicate_group_name");
        }

        // ตั้งค่า default ถ้ายังไม่ได้กำหนด
        if (assetGroup.getMonthlyAddonFee() == null) {
            assetGroup.setMonthlyAddonFee(java.math.BigDecimal.ZERO);
        }
        if (assetGroup.getOneTimeDamageFee() == null) {
            assetGroup.setOneTimeDamageFee(java.math.BigDecimal.ZERO);
        }
        if (assetGroup.getFreeReplacement() == null) {
            assetGroup.setFreeReplacement(true);
        }

        return assetGroupRepository.save(assetGroup);
    }

    // ✅ Update (รองรับฟิลด์ใหม่ครบ)
    public AssetGroup updateAssetGroup(Long id, AssetGroup assetGroup) {
        AssetGroup existingAssetGroup = assetGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset Group not found with id " + id));

        // ตรวจชื่อซ้ำ
        if (!existingAssetGroup.getAssetGroupName().equals(assetGroup.getAssetGroupName())
                && assetGroupRepository.existsByAssetGroupName(assetGroup.getAssetGroupName())) {
            throw new RuntimeException("duplicate_group_name");
        }

        // อัปเดตข้อมูลทั้งหมด
        existingAssetGroup.setAssetGroupName(assetGroup.getAssetGroupName());
        existingAssetGroup.setMonthlyAddonFee(assetGroup.getMonthlyAddonFee());
        existingAssetGroup.setOneTimeDamageFee(assetGroup.getOneTimeDamageFee());
        existingAssetGroup.setFreeReplacement(assetGroup.getFreeReplacement());

        return assetGroupRepository.save(existingAssetGroup);
    }

    // ✅ Delete group พร้อม assets ภายใน
    public int deleteAssetGroup(Long id) {
        List<Asset> assets = assetRepository.findByAssetGroupId(id);
        int deletedCount = assets.size();

        if (!assets.isEmpty()) {
            assetRepository.deleteAll(assets);
        }

        assetGroupRepository.deleteById(id);
        return deletedCount;
    }
}