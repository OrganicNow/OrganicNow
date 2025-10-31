package com.organicnow.backend.service;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.AssetRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetGroupRepository assetGroupRepository;
    private final RoomAssetRepository roomAssetRepository;

    // ✅ ดูทั้งหมด (stock + ใช้อยู่)
    public List<AssetDto> getAllAssets() {
        return assetRepository.findAllAssetOptions();
    }

    // ✅ ดึงของในห้อง
    public List<AssetDto> getAssetsByRoomId(Long roomId) {
        return assetRepository.findAssetsByRoomId(roomId);
    }

    // ✅ ดึงของใน stock (ยังว่าง)
    public List<AssetDto> getAvailableAssets() {
        return assetRepository.findAvailableAssets();
    }

    // 🆕 ดึงของที่อยู่ในห้อง (in_use)
    public List<AssetDto> getInUseAssets() {
        return assetRepository.findInUseAssets();
    }

    // ✅ สร้าง asset เดี่ยว
    public Asset createAsset(Asset asset) {
        if (asset.getStatus() == null || asset.getStatus().isBlank()) {
            asset.setStatus("available");
        }
        return assetRepository.save(asset);
    }

    // ✅ อัปเดตข้อมูล asset เดี่ยว
    public Asset updateAsset(Long id, Asset asset) {
        Asset existing = assetRepository.findById(id).orElseThrow();
        existing.setAssetName(asset.getAssetName());
        existing.setAssetGroup(asset.getAssetGroup());
        if (asset.getStatus() != null && !asset.getStatus().isBlank()) {
            existing.setStatus(asset.getStatus());
        }
        return assetRepository.save(existing);
    }

    // ✅ soft delete
    @Transactional
    public void softDeleteAsset(Long id) {
        Asset existing = assetRepository.findById(id).orElseThrow();
        roomAssetRepository.deleteByAsset_Id(id);
        existing.setStatus("deleted");
        assetRepository.save(existing);
    }

    // ✅ เปลี่ยนสถานะทั่วไป
    @Transactional
    public Asset updateStatus(Long id, String status) {
        Asset existing = assetRepository.findById(id).orElseThrow();
        existing.setStatus(status);
        return assetRepository.save(existing);
    }

    // 🆕 Mark ว่า asset ถูกใช้แล้ว (in_use)
    @Transactional
    public void markAssetInUse(Long assetId) {
        Asset asset = assetRepository.findById(assetId).orElseThrow();
        asset.setStatus("in_use");
        assetRepository.save(asset);
    }

    // 🆕 Mark ว่า asset กลับมา stock (available)
    @Transactional
    public void markAssetAvailable(Long assetId) {
        Asset asset = assetRepository.findById(assetId).orElseThrow();
        asset.setStatus("available");
        assetRepository.save(asset);
    }

    // ✅ Bulk create
    @Transactional
    public List<Asset> createBulk(Long assetGroupId, String assetName, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        AssetGroup group = assetGroupRepository.findById(assetGroupId)
                .orElseThrow(() -> new IllegalArgumentException("AssetGroup not found"));

        List<Asset> existingAssets = assetRepository.findByAssetGroupId(assetGroupId);
        int maxIndex = existingAssets.stream()
                .filter(a -> a.getAssetName().startsWith(assetName + "-"))
                .mapToInt(a -> {
                    try {
                        return Integer.parseInt(a.getAssetName().replace(assetName + "-", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        List<Asset> result = new ArrayList<>();
        for (int i = 1; i <= qty; i++) {
            String numberedName = assetName + "-" + String.format("%03d", maxIndex + i);
            Asset a = Asset.builder()
                    .assetGroup(group)
                    .assetName(numberedName)
                    .status("available")
                    .build();
            result.add(a);
        }

        return assetRepository.saveAll(result);
    }
}