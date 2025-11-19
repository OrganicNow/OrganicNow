package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.DashboardDto;
import com.organicnow.backend.dto.FinanceMonthlyDto;
import com.organicnow.backend.dto.MaintainMonthlyDto;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    private RoomRepository roomRepository;
    private ContractRepository contractRepository;
    private MaintainRepository maintainRepository;
    private InvoiceRepository invoiceRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        contractRepository = mock(ContractRepository.class);
        maintainRepository = mock(MaintainRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);

        dashboardService = new DashboardService(
                roomRepository,
                contractRepository,
                maintainRepository,
                invoiceRepository
        );
    }

    // ----------------------------------------------------------
    @Test
    void testGetRoomStatuses() {

        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("101");
        r1.setRoomFloor(1);

        Room r2 = new Room();
        r2.setId(2L);
        r2.setRoomNumber("102");
        r2.setRoomFloor(1);

        when(roomRepository.findAll()).thenReturn(List.of(r1, r2));
        when(contractRepository.existsActiveContractByRoomId(1L)).thenReturn(true);
        when(contractRepository.existsActiveContractByRoomId(2L)).thenReturn(false);
        when(maintainRepository.existsActiveMaintainByRoomId(2L)).thenReturn(true);

        var result = dashboardService.getRoomStatuses();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).get("status"));
        assertEquals(2, result.get(1).get("status"));
    }

    // ----------------------------------------------------------
    @Test
    void testGetMaintainRequests() {

        Object[] raw = new Object[]{"2025-01", 5L};

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(raw);

        when(maintainRepository.countRequestsLast12Months())
                .thenReturn(mockData);

        var result = dashboardService.getMaintainRequests();

        assertEquals(6, result.size());
        assertEquals(result.get(5).getMonth(), YearMonth.now().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)));

    }


    // ----------------------------------------------------------
    @Test
    void testGetFinanceStats() {
        String currentYearMonth = YearMonth.now().toString(); // เช่น "2025-11"
        Object[] raw = new Object[]{currentYearMonth, 10L, 3L, 5L};

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(raw);

        when(invoiceRepository.countFinanceLast12Months())
                .thenReturn(mockData);

        var result = dashboardService.getFinanceStats();

        // 🔥 expected month = last month in the 6-month window
        String expected = YearMonth.now()
                .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));

        FinanceMonthlyDto last = result.get(5);

        assertEquals(expected, last.getMonth());
        assertEquals(10, last.getOnTime());
        assertEquals(3, last.getPenalty());
        assertEquals(5, last.getOverdue());
    }


    // ----------------------------------------------------------
    @Test
    void testGetRoomUsage() {

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomFloor(1);

        when(roomRepository.findAll()).thenReturn(List.of(room));

        Object[] raw = new Object[]{"101", "2025-02", 5, 8};

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(raw);

        when(invoiceRepository.findRoomUsageSummary())
                .thenReturn(mockData);

        Map<String, Object> result = dashboardService.getRoomUsage();

        assertTrue(result.containsKey("101"));
    }


    // ----------------------------------------------------------
    @Test
    void testGetDashboardData() {
        when(roomRepository.findAll()).thenReturn(List.of());
        when(maintainRepository.countRequestsLast12Months()).thenReturn(List.of());
        when(invoiceRepository.countFinanceLast12Months()).thenReturn(List.of());
        when(invoiceRepository.findRoomUsageSummary()).thenReturn(List.of());

        DashboardDto dto = dashboardService.getDashboardData();

        assertNotNull(dto.getRooms());
        assertNotNull(dto.getMaintains());
        assertNotNull(dto.getFinances());
        assertNotNull(dto.getUsages());
    }

    // ----------------------------------------------------------
    @Test
    void testExportMonthlyUsageCsv() {

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomFloor(1);

        when(roomRepository.findAll()).thenReturn(List.of(room));

        Object[] raw = new Object[]{
                "101", "John", "Basic",
                3000, 10, 50, 12, 60, 3120
        };

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(raw);

        when(invoiceRepository.findUsageByMonth("2025-02"))
                .thenReturn(mockData);

        List<String[]> csv = dashboardService.exportMonthlyUsageCsv("Feb 2025");

        assertEquals(3, csv.size());
        assertEquals("101", csv.get(1)[0]);
    }

}
