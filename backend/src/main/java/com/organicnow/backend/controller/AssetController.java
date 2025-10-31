package com.organicnow.backend.controller;

import com.organicnow.backend.dto.ApiResponse;
import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.service.AssetService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4173"},
        allowCredentials = "true"
)
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    // ✅ ดึงสินค้าตามห้อง
    @GetMapping("/{roomId}")
    public ApiResponse<List<AssetDto>> getAssetsByRoomId(@PathVariable Long roomId) {
        return new ApiResponse<>("success", assetService.getAssetsByRoomId(roomId));
    }

    // ✅ ดึงสินค้าทั้งหมด (รวมของที่ใช้อยู่)
    @GetMapping("/all")
    public ApiResponse<List<AssetDto>> getAllAssets() {
        return new ApiResponse<>("success", assetService.getAllAssets());
    }

    // ✅ ดึงเฉพาะสินค้าที่ว่าง (ยังไม่ถูกใช้)
    @GetMapping("/available")
    public ApiResponse<List<AssetDto>> getAvailableAssets() {
        return new ApiResponse<>("success", assetService.getAvailableAssets());
    }

    // ✅ Bulk create: /assets/bulk?assetGroupId=2&name=table&qty=24
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Integer>> createBulk(
            @RequestParam("assetGroupId") Long assetGroupId,
            @RequestParam("name") String assetName,
            @RequestParam("qty") int qty
    ) {
        var created = assetService.createBulk(assetGroupId, assetName, qty);
        return ResponseEntity.status(201).body(new ApiResponse<>("success", created.size()));
    }

    // ✅ สร้างสินค้าชิ้นเดียว
    @PostMapping("/create")
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        return ResponseEntity.status(201).body(assetService.createAsset(asset));
    }

    // ✅ อัปเดตสินค้าชิ้นเดียว
    @PutMapping("/update/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @RequestBody Asset asset) {
        return ResponseEntity.ok(assetService.updateAsset(id, asset));
    }

    // ✅ Soft delete (ไม่ลบจริง แต่เปลี่ยนเป็น deleted)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        assetService.softDeleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ เปลี่ยนสถานะ asset เฉพาะชิ้น (เช่น maintenance)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Asset> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusReq req) {
        return ResponseEntity.ok(assetService.updateStatus(id, req.getStatus()));
    }

    @Data
    public static class UpdateStatusReq {
        private String status;
    }

    @GetMapping
    public ApiResponse<List<AssetDto>> listAllAssets() {
        return new ApiResponse<>("success", assetService.getAllAssets());
    }
}