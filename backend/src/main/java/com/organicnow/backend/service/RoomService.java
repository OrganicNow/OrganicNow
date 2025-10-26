package com.organicnow.backend.service;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.dto.RoomUpdateDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import com.organicnow.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final AssetRepository assetRepository;
    private final MaintainRepository maintainRepository;
    private final RoomAssetRepository roomAssetRepository;

    // ✅ ดึงข้อมูลห้องทั้งหมด (พร้อม requests) และ assets แบบยิง DB ครั้งเดียว (ไม่ N+1)
    public List<RoomDetailDto> getAllRooms() {
        List<RoomDetailDto> rooms = roomRepository.findAllRooms();
        if (rooms.isEmpty()) return rooms;

        // roomIds สำหรับดึง assets ครั้งเดียว
        List<Long> roomIds = rooms.stream()
                .map(RoomDetailDto::getRoomId)
                .collect(Collectors.toList());

        // ✅ ดึง assets ของทุกห้องในครั้งเดียว
        List<Object[]> rows = roomAssetRepository.findAssetsByRoomIds(roomIds);

        // map: roomId -> List<AssetDto>
        Map<Long, List<AssetDto>> assetsByRoom = new HashMap<>();
        for (Object[] row : rows) {
            Long roomId       = (Long)    row[0];
            Long assetId      = (Long)    row[1];
            String assetName  = (String)  row[2];
            String groupName  = (String)  row[3];
            Integer roomFloor = (Integer) row[4];
            String roomNumber = (String)  row[5];

            AssetDto dto = new AssetDto(assetId, assetName, groupName, roomFloor, roomNumber);
            assetsByRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(dto);
        }

        // เติม requests (คงเดิม) + assets (จาก map ที่รวมมาแล้ว)
        for (RoomDetailDto room : rooms) {
            List<RequestDto> reqs = maintainRepository.findRequestsByRoomId(room.getRoomId());
            room.setRequests(reqs);

            room.setAssets(assetsByRoom.getOrDefault(room.getRoomId(), Collections.emptyList()));
        }

        return rooms;
    }

    // ✅ ดึงข้อมูลห้องแบบละเอียด (เดิม) — ใช้ได้เลย
    public RoomDetailDto getRoomDetail(Long roomId) {
        RoomDetailDto dto = roomRepository.findRoomDetail(roomId);
        if (dto == null) return null;

        List<AssetDto> assets = assetRepository.findAssetsByRoomId(roomId);
        List<RequestDto> requests = maintainRepository.findRequestsByRoomId(roomId);

        dto.setAssets(assets);
        dto.setRequests(requests);
        return dto;
    }

    // ✅ เพิ่มของเข้าห้อง
    @Transactional
    public void addAssetToRoom(Long roomId, Long assetId) {
        if (roomAssetRepository.existsByRoomIdAndAssetId(roomId, assetId)) {
            throw new RuntimeException("Asset already exists in this room");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Asset asset = assetRepository.findAvailableById(assetId);
        if (asset == null) {
            throw new RuntimeException("Asset not available or not found");
        }

        RoomAsset ra = new RoomAsset();
        ra.setRoom(room);
        ra.setAsset(asset);
        roomAssetRepository.save(ra);

        asset.setStatus("in_use");
        assetRepository.save(asset);
    }

    // ✅ เอาของออกจากห้อง
    @Transactional
    public void removeAssetFromRoom(Long roomId, Long assetId) {
        RoomAsset ra = roomAssetRepository.findByRoomIdAndAssetId(roomId, assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found in this room"));
        roomAssetRepository.delete(ra);

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        asset.setStatus("available");
        assetRepository.save(asset);
    }

    // ✅ อัปเดตของในห้องแบบฉลาด (diff update — ไม่ลบหมด)
    @Transactional
    public void updateRoomAssets(Long roomId, List<Long> newAssetIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomAsset> oldRelations = roomAssetRepository.findByRoomId(roomId);
        Set<Long> oldAssetIds = oldRelations.stream()
                .map(ra -> ra.getAsset().getId())
                .collect(Collectors.toSet());
        Set<Long> newAssetIdSet = new HashSet<>(newAssetIds != null ? newAssetIds : Collections.emptyList());

        // 1️⃣ ลบออก
        Set<Long> toRemove = oldAssetIds.stream()
                .filter(id -> !newAssetIdSet.contains(id))
                .collect(Collectors.toSet());

        for (RoomAsset ra : oldRelations) {
            if (toRemove.contains(ra.getAsset().getId())) {
                roomAssetRepository.delete(ra);
                Asset asset = ra.getAsset();
                asset.setStatus("available");
                assetRepository.save(asset);
            }
        }

        // 2️⃣ เพิ่มใหม่
        Set<Long> toAdd = newAssetIdSet.stream()
                .filter(id -> !oldAssetIds.contains(id))
                .collect(Collectors.toSet());

        if (!toAdd.isEmpty()) {
            List<Asset> assetsToAdd = assetRepository.findAllById(toAdd);
            for (Asset asset : assetsToAdd) {
                RoomAsset newRa = new RoomAsset();
                newRa.setRoom(room);
                newRa.setAsset(asset);
                roomAssetRepository.save(newRa);

                asset.setStatus("in_use");
                assetRepository.save(asset);
            }
        }
    }

    // ✅ อัปเดตข้อมูลพื้นฐานของห้อง
    @Transactional
    public void updateRoom(Long id, RoomUpdateDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (dto.getRoomFloor() != null) room.setRoomFloor(dto.getRoomFloor());
        if (dto.getRoomNumber() != null) room.setRoomNumber(dto.getRoomNumber());

        roomRepository.save(room);
    }


}
