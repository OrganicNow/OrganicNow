package com.organicnow.backend.dto;

public class AssetDto {
    private Long assetId;
    private String assetName;
    private String assetGroupName; // ✅ เดิม assetType → เปลี่ยนเป็น assetGroupName
    private Long assetGroupId;
    private Integer floor;
    private String room;
    private String status;

    public AssetDto() {}

    // ใช้กับ /assets/all แบบย่อ (id, name, groupName)
    public AssetDto(Long assetId, String assetName, String assetGroupName) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetGroupName = assetGroupName;
    }

    // ใช้กับสินค้าของห้อง (roomFloor, roomNumber)
    public AssetDto(Long assetId, String assetName, String assetGroupName, Integer floor, String room) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetGroupName = assetGroupName;
        this.floor = floor;
        this.room = room;
        this.status = "Active"; // default
    }

    // ใช้กับรายการทั้งหมด (มี status)
    public AssetDto(Long assetId, String assetName, String assetGroupName,
                    Integer floor, String room, String status) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetGroupName = assetGroupName;
        this.floor = floor;
        this.room = room;
        this.status = status;
    }

    // ใช้เมื่ออยากให้มี assetGroupId ด้วย
    public AssetDto(Long assetId, String assetName, String assetGroupName,
                    Long assetGroupId, Integer floor, String room, String status) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetGroupName = assetGroupName;
        this.assetGroupId = assetGroupId;
        this.floor = floor;
        this.room = room;
        this.status = status;
    }

    // ===== Getters & Setters =====
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getAssetGroupName() { return assetGroupName; }
    public void setAssetGroupName(String assetGroupName) { this.assetGroupName = assetGroupName; }

    public Long getAssetGroupId() { return assetGroupId; }
    public void setAssetGroupId(Long assetGroupId) { this.assetGroupId = assetGroupId; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}