package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.DashboardDto;
import com.organicnow.backend.dto.MaintainMonthlyDto;
import com.organicnow.backend.dto.FinanceMonthlyDto;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DashboardDtoTest {

    @Test
    void testConstructorAndGetters() {

        // ----- rooms -----
        Map<String, Object> room1 = new HashMap<>();
        room1.put("room", "101");
        room1.put("status", "Occupied");

        Map<String, Object> room2 = new HashMap<>();
        room2.put("room", "102");
        room2.put("status", "Vacant");

        List<Map<String, Object>> rooms = Arrays.asList(room1, room2);

        // ----- maintains -----
        List<MaintainMonthlyDto> maintains = Arrays.asList(
                new MaintainMonthlyDto("2024-01", 5L),
                new MaintainMonthlyDto("2024-02", 3L)
        );

        // ----- finances -----
        List<FinanceMonthlyDto> finances = Arrays.asList(
                new FinanceMonthlyDto("2024-01", 100L, 20L, 10L),
                new FinanceMonthlyDto("2024-02", 120L, 15L, 5L)
        );

        // ----- usages -----
        Map<String, Object> usages = new HashMap<>();
        usages.put("water", 500);
        usages.put("electricity", 800);

        // ----- dto -----
        DashboardDto dto = new DashboardDto(rooms, maintains, finances, usages);

        assertEquals(rooms, dto.getRooms());
        assertEquals(maintains, dto.getMaintains());
        assertEquals(finances, dto.getFinances());
        assertEquals(usages, dto.getUsages());
    }


    @Test
    void testNoArgsConstructorAndSetters() {
        DashboardDto dto = new DashboardDto();

        List<Map<String, Object>> rooms = new ArrayList<>();
        rooms.add(Map.of("room", "301", "status", "Occupied"));

        List<MaintainMonthlyDto> maintains = List.of(
                new MaintainMonthlyDto("2024-01", 4L)
        );

        List<FinanceMonthlyDto> finances = List.of(
                new FinanceMonthlyDto("2024-01", 50L, 5L, 2L)
        );

        Map<String, Object> usages = Map.of("water", 200, "electricity", 400);

        dto.setRooms(rooms);
        dto.setMaintains(maintains);
        dto.setFinances(finances);
        dto.setUsages(usages);

        assertEquals(rooms, dto.getRooms());
        assertEquals(maintains, dto.getMaintains());
        assertEquals(finances, dto.getFinances());
        assertEquals(usages, dto.getUsages());
    }


    @Test
    void testEqualsAndHashCode() {
        List<Map<String, Object>> rooms = List.of(Map.of("room", "101", "status", "OK"));

        List<MaintainMonthlyDto> maintains = List.of(
                new MaintainMonthlyDto("2024-01", 5L)
        );

        List<FinanceMonthlyDto> finances = List.of(
                new FinanceMonthlyDto("2024-01", 100L, 20L, 10L)
        );

        Map<String, Object> usages = Map.of("water", 100, "electricity", 200);

        DashboardDto dto1 = new DashboardDto(rooms, maintains, finances, usages);
        DashboardDto dto2 = new DashboardDto(rooms, maintains, finances, usages);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }


    @Test
    void testToStringNotNull() {
        DashboardDto dto = new DashboardDto();
        assertNotNull(dto.toString());
    }
}
