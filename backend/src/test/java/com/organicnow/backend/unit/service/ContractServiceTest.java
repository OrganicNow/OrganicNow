package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.RoomRepository;
import com.organicnow.backend.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContractServiceTest {

    private ContractRepository contractRepository;
    private RoomRepository roomRepository;
    private ContractService service;

    @BeforeEach
    void setup() {
        contractRepository = mock(ContractRepository.class);
        roomRepository = mock(RoomRepository.class);
        service = new ContractService(contractRepository, roomRepository);
    }

    // -------------------------------------------------------
    // ✅ TEST: getTenantList()
    // -------------------------------------------------------
    @Test
    void testGetTenantList_returnsList() {
        TenantDto dto = TenantDto.builder()
                .contractId(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(contractRepository.findTenantRows()).thenReturn(List.of(dto));

        List<TenantDto> result = service.getTenantList();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    // -------------------------------------------------------
    // ✅ TEST: getOccupiedRoomIds()
    // -------------------------------------------------------
    @Test
    void testGetOccupiedRoomIds() {
        when(contractRepository.findCurrentlyOccupiedRoomIds()).thenReturn(List.of(101L, 102L));

        List<Long> result = service.getOccupiedRoomIds();

        assertEquals(2, result.size());
        assertTrue(result.contains(101L));
    }

    // -------------------------------------------------------
    // 🟩 TEST: findContractByFloorAndRoom() — SUCCESS
    // -------------------------------------------------------
    @Test
    void testFindContractByFloorAndRoom_success() {

        // prepare mock objects
        Room room = new Room();
        room.setId(50L);
        room.setRoomFloor(3);
        room.setRoomNumber("305");

        Tenant tenant = new Tenant();
        tenant.setFirstName("Alice");
        tenant.setLastName("Wonder");

        Contract contract = new Contract();
        contract.setId(99L);
        contract.setRoom(room);
        contract.setTenant(tenant);

        when(roomRepository.findCurrentContractByRoomFloorAndNumber(3, "305"))
                .thenReturn(contract);

        TenantDto dto = service.findContractByFloorAndRoom(3, "305");

        assertEquals(99L, dto.getContractId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Wonder", dto.getLastName());
        assertEquals(3, dto.getFloor());
        assertEquals("305", dto.getRoom());
        assertEquals(50L, dto.getRoomId());
    }

    // -------------------------------------------------------
    // ❌ TEST: findContractByFloorAndRoom() — contract = null
    // -------------------------------------------------------
    @Test
    void testFindContractByFloorAndRoom_notFound_throwsException() {

        when(roomRepository.findCurrentContractByRoomFloorAndNumber(2, "201"))
                .thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.findContractByFloorAndRoom(2, "201"));

        assertTrue(ex.getMessage().contains("ไม่พบสัญญาสำหรับห้อง Floor 2 Room 201"));
    }

    // -------------------------------------------------------
    // ❌ TEST: findContractByFloorAndRoom() — throws inside repository
    // -------------------------------------------------------
    @Test
    void testFindContractByFloorAndRoom_repositoryThrows_exceptionWrapped() {

        when(roomRepository.findCurrentContractByRoomFloorAndNumber(1, "101"))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.findContractByFloorAndRoom(1, "101"));

        assertTrue(ex.getMessage().contains("เกิดข้อผิดพลาดในการค้นหาสัญญา: DB error"));
    }

    // -------------------------------------------------------
    // 🟡 TEST: findContractByFloorAndRoom() — contract exists but missing fields
    // -------------------------------------------------------
    @Test
    void testFindContractByFloorAndRoom_missingFields_usesFallback() {

        // Contract exists but tenant/room = null
        Contract contract = new Contract();
        contract.setId(77L);
        contract.setTenant(null);
        contract.setRoom(null);

        when(roomRepository.findCurrentContractByRoomFloorAndNumber(4, "401"))
                .thenReturn(contract);

        TenantDto dto = service.findContractByFloorAndRoom(4, "401");

        assertEquals(77L, dto.getContractId());
        assertEquals("N/A", dto.getFirstName());
        assertEquals("", dto.getLastName());
        assertEquals(4, dto.getFloor());
        assertEquals("401", dto.getRoom());
        assertNull(dto.getRoomId());
    }
}
