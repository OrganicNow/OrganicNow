package com.organicnow.backend.service;

import com.organicnow.backend.dto.*;
import com.organicnow.backend.model.*;
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
    private final AssetEventRepository assetEventRepository;

    // ✅ Helper: แปลง int → ชื่อขนาดห้อง
    private String mapRoomSizeName(Integer sizeCode) {
        return switch (sizeCode) {
            case 0 -> "Studio";
            case 1 -> "Superior";
            case 2 -> "Deluxe";
            default -> "-";
        };
    }

    // ✅ Helper: แปลงชื่อ → int
    private Integer parseRoomSizeCode(String name) {
        if (name == null) return 0;
        return switch (name.trim().toLowerCase()) {
            case "studio" -> 0;
            case "superior" -> 1;
            case "deluxe" -> 2;
            default -> {
                try {
                    yield Integer.parseInt(name);
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
        };
    }

    // ✅ ดึงข้อมูลห้องทั้งหมด
    public List<RoomDetailDto> getAllRooms() {
        List<RoomDetailDto> rooms = roomRepository.findAllRooms();
        if (rooms.isEmpty()) return rooms;

        List<Long> roomIds = rooms.stream()
                .map(RoomDetailDto::getRoomId)
                .collect(Collectors.toList());

        List<Object[]> rows = roomAssetRepository.findAssetsByRoomIds(roomIds);
        Map<Long, List<AssetDto>> assetsByRoom = new HashMap<>();
        for (Object[] row : rows) {
            Long roomId = (Long) row[0];
            Long assetId = (Long) row[1];
            String assetName = (String) row[2];
            String groupName = (String) row[3];
            Integer roomFloor = (Integer) row[4];
            String roomNumber = (String) row[5];
            AssetDto dto = new AssetDto(assetId, assetName, groupName, roomFloor, roomNumber);
            assetsByRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(dto);
        }

        // ✅ เพิ่มชื่อขนาดห้อง
        for (RoomDetailDto room : rooms) {
            Room full = roomRepository.findById(room.getRoomId()).orElse(null);
            if (full != null) {
                room.setRoomSize(mapRoomSizeName(full.getRoomSize()));
            }

            List<RequestDto> reqs = maintainRepository.findRequestsByRoomId(room.getRoomId());
            room.setRequests(reqs);
            room.setAssets(assetsByRoom.getOrDefault(room.getRoomId(), Collections.emptyList()));
        }

        return rooms;
    }

    // ✅ ดึงข้อมูลห้องแบบละเอียด
    public RoomDetailDto getRoomDetail(Long roomId) {
        RoomDetailDto dto = roomRepository.findRoomDetail(roomId);
        if (dto == null) return null;

        Room full = roomRepository.findById(roomId).orElse(null);
        if (full != null) {
            dto.setRoomSize(mapRoomSizeName(full.getRoomSize()));
        }

        dto.setAssets(assetRepository.findAssetsByRoomId(roomId));
        dto.setRequests(maintainRepository.findRequestsByRoomId(roomId));
        return dto;
    }

    // ✅ เพิ่ม / ลบ / อัปเดต assets
    @Transactional
    public void addAssetToRoom(Long roomId, Long assetId) {
        if (roomAssetRepository.existsByRoomIdAndAssetId(roomId, assetId)) {
            throw new RuntimeException("Asset already exists in this room");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Asset asset = assetRepository.findAvailableById(assetId);
        if (asset == null) throw new RuntimeException("Asset not available or not found");

        RoomAsset ra = new RoomAsset();
        ra.setRoom(room);
        ra.setAsset(asset);
        roomAssetRepository.save(ra);

        asset.setStatus("in_use");
        assetRepository.save(asset);
    }

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

    @Transactional
    public void updateRoomAssets(Long roomId, List<Long> newAssetIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomAsset> oldRelations = roomAssetRepository.findByRoomId(roomId);
        Set<Long> oldAssetIds = oldRelations.stream()
                .map(ra -> ra.getAsset().getId())
                .collect(Collectors.toSet());
        Set<Long> newAssetIdSet = new HashSet<>(newAssetIds != null ? newAssetIds : Collections.emptyList());

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

    // ✅ เพิ่ม/ลบ พร้อมเหตุผล (Event Logging)
    @Transactional
    public void updateRoomAssetsWithReason(Long roomId, List<Long> newAssetIds, String reasonType, String note) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomAsset> oldRelations = roomAssetRepository.findByRoomId(roomId);
        Set<Long> oldAssetIds = oldRelations.stream()
                .map(ra -> ra.getAsset().getId())
                .collect(Collectors.toSet());
        Set<Long> newAssetIdSet = new HashSet<>(newAssetIds != null ? newAssetIds : Collections.emptyList());

        // ✅ ของที่ถูกลบออก
        Set<Long> toRemove = oldAssetIds.stream()
                .filter(id -> !newAssetIdSet.contains(id))
                .collect(Collectors.toSet());

        for (RoomAsset ra : oldRelations) {
            if (toRemove.contains(ra.getAsset().getId())) {
                roomAssetRepository.delete(ra);
                Asset asset = ra.getAsset();
                asset.setStatus("available");
                assetRepository.save(asset);

                assetEventRepository.save(AssetEvent.builder()
                        .room(room)
                        .asset(asset)
                        .eventType("removed")
                        .reasonType(reasonType)
                        .note(note)
                        .build());
            }
        }

        // ✅ ของที่ถูกเพิ่มใหม่
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

                assetEventRepository.save(AssetEvent.builder()
                        .room(room)
                        .asset(asset)
                        .eventType("added")
                        .reasonType(reasonType)
                        .note(note)
                        .build());
            }
        }
    }

    // ✅ แปลง Entity → DTO เพื่อป้องกัน ByteBuddy error
    public List<AssetEventDto> getRoomAssetEvents(Long roomId) {
        return assetEventRepository.findByRoom_Id(roomId).stream()
                .map(event -> AssetEventDto.builder()
                        .eventId(event.getEventId())
                        .roomId(event.getRoom().getId())
                        .assetId(event.getAsset().getId())
                        .assetName(event.getAsset().getAssetName())
                        .eventType(event.getEventType())
                        .reasonType(event.getReasonType())
                        .note(event.getNote())
                        .createdAt(event.getCreatedAt())
                        .build())
                .toList();
    }

    // ✅ อัปเดตข้อมูลพื้นฐานของห้อง
    @Transactional
    public void updateRoom(Long id, RoomUpdateDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (dto.getRoomFloor() != null) room.setRoomFloor(dto.getRoomFloor());
        if (dto.getRoomNumber() != null) room.setRoomNumber(dto.getRoomNumber());
        if (dto.getRoomSize() != null) room.setRoomSize(parseRoomSizeCode(dto.getRoomSize()));

        roomRepository.save(room);
    }

    // ✅ เพิ่มห้องใหม่
    @Transactional
    public Room createRoom(RoomUpdateDto dto) {
        if (dto.getRoomNumber() == null || dto.getRoomFloor() == null)
            throw new RuntimeException("Missing required fields: roomNumber or roomFloor");

        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomFloor(dto.getRoomFloor());
        room.setRoomSize(parseRoomSizeCode(dto.getRoomSize()));

        return roomRepository.save(room);
    }

    // ✅ ลบห้อง (แก้ไขเพิ่มลบ event ก่อน)
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));

        // ✅ ลบ event ทั้งหมดที่อ้างถึงห้องนี้ก่อน
        assetEventRepository.deleteByRoom_Id(id);
        // ✅ ลบ maintenance request ทั้งหมดที่อ้างถึงห้องนี้ (ป้องกัน constraint error)
        var requests = maintainRepository.findRequestsByRoomId(id);
        if (!requests.isEmpty()) {
            maintainRepository.deleteAllByRoomId(id);
        }

        // ✅ ลบความสัมพันธ์ room ↔ asset ก่อน (เหมือนเดิม)
        List<RoomAsset> roomAssets = roomAssetRepository.findByRoomId(id);
        if (!roomAssets.isEmpty()) {
            for (RoomAsset ra : roomAssets) {
                Asset asset = ra.getAsset();
                if (asset != null) {
                    asset.setStatus("available");
                    assetRepository.save(asset);
                }
            }
            roomAssetRepository.deleteAll(roomAssets);
        }

        // ✅ แล้วค่อยลบห้อง
        roomRepository.delete(room);
    }
}