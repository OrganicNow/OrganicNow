//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.service.ContractTypeService;
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
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ContractTypeController.class)
//class ContractTypeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ContractTypeService contractTypeService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void testGetAllContractTypes() throws Exception {
//        ContractType type1 = new ContractType();
//        type1.setId(1L);
//        type1.setName("3 เดือน");
//        type1.setDuration(3);
//
//        ContractType type2 = new ContractType();
//        type2.setId(2L);
//        type2.setName("6 เดือน");
//        type2.setDuration(6);
//
//        Mockito.when(contractTypeService.getAllContractTypes()).thenReturn(List.of(type1, type2));
//
//        mockMvc.perform(get("/contract-types"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].contract_name").value("3 เดือน"))
//                .andExpect(jsonPath("$[0].duration").value(3))
//                .andExpect(jsonPath("$[1].contract_name").value("6 เดือน"))
//                .andExpect(jsonPath("$[1].duration").value(6));
//    }
//
//    @Test
//    void testGetContractTypeById() throws Exception {
//        ContractType type = new ContractType();
//        type.setId(1L);
//        type.setName("3 เดือน");
//        type.setDuration(3);
//
//        Mockito.when(contractTypeService.getContractTypeById(1L)).thenReturn(type);
//
//        mockMvc.perform(get("/contract-types/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.contract_name").value("3 เดือน"))
//                .andExpect(jsonPath("$.duration").value(3));
//    }
//
//    @Test
//    void testCreateContractType() throws Exception {
//        ContractType type = new ContractType();
//        type.setId(1L);
//        type.setName("3 เดือน");
//        type.setDuration(3);
//
//        Mockito.when(contractTypeService.createContractType(any(ContractType.class))).thenReturn(type);
//
//        mockMvc.perform(post("/contract-types")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(type)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.contract_name").value("3 เดือน"))
//                .andExpect(jsonPath("$.duration").value(3));
//    }
//
//    @Test
//    void testDeleteContractType() throws Exception {
//        Long id = 1L;
//        Mockito.doNothing().when(contractTypeService).deleteContractType(id);
//
//        mockMvc.perform(delete("/contract-types/{id}", id))
//                .andExpect(status().isNoContent());
//
//        Mockito.verify(contractTypeService).deleteContractType(id);
//    }
//}
