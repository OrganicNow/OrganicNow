package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.LegacyApiController;
import com.organicnow.backend.service.ContractService;
import com.organicnow.backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LegacyApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class LegacyApiControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ContractService contractService;

    @MockBean
    RoomService roomService;

    // ---------------------------------------------------
    // 1) GET /contracts/occupied-rooms
    // ---------------------------------------------------

    @Test
    void getOccupiedRoomsLegacy_shouldReturnIdsFromService() throws Exception {
        when(contractService.getOccupiedRoomIds())
                .thenReturn(List.of(1L, 2L, 5L));

        mockMvc.perform(get("/contracts/occupied-rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(5));
    }

    @Test
    void getOccupiedRoomsLegacy_whenEmpty_shouldReturnEmptyArray() throws Exception {
        when(contractService.getOccupiedRoomIds())
                .thenReturn(List.of());

        mockMvc.perform(get("/contracts/occupied-rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOccupiedRoomsLegacy_whenServiceThrows_shouldReturn500() throws Exception {
        when(contractService.getOccupiedRoomIds())
                .thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/contracts/occupied-rooms"))
                .andExpect(status().is5xxServerError());
    }

    // ---------------------------------------------------
    // 2) GET /contracts
    // ---------------------------------------------------

    @Test
    void getContractsLegacy_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
