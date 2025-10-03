package com.organicnow.backend.controller;

import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.service.AssetGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset-group")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4173"}, allowCredentials = "true")
public class AssetGroupController {

    private final AssetGroupService assetGroupService;

    public AssetGroupController(AssetGroupService assetGroupService) {
        this.assetGroupService = assetGroupService;
    }

    // Get all Asset Groups
    @GetMapping("/list")
    public ResponseEntity<List<AssetGroup>> getAllAssetGroups() {
        List<AssetGroup> assetGroups = assetGroupService.getAllAssetGroups();
        return ResponseEntity.ok(assetGroups);
    }

    // Create Asset Group
    @PostMapping("/create")
    public ResponseEntity<AssetGroup> createAssetGroup(@RequestBody AssetGroup assetGroup) {
        AssetGroup createdAssetGroup = assetGroupService.createAssetGroup(assetGroup);
        return ResponseEntity.status(201).body(createdAssetGroup);
    }

    // Update Asset Group
    @PutMapping("/update/{id}")
    public ResponseEntity<AssetGroup> updateAssetGroup(@PathVariable Long id, @RequestBody AssetGroup assetGroup) {
        AssetGroup updatedAssetGroup = assetGroupService.updateAssetGroup(id, assetGroup);
        return ResponseEntity.ok(updatedAssetGroup);
    }

    // Delete Asset Group (ลบ Asset ด้วย)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssetGroup(@PathVariable Long id) {
        int deletedAssets = assetGroupService.deleteAssetGroup(id);
        return ResponseEntity.ok(Map.of(
                "message", "deleted_group",
                "deletedAssets", deletedAssets
        ));
    }
}