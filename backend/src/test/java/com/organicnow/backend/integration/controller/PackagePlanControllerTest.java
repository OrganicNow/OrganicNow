//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.dto.PackagePlanDto;
//import com.organicnow.backend.dto.PackagePlanRequestDto;
//import com.organicnow.backend.service.PackagePlanService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(PackagePlanController.class)
//class PackagePlanControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private PackagePlanService packagePlanService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void testGetAllPackages() throws Exception {
//        PackagePlanDto dto = new PackagePlanDto(
//                1L,
//                BigDecimal.valueOf(10000),
//                1,
//                "6 เดือน",
//                6
//        );
//
//        Mockito.when(packagePlanService.getAllPackages()).thenReturn(List.of(dto));
//
//        mockMvc.perform(get("/packages"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].contract_name").value("6 เดือน"))
//                .andExpect(jsonPath("$[0].price").value(10000));
//    }
//
//    @Test
//    void testCreatePackage() throws Exception {
//        PackagePlanRequestDto request = new PackagePlanRequestDto();
//        request.setPrice(BigDecimal.valueOf(8000));
//        request.setIsActive(1);
//        request.setContractTypeId(1L);
//
//        mockMvc.perform(post("/packages")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated());  // ✅ เปลี่ยนเป็น isCreated()
//
//        Mockito.verify(packagePlanService).createPackage(any(PackagePlanRequestDto.class));
//    }
//}
