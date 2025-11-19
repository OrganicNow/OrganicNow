//package com.organicnow.backend.dto;
//
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class DashboardDtoTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        // Act
//        DashboardDto dashboardDto = new DashboardDto();
//
//        // Assert
//        assertNull(dashboardDto.getRooms());
//        assertNull(dashboardDto.getMaintains());
//        assertNull(dashboardDto.getFinances());
//    }
//
//    @Test
//    void testAllArgsConstructor() {
//        // Arrange
//        Map<String, Object> room1 = new HashMap<>();
//        room1.put("roomNumber", "101");
//        room1.put("status", "Occupied");
//
//        Map<String, Object> room2 = new HashMap<>();
//        room2.put("roomNumber", "102");
//        room2.put("status", "Available");
//
//        List<Map<String, Object>> rooms = Arrays.asList(room1, room2);
//
//        // Create MaintainMonthlyDto and FinanceMonthlyDto instances
//        MaintainMonthlyDto maintainDto = new MaintainMonthlyDto("2025-03", 5L);
//        FinanceMonthlyDto financeDto = new FinanceMonthlyDto("2025-03", 10L, 2L, 1L);
//
//        List<MaintainMonthlyDto> maintains = Arrays.asList(maintainDto);
//        List<FinanceMonthlyDto> finances = Arrays.asList(financeDto);
//
//        // Act
//        DashboardDto dashboardDto = new DashboardDto(rooms, maintains, finances);
//
//        // Assert
//        assertNotNull(dashboardDto.getRooms());
//        assertEquals(2, dashboardDto.getRooms().size());
//        assertEquals("101", dashboardDto.getRooms().get(0).get("roomNumber"));
//        assertEquals("Occupied", dashboardDto.getRooms().get(0).get("status"));
//
//        assertNotNull(dashboardDto.getMaintains());
//        assertEquals(1, dashboardDto.getMaintains().size());
//        assertEquals("2025-03", dashboardDto.getMaintains().get(0).getMonth());
//        assertEquals(5L, dashboardDto.getMaintains().get(0).getTotal());
//
//        assertNotNull(dashboardDto.getFinances());
//        assertEquals(1, dashboardDto.getFinances().size());
//        assertEquals("2025-03", dashboardDto.getFinances().get(0).getMonth());
//        assertEquals(10L, dashboardDto.getFinances().get(0).getOnTime());
//        assertEquals(2L, dashboardDto.getFinances().get(0).getPenalty());
//        assertEquals(1L, dashboardDto.getFinances().get(0).getOverdue());
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        DashboardDto dashboardDto = new DashboardDto();
//        Map<String, Object> room = new HashMap<>();
//        room.put("roomNumber", "103");
//        room.put("status", "Occupied");
//
//        List<Map<String, Object>> rooms = Arrays.asList(room);
//        dashboardDto.setRooms(rooms);
//
//        // Act & Assert
//        assertNotNull(dashboardDto.getRooms());
//        assertEquals("103", dashboardDto.getRooms().get(0).get("roomNumber"));
//        assertEquals("Occupied", dashboardDto.getRooms().get(0).get("status"));
//    }
//
//    @Test
//    void testEmptyConstructorAndSetter() {
//        // Arrange
//        DashboardDto dashboardDto = new DashboardDto();
//        assertNull(dashboardDto.getRooms());
//        assertNull(dashboardDto.getMaintains());
//        assertNull(dashboardDto.getFinances());
//
//        List<Map<String, Object>> rooms = Arrays.asList(new HashMap<>());
//        dashboardDto.setRooms(rooms);
//
//        // Act & Assert
//        assertNotNull(dashboardDto.getRooms());
//    }
//}
