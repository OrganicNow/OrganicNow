package com.organicnow.backend.controller;

import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.dto.RoomUpdateDto;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000"},
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<RoomDetailDto> getRoomDetail(@PathVariable Long id) {
        RoomDetailDto dto = roomService.getRoomDetail(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<RoomDetailDto>> getAllRooms() {
        List<RoomDetailDto> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/list")
    public ResponseEntity<List<RoomDetailDto>> getAllRoomsList() {
        return getAllRooms();
    }

    // ✅ เพิ่มห้องใหม่
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomUpdateDto dto) {
        try {
            Room newRoom = roomService.createRoom(dto);
            return ResponseEntity.ok(newRoom);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{roomId}/assets/{assetId}")
    public ResponseEntity<?> addAssetToRoom(
            @PathVariable Long roomId,
            @PathVariable Long assetId
    ) {
        try {
            roomService.addAssetToRoom(roomId, assetId);
            return ResponseEntity.ok().body("Asset added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{roomId}/assets/{assetId}")
    public ResponseEntity<?> removeAssetFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long assetId
    ) {
        try {
            roomService.removeAssetFromRoom(roomId, assetId);
            return ResponseEntity.ok().body("Asset removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{roomId}/assets")
    public ResponseEntity<?> updateRoomAssets(
            @PathVariable Long roomId,
            @RequestBody List<Long> assetIds
    ) {
        try {
            roomService.updateRoomAssets(roomId, assetIds);
            return ResponseEntity.ok("Room assets updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoomInfo(
            @PathVariable Long id,
            @RequestBody RoomUpdateDto dto
    ) {
        try {
            roomService.updateRoom(id, dto);
            return ResponseEntity.ok("Room info updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
