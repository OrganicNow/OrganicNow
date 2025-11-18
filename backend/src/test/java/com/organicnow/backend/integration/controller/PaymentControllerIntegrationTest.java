package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.PaymentController;
import com.organicnow.backend.dto.CreatePaymentRecordRequest;
import com.organicnow.backend.dto.PaymentProofDto;
import com.organicnow.backend.dto.PaymentRecordDto;
import com.organicnow.backend.dto.UpdatePaymentRecordRequest;
import com.organicnow.backend.model.PaymentProof;
import com.organicnow.backend.model.PaymentRecord;
import com.organicnow.backend.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    // -------------------------------------------------------
    // 1) POST /api/payments/records - addPaymentRecord
    // -------------------------------------------------------
    @Test
    void addPaymentRecord_shouldReturn200AndCallService() throws Exception {
        PaymentRecordDto dto = Mockito.mock(PaymentRecordDto.class);
        when(paymentService.addPaymentRecord(any(CreatePaymentRecordRequest.class)))
                .thenReturn(dto);

        String json = """
                {
                  "invoiceId": 1,
                  "paymentAmount": 1000,
                  "paymentMethod": "CASH",
                  "notes": "test"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());

        verify(paymentService).addPaymentRecord(any(CreatePaymentRecordRequest.class));
    }

    @Test
    void addPaymentRecord_whenServiceThrows_shouldReturn500() throws Exception {
        when(paymentService.addPaymentRecord(any(CreatePaymentRecordRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        String json = """
                {
                  "invoiceId": 1,
                  "paymentAmount": 1000,
                  "paymentMethod": "CASH",
                  "notes": "test"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 2) GET /api/payments/records/invoice/{invoiceId}
    // -------------------------------------------------------
    @Test
    void getPaymentRecordsByInvoice_shouldReturnList() throws Exception {
        PaymentRecordDto r1 = Mockito.mock(PaymentRecordDto.class);
        PaymentRecordDto r2 = Mockito.mock(PaymentRecordDto.class);

        when(paymentService.getPaymentRecordsByInvoice(1L))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/payments/records/invoice/{invoiceId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPaymentRecordsByInvoice_whenServiceThrows_shouldReturnEmptyList() throws Exception {
        when(paymentService.getPaymentRecordsByInvoice(anyLong()))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/payments/records/invoice/{invoiceId}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------
    // 3) GET /api/payments/payment-methods
    // -------------------------------------------------------
    @Test
    void getPaymentMethods_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/api/payments/payment-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CASH").exists());
    }

    // -------------------------------------------------------
    // 4) GET /api/payments/payment-statuses
    // -------------------------------------------------------
    @Test
    void getPaymentStatuses_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/api/payments/payment-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDING").exists());
    }

    // -------------------------------------------------------
    // 5) PUT /api/payments/records/{paymentRecordId}
    // -------------------------------------------------------
    @Test
    void updatePaymentRecord_shouldReturn200() throws Exception {
        PaymentRecordDto dto = Mockito.mock(PaymentRecordDto.class);
        when(paymentService.updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class)))
                .thenReturn(dto);

        String json = """
                {
                  "paymentAmount": 2000,
                  "paymentMethod": "BANK_TRANSFER",
                  "paymentStatus": "CONFIRMED",
                  "notes": "updated"
                }
                """;

        mockMvc.perform(
                        put("/api/payments/records/{id}", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());

        verify(paymentService).updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class));
    }

    @Test
    void updatePaymentRecord_whenServiceThrows_shouldReturn500() throws Exception {
        when(paymentService.updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class)))
                .thenThrow(new RuntimeException("update fail"));

        String json = """
                {
                  "paymentAmount": 2000,
                  "paymentMethod": "BANK_TRANSFER",
                  "paymentStatus": "CONFIRMED",
                  "notes": "updated"
                }
                """;

        mockMvc.perform(
                        put("/api/payments/records/{id}", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 6) PUT /api/payments/records/{paymentRecordId}/status
    // -------------------------------------------------------
    @Test
    void updatePaymentStatus_shouldWrapStatusAndCallService() throws Exception {
        PaymentRecordDto dto = Mockito.mock(PaymentRecordDto.class);
        when(paymentService.updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(
                        put("/api/payments/records/{id}/status", 5L)
                                .param("status", "CONFIRMED")
                )
                .andExpect(status().isOk());

        verify(paymentService).updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class));
    }

    @Test
    void updatePaymentStatus_whenServiceThrows_shouldReturn500() throws Exception {
        when(paymentService.updatePaymentRecord(anyLong(), any(UpdatePaymentRecordRequest.class)))
                .thenThrow(new RuntimeException("status fail"));

        mockMvc.perform(
                        put("/api/payments/records/{id}/status", 5L)
                                .param("status", "CONFIRMED")
                )
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 7) DELETE /api/payments/records/{paymentRecordId}
    // -------------------------------------------------------
    @Test
    void deletePaymentRecord_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/payments/records/{id}", 7L))
                .andExpect(status().isOk());

        verify(paymentService).deletePaymentRecord(7L);
    }

    @Test
    void deletePaymentRecord_whenServiceThrows_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("delete fail"))
                .when(paymentService).deletePaymentRecord(8L);

        mockMvc.perform(delete("/api/payments/records/{id}", 8L))
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 8) POST /api/payments/records/{paymentRecordId}/proofs (upload)
    // -------------------------------------------------------
    @Test
    void uploadPaymentProof_withValidFile_shouldReturn200() throws Exception {
        PaymentProofDto dto = Mockito.mock(PaymentProofDto.class);
        when(paymentService.uploadPaymentProof(
                anyLong(),
                any(),
                any(PaymentProof.ProofType.class),
                any(),
                any())
        ).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "slip.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/payments/records/{id}/proofs", 1L)
                                .file(file)
                                .param("proofType", "BANK_SLIP")
                                .param("description", "test desc")
                                .param("uploadedBy", "tester")
                )
                .andExpect(status().isOk());

        verify(paymentService).uploadPaymentProof(
                eq(1L),
                any(),
                eq(PaymentProof.ProofType.BANK_SLIP),
                eq("test desc"),
                eq("tester")
        );
    }

    @Test
    void uploadPaymentProof_withEmptyFile_shouldReturn400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        mockMvc.perform(
                        multipart("/api/payments/records/{id}/proofs", 2L)
                                .file(emptyFile)
                                .param("proofType", "BANK_SLIP")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadPaymentProof_withTooLargeFile_shouldReturn400() throws Exception {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB > 5MB limit
        MockMultipartFile bigFile = new MockMultipartFile(
                "file",
                "big.png",
                MediaType.IMAGE_PNG_VALUE,
                bigContent
        );

        mockMvc.perform(
                        multipart("/api/payments/records/{id}/proofs", 3L)
                                .file(bigFile)
                                .param("proofType", "BANK_SLIP")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadPaymentProof_whenServiceThrows_shouldReturn500() throws Exception {
        when(paymentService.uploadPaymentProof(
                anyLong(),
                any(),
                any(PaymentProof.ProofType.class),
                any(),
                any())
        ).thenThrow(new RuntimeException("upload fail"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "slip.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/payments/records/{id}/proofs", 4L)
                                .file(file)
                                .param("proofType", "BANK_SLIP")
                )
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 9) GET /api/payments/records/{paymentRecordId}/proofs
    // -------------------------------------------------------
    @Test
    void getPaymentProofsByPaymentRecord_shouldReturnList() throws Exception {
        PaymentProofDto p1 = Mockito.mock(PaymentProofDto.class);
        PaymentProofDto p2 = Mockito.mock(PaymentProofDto.class);

        when(paymentService.getPaymentProofsByPaymentRecord(10L))
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/payments/records/{id}/proofs", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPaymentProofsByPaymentRecord_whenServiceThrows_shouldReturn500() throws Exception {
        when(paymentService.getPaymentProofsByPaymentRecord(11L))
                .thenThrow(new RuntimeException("proof list fail"));

        mockMvc.perform(get("/api/payments/records/{id}/proofs", 11L))
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 10) GET /api/payments/proofs/{paymentProofId}/download
    // -------------------------------------------------------
    @Test
    void downloadPaymentProof_shouldReturnFileAndFilenameInHeader() throws Exception {
        long proofId = 100L;

        ByteArrayResource resource = new ByteArrayResource("file-data".getBytes());
        PaymentProofDto proofDto = Mockito.mock(PaymentProofDto.class);
        when(proofDto.getOriginalFilename()).thenReturn("slip.png");

        when(paymentService.downloadPaymentProof(proofId)).thenReturn(resource);
        when(paymentService.getPaymentProofById(proofId)).thenReturn(proofDto);

        mockMvc.perform(get("/api/payments/proofs/{id}/download", proofId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("slip.png")));
    }

    @Test
    void downloadPaymentProof_whenServiceThrows_shouldReturn404() throws Exception {
        long proofId = 101L;

        when(paymentService.downloadPaymentProof(proofId))
                .thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/payments/proofs/{id}/download", proofId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // 11) DELETE /api/payments/proofs/{paymentProofId}
    // -------------------------------------------------------
    @Test
    void deletePaymentProof_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/payments/proofs/{id}", 200L))
                .andExpect(status().isOk());

        verify(paymentService).deletePaymentProof(200L);
    }

    @Test
    void deletePaymentProof_whenServiceThrows_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("delete proof fail"))
                .when(paymentService).deletePaymentProof(300L);

        mockMvc.perform(delete("/api/payments/proofs/{id}", 300L))
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 12) GET /api/payments/summary/invoice/{invoiceId}
    // -------------------------------------------------------
    @Test
    void getPaymentSummary_shouldReturnTotals() throws Exception {
        long invoiceId = 1L;

        when(paymentService.getTotalPaidAmount(invoiceId))
                .thenReturn(new BigDecimal("1000"));
        when(paymentService.getTotalPendingAmount(invoiceId))
                .thenReturn(new BigDecimal("500"));

        mockMvc.perform(get("/api/payments/summary/invoice/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPaid").value(1000))
                .andExpect(jsonPath("$.totalPending").value(500))
                .andExpect(jsonPath("$.totalReceived").value(1500));
    }

    // -------------------------------------------------------
    // 13) GET /api/payments/proof-types
    // -------------------------------------------------------
    @Test
    void getProofTypes_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/api/payments/proof-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BANK_SLIP").exists());
    }
}
