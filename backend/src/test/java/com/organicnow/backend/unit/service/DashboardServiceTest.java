//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.DashboardDto;
//import com.organicnow.backend.dto.FinanceMonthlyDto;
//import com.organicnow.backend.dto.MaintainMonthlyDto;
//import com.organicnow.backend.model.Room;
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.repository.InvoiceRepository;
//import com.organicnow.backend.repository.MaintainRepository;
//import com.organicnow.backend.repository.RoomRepository;
//import com.organicnow.backend.service.DashboardService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class DashboardServiceTest {
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @Mock
//    private MaintainRepository maintainRepository;
//
//    @Mock
//    private InvoiceRepository invoiceRepository;
//
//    @InjectMocks
//    private DashboardService dashboardService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void getRoomStatuses_shouldReturnCorrectStatuses() {
//        Room room1 = Room.builder().id(1L).roomNumber("101").build();
//        Room room2 = Room.builder().id(2L).roomNumber("102").build();
//        Room room3 = Room.builder().id(3L).roomNumber("103").build();
//
//        // Mock repository method calls
//        when(roomRepository.findAll()).thenReturn(List.of(room1, room2, room3));
//        when(contractRepository.existsActiveContractByRoomId(1L)).thenReturn(true);
//        when(contractRepository.existsActiveContractByRoomId(2L)).thenReturn(false);
//        when(contractRepository.existsActiveContractByRoomId(3L)).thenReturn(false);
//
//        when(maintainRepository.existsActiveMaintainByRoomId(2L)).thenReturn(true);
//        when(maintainRepository.existsActiveMaintainByRoomId(3L)).thenReturn(false);
//
//        // Call service method to test
//        List<Map<String, Object>> result = dashboardService.getRoomStatuses();
//
//        // Assertions for expected results
//        assertEquals(3, result.size());
//        assertEquals(1, result.get(0).get("status")); // room1 occupied
//        assertEquals(2, result.get(1).get("status")); // room2 repair
//        assertEquals(0, result.get(2).get("status")); // room3 available
//    }
//
//    @Test
//    void getMaintainRequests_shouldReturnMappedDtos() {
//        // Mock repository to return raw data (likely Object[] or similar)
//        Object[] maintain1 = {"2025-01", 5L};
//        Object[] maintain2 = {"2025-02", 3L};
//
//        // Mock repository method to return sample data
//        when(maintainRepository.countRequestsLast12Months()).thenReturn(List.of(maintain1, maintain2));
//
//        // Call the service method to test
//        List<MaintainMonthlyDto> result = dashboardService.getMaintainRequests();
//
//        // Assertions for correct mapping of data
//        assertEquals(2, result.size());
//        assertEquals("2025-01", result.get(0).getMonth());
//        assertEquals(5L, result.get(0).getTotal());  // Use getTotal() since it's defined in the DTO
//        assertEquals("2025-02", result.get(1).getMonth());
//        assertEquals(3L, result.get(1).getTotal());  // Use getTotal() here as well
//    }
//
//    @Test
//    void getFinanceStats_shouldReturnMappedDtos() {
//        // Mock repository to return raw data (likely Object[] or similar)
//        Object[] finance1 = {"2025-01", 10L, 2L, 1L};
//        Object[] finance2 = {"2025-02", 8L, 0L, 3L};
//
//        // Mock repository method to return sample data
//        when(invoiceRepository.countFinanceLast12Months()).thenReturn(List.of(finance1, finance2));
//
//        // Call the service method to test
//        List<FinanceMonthlyDto> result = dashboardService.getFinanceStats();
//
//        // Assertions for correct mapping of data
//        assertEquals(2, result.size());
//        assertEquals("2025-01", result.get(0).getMonth());
//        assertEquals(10L, result.get(0).getOnTime());
//        assertEquals(2L, result.get(0).getPenalty());
//        assertEquals(1L, result.get(0).getOverdue());
//
//        assertEquals("2025-02", result.get(1).getMonth());
//        assertEquals(8L, result.get(1).getOnTime());
//        assertEquals(0L, result.get(1).getPenalty());
//        assertEquals(3L, result.get(1).getOverdue());
//    }
//
//    @Test
//    void getDashboardData_shouldReturnAllSections() {
//        // Mock room statuses
//        Room room = Room.builder().id(1L).roomNumber("101").build();
//        when(roomRepository.findAll()).thenReturn(List.of(room));
//        when(contractRepository.existsActiveContractByRoomId(1L)).thenReturn(false);
//        when(maintainRepository.existsActiveMaintainByRoomId(1L)).thenReturn(false);
//
//        // Mock maintain data (raw data from repository)
//        Object[] maintain1 = {"2025-01", 5L};
//        Object[] maintain2 = {"2025-02", 3L};
//        when(maintainRepository.countRequestsLast12Months())
//                .thenReturn(List.of(maintain1, maintain2));  // Return raw data
//
//        // Mock finance data (raw data from repository)
//        Object[] finance1 = {"2025-01", 10L, 2L, 1L};
//        when(invoiceRepository.countFinanceLast12Months())
//                .thenReturn(java.util.Collections.singletonList(finance1));  // Return raw data
//
//        // Call the service method to get the complete dashboard data
//        DashboardDto dashboard = dashboardService.getDashboardData();
//
//        // Assertions for correct results
//        assertNotNull(dashboard);
//        assertEquals(1, dashboard.getRooms().size());  // Check room statuses
//        assertEquals(2, dashboard.getMaintains().size());  // Ensure 2 items in maintain requests
//        assertEquals(1, dashboard.getFinances().size());  // Check finance stats
//
//        // Check MaintainMonthlyDto values
//        MaintainMonthlyDto maintainDto = dashboard.getMaintains().get(0);
//        assertEquals("2025-01", maintainDto.getMonth());
//        assertEquals(5L, maintainDto.getTotal());  // Use getTotal() as per the DTO
//
//        // Check FinanceMonthlyDto values
//        FinanceMonthlyDto financeDto = dashboard.getFinances().get(0);
//        assertEquals("2025-01", financeDto.getMonth());
//        assertEquals(10L, financeDto.getOnTime());
//        assertEquals(2L, financeDto.getPenalty());
//        assertEquals(1L, financeDto.getOverdue());
//    }
//
//}