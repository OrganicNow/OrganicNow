//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.service.ContractService;
//import com.organicnow.backend.service.RoomService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Unit test for LegacyApiController
// * - ทดสอบว่า endpoint เก่าทำงานถูกต้อง
// * - Mock service layer (ContractService, RoomService)
// */
//@WebMvcTest(LegacyApiController.class)
//class LegacyApiControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ContractService contractService;
//
//    @MockBean
//    private RoomService roomService;
//
//    @Test
//    @DisplayName("GET /contracts/occupied-rooms → should return list of occupied room IDs")
//    void testGetOccupiedRoomsLegacy_ShouldReturnRoomIds() throws Exception {
//        // Arrange
//        List<Long> mockIds = List.of(1L, 2L, 3L);
//        Mockito.when(contractService.getOccupiedRoomIds()).thenReturn(mockIds);
//
//        // Act & Assert
//        mockMvc.perform(get("/contracts/occupied-rooms")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(3)))
//                .andExpect(jsonPath("$[0]").value(1))
//                .andExpect(jsonPath("$[1]").value(2))
//                .andExpect(jsonPath("$[2]").value(3));
//
//        // Verify interaction
//        Mockito.verify(contractService).getOccupiedRoomIds();
//    }
//
//    @Test
//    @DisplayName("GET /contracts → should return empty list")
//    void testGetContractsLegacy_ShouldReturnEmptyList() throws Exception {
//        mockMvc.perform(get("/contracts")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(0)));
//    }
//}
