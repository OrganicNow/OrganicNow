package com.organicnow.backend.integration.controller;

import com.organicnow.backend.BackendApplication;
import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import com.organicnow.backend.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    InvoiceService invoiceService;

    // -----------------------------------------------
    // Helper: mock invoice list (ใช้ Mockito.mock เพื่อไม่ต้องรู้ structure ของ InvoiceDto)
    // -----------------------------------------------
    private List<InvoiceDto> mockInvoiceList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> Mockito.mock(InvoiceDto.class))
                .toList();
    }

    // ===================================================
    // 1) GET /invoice/list
    // ===================================================
    @Test
    void getAllInvoices_shouldReturnOkAndList() throws Exception {
        Mockito.when(invoiceService.getAllInvoices())
                .thenReturn(mockInvoiceList(2));

        mockMvc.perform(get("/invoice/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllInvoices_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getAllInvoices())
                .thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/invoice/list"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 2) GET /invoice/{id}
    // ===================================================
    @Test
    void getInvoiceById_whenFound_shouldReturnOk() throws Exception {
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.getInvoiceById(1L))
                .thenReturn(Optional.of(dto));

        mockMvc.perform(get("/invoice/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getInvoiceById_whenNotFound_shouldReturn404() throws Exception {
        Mockito.when(invoiceService.getInvoiceById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/invoice/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInvoiceById_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoiceById(1L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/invoice/1"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 3) GET /invoice/search?query=...
    // ===================================================
    @Test
    void searchInvoices_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.searchInvoices("abc"))
                .thenReturn(mockInvoiceList(1));

        mockMvc.perform(get("/invoice/search")
                        .param("query", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchInvoices_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.searchInvoices("error"))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/search")
                        .param("query", "error"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 4) GET /invoice/contract/{contractId}
    // ===================================================
    @Test
    void getInvoicesByContractId_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByContractId(10L))
                .thenReturn(mockInvoiceList(3));

        mockMvc.perform(get("/invoice/contract/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getInvoicesByContractId_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByContractId(10L))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/contract/10"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 5) GET /invoice/room/{roomId}
    // ===================================================
    @Test
    void getInvoicesByRoomId_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByRoomId(1L))
                .thenReturn(mockInvoiceList(2));

        mockMvc.perform(get("/invoice/room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getInvoicesByRoomId_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByRoomId(1L))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/room/1"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 6) GET /invoice/tenant/{tenantId}
    // ===================================================
    @Test
    void getInvoicesByTenantId_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByTenantId(5L))
                .thenReturn(mockInvoiceList(1));

        mockMvc.perform(get("/invoice/tenant/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getInvoicesByTenantId_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByTenantId(5L))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/tenant/5"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 7) GET /invoice/status/{status}
    // ===================================================
    @Test
    void getInvoicesByStatus_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByStatus(1))
                .thenReturn(mockInvoiceList(4));

        mockMvc.perform(get("/invoice/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void getInvoicesByStatus_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByStatus(1))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/status/1"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 8) GET /invoice/unpaid, /paid, /overdue
    // ===================================================
    @Test
    void getUnpaidInvoices_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getUnpaidInvoices())
                .thenReturn(mockInvoiceList(2));

        mockMvc.perform(get("/invoice/unpaid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPaidInvoices_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getPaidInvoices())
                .thenReturn(mockInvoiceList(3));

        mockMvc.perform(get("/invoice/paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getOverdueInvoices_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getOverdueInvoices())
                .thenReturn(mockInvoiceList(1));

        mockMvc.perform(get("/invoice/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUnpaidInvoices_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getUnpaidInvoices())
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/unpaid"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 9) POST /invoice/create
    // ===================================================
    @Test
    void createInvoice_whenSuccess_shouldReturnOk() throws Exception {
        // mock service
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                .thenReturn(dto);

        String payload = """
                {
                  "contractId": 1,
                  "requestedRent": 1000,
                  "requestedWater": 100,
                  "requestedElectricity": 50
                }
                """;

        mockMvc.perform(post("/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void createInvoice_whenServiceThrows_shouldReturn400WithMessage() throws Exception {
        Mockito.when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                .thenThrow(new RuntimeException("Create failed"));

        String payload = """
                {
                  "contractId": 1,
                  "requestedRent": 1000
                }
                """;

        mockMvc.perform(post("/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Create invoice failed")));
    }

    // ===================================================
    // 10) PUT /invoice/update/{id}
    // ===================================================
    @Test
    void updateInvoice_whenSuccess_shouldReturnOk() throws Exception {
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.updateInvoice(eq(1L), any(UpdateInvoiceRequest.class)))
                .thenReturn(dto);

        String payload = """
                {
                  "requestedRent": 1200
                }
                """;

        mockMvc.perform(put("/invoice/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void updateInvoice_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.updateInvoice(eq(1L), any(UpdateInvoiceRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        String payload = """
                {
                  "requestedRent": 1200
                }
                """;

        mockMvc.perform(put("/invoice/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 11) PUT /invoice/pay/{id}
    // ===================================================
    @Test
    void markAsPaid_whenSuccess_shouldReturnOk() throws Exception {
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.markAsPaid(1L)).thenReturn(dto);

        mockMvc.perform(put("/invoice/pay/1"))
                .andExpect(status().isOk());
    }

    @Test
    void markAsPaid_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.markAsPaid(1L))
                .thenThrow(new RuntimeException("Pay failed"));

        mockMvc.perform(put("/invoice/pay/1"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 12) PUT /invoice/cancel/{id}
    // ===================================================
    @Test
    void cancelInvoice_whenSuccess_shouldReturnOk() throws Exception {
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.cancelInvoice(1L)).thenReturn(dto);

        mockMvc.perform(put("/invoice/cancel/1"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelInvoice_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.cancelInvoice(1L))
                .thenThrow(new RuntimeException("Cancel failed"));

        mockMvc.perform(put("/invoice/cancel/1"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 13) PUT /invoice/penalty/{id}?penaltyAmount=...
    // ===================================================
    @Test
    void addPenalty_whenSuccess_shouldReturnOk() throws Exception {
        InvoiceDto dto = Mockito.mock(InvoiceDto.class);
        Mockito.when(invoiceService.addPenalty(1L, 100))
                .thenReturn(dto);

        mockMvc.perform(put("/invoice/penalty/1")
                        .param("penaltyAmount", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void addPenalty_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.addPenalty(1L, 100))
                .thenThrow(new RuntimeException("Penalty failed"));

        mockMvc.perform(put("/invoice/penalty/1")
                        .param("penaltyAmount", "100"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 14) DELETE /invoice/delete/{id}
    // ===================================================
    @Test
    void deleteInvoice_whenSuccess_shouldReturnOkAndSuccessJson() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/invoice/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ลบใบแจ้งหนี้สำเร็จ"));
    }

    @Test
    void deleteInvoice_whenServiceThrows_shouldReturn400WithErrorJson() throws Exception {
        doThrow(new RuntimeException("Some delete error"))
                .when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/invoice/delete/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("ไม่สามารถลบใบแจ้งหนี้ได้")))
                .andExpect(jsonPath("$.error").value("RuntimeException"));
    }

    // ===================================================
    // 15) GET /invoice/date-range
    // ===================================================
    @Test
    void getInvoicesByDateRange_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockInvoiceList(2));

        mockMvc.perform(get("/invoice/date-range")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getInvoicesByDateRange_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/date-range")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 16) GET /invoice/amount-range
    // ===================================================
    @Test
    void getInvoicesByNetAmountRange_shouldReturnOk() throws Exception {
        Mockito.when(invoiceService.getInvoicesByNetAmountRange(100, 500))
                .thenReturn(mockInvoiceList(1));

        mockMvc.perform(get("/invoice/amount-range")
                        .param("minAmount", "100")
                        .param("maxAmount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getInvoicesByNetAmountRange_whenServiceThrows_shouldReturn400() throws Exception {
        Mockito.when(invoiceService.getInvoicesByNetAmountRange(100, 500))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/invoice/amount-range")
                        .param("minAmount", "100")
                        .param("maxAmount", "500"))
                .andExpect(status().isBadRequest());
    }

    // ===================================================
    // 17) POST /invoice/import-csv
    // ===================================================
    @Test
    void importCsv_whenValidCsv_shouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usage.csv",
                "text/csv",
                "room,month,water,electricity".getBytes()
        );

        Mockito.when(invoiceService.importUtilityUsageFromCsv(any()))
                .thenReturn("Imported 3 records");

        mockMvc.perform(multipart("/invoice/import-csv").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Imported 3 records"));
    }

    @Test
    void importCsv_whenFileEmpty_shouldReturn400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "usage.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/invoice/import-csv").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please select a file to upload"));
    }

    @Test
    void importCsv_whenNotCsv_shouldReturn400() throws Exception {
        MockMultipartFile notCsv = new MockMultipartFile(
                "file",
                "usage.txt",
                "text/plain",
                "data".getBytes()
        );

        mockMvc.perform(multipart("/invoice/import-csv").file(notCsv))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload a CSV file"));
    }

    @Test
    void importCsv_whenServiceThrows_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usage.csv",
                "text/csv",
                "data".getBytes()
        );

        Mockito.when(invoiceService.importUtilityUsageFromCsv(any()))
                .thenThrow(new RuntimeException("Parse error"));

        mockMvc.perform(multipart("/invoice/import-csv").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Failed to import CSV")));
    }

    // ===================================================
    // 18) GET /invoice/pdf/{id}
    // ===================================================
    @Test
    void generateInvoicePdf_whenSuccess_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = "PDFDATA".getBytes();
        Mockito.when(invoiceService.generateInvoicePdf(1L))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/invoice/pdf/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("invoice_1.pdf")))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void generateInvoicePdf_whenServiceThrows_shouldReturn500() throws Exception {
        Mockito.when(invoiceService.generateInvoicePdf(1L))
                .thenThrow(new RuntimeException("PDF error"));

        mockMvc.perform(get("/invoice/pdf/1"))
                .andExpect(status().isInternalServerError());
    }
}
