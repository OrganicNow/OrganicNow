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

    // ✅ Dropdown
    @Transactional(readOnly = true)
    public List<AssetGroupDropdownDto> getAllGroupsForDropdown() {
        return assetGroupRepository.findAll().stream()
                .map(group -> AssetGroupDropdownDto.builder()
                        .id(group.getId())
                        .name(group.getAssetGroupName())
                        .build())
                .toList();
    }

    // ✅ เพิ่ม threshold ให้ทุก group = 5 ในการดึงข้อมูลทั้งหมด
    public List<AssetGroupDropdownDto> getAllGroups() {
        return assetGroupRepository.findAll().stream()
                .map(g -> new AssetGroupDropdownDto(g.getId(), g.getAssetGroupName(), 5)) // ใช้ threshold = 5 ตลอด
                .toList();
    }

    // ✅ Get all (Entity)
    public List<AssetGroup> getAllAssetGroups() {
        return assetGroupRepository.findAll();
    }

    // ✅ Create
    public AssetGroup createAssetGroup(AssetGroup assetGroup) {
        if (assetGroupRepository.existsByAssetGroupName(assetGroup.getAssetGroupName())) {
            throw new RuntimeException("duplicate_group_name");
        }
        return assetGroupRepository.save(assetGroup);
    }

    // ✅ Update
    public AssetGroup updateAssetGroup(Long id, AssetGroup assetGroup) {
        AssetGroup existingAssetGroup = assetGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset Group not found with id " + id));

        if (!existingAssetGroup.getAssetGroupName().equals(assetGroup.getAssetGroupName())
                && assetGroupRepository.existsByAssetGroupName(assetGroup.getAssetGroupName())) {
            throw new RuntimeException("duplicate_group_name");
        }

        existingAssetGroup.setAssetGroupName(assetGroup.getAssetGroupName());
        return assetGroupRepository.save(existingAssetGroup);
    }

    // ✅ Delete group + assets
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