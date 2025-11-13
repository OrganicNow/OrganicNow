//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.DashboardDto;
//import com.organicnow.backend.service.DashboardService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.ArrayList;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//class DashboardControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private DashboardService dashboardService;
//
//    @InjectMocks
//    private DashboardController dashboardController;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
//    }
//
//    @Test
//    void getDashboardData_shouldReturnDashboardDto() throws Exception {
//        // Given
//        DashboardDto dashboardDto = new DashboardDto();
//        // Populate the DashboardDto with mock data
//        dashboardDto.setRooms(new ArrayList<>());
//        dashboardDto.setMaintains(new ArrayList<>());
//        dashboardDto.setFinances(new ArrayList<>());
//
//        // Mock the service call
//        when(dashboardService.getDashboardData()).thenReturn(dashboardDto);
//
//        // When & Then
//        mockMvc.perform(get("/dashboard"))
//                .andExpect(status().isOk())  // Verify HTTP status is 200 OK
//                .andExpect(jsonPath("$.rooms").isArray())  // Check if rooms is an array
//                .andExpect(jsonPath("$.maintains").isArray())  // Check if maintains is an array
//                .andExpect(jsonPath("$.finances").isArray());  // Check if finances is an array
//    }
//}
