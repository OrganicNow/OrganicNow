package com.organicnow.backend.repository;

import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ✅ ดึงห้องทั้งหมดพร้อม tenant และสถานะ
    @Query("""
        SELECT new com.organicnow.backend.dto.RoomDetailDto(
            r.id, r.roomNumber, r.roomFloor,
            CASE WHEN c.id IS NOT NULL THEN 'occupied' ELSE 'available' END,
            COALESCE(t.firstName, ''), COALESCE(t.lastName, ''),
            COALESCE(t.phoneNumber, ''), COALESCE(t.email, ''),
            COALESCE(ct.name, ''), c.signDate, c.startDate, c.endDate
        )
        FROM Room r
        LEFT JOIN Contract c ON r.id = c.room.id AND c.status = 1
        LEFT JOIN Tenant t ON c.tenant.id = t.id
        LEFT JOIN PackagePlan p ON c.packagePlan.id = p.id
        LEFT JOIN ContractType ct ON p.contractType.id = ct.id
        ORDER BY r.roomFloor, r.roomNumber
    """)
    List<RoomDetailDto> findAllRooms();

    // ✅ ดึงห้องตาม id (แก้ไขให้ดูสัญญาที่ยังไม่หมดอายุ)
    @Query("""
        SELECT new com.organicnow.backend.dto.RoomDetailDto(
            r.id, r.roomNumber, r.roomFloor,
            CASE WHEN c.id IS NOT NULL THEN 'occupied' ELSE 'available' END,
            COALESCE(t.firstName, ''), COALESCE(t.lastName, ''),
            COALESCE(t.phoneNumber, ''), COALESCE(t.email, ''),
            COALESCE(ct.name, ''), c.signDate, c.startDate, c.endDate
        )
        FROM Room r
        LEFT JOIN Contract c ON r.id = c.room.id AND c.status = 1 AND c.endDate >= CURRENT_DATE
        LEFT JOIN Tenant t ON c.tenant.id = t.id
        LEFT JOIN PackagePlan p ON c.packagePlan.id = p.id
        LEFT JOIN ContractType ct ON p.contractType.id = ct.id
        WHERE r.id = :roomId
    """)
    RoomDetailDto findRoomDetail(@Param("roomId") Long roomId);

    Optional<Room> findByRoomNumber(String roomNumber);
    
    Optional<Room> findByRoomFloorAndRoomNumber(Integer roomFloor, String roomNumber);

    boolean existsByRoomSize(Integer roomSize);
    
    // ✅ หา contract ปัจจุบันของห้อง (สำหรับ Invoice display)
    @Query("""
        SELECT c
        FROM Contract c
        JOIN c.room r
        WHERE r.roomFloor = :roomFloor 
        AND r.roomNumber = :roomNumber 
        AND c.status = 1 
        AND c.endDate >= CURRENT_DATE
        ORDER BY c.signDate DESC
        LIMIT 1
    """)
    Contract findCurrentContractByRoomFloorAndNumber(@Param("roomFloor") Integer roomFloor, 
                                                    @Param("roomNumber") String roomNumber);
}
