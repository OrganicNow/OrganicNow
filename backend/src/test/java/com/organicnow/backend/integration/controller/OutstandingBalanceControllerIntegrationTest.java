package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.OutstandingBalanceController;
import com.organicnow.backend.model.PaymentRecord;
import com.organicnow.backend.service.OutstandingBalanceService;
import com.organicnow.backend.service.OutstandingBalanceService.OutstandingBalanceSummary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OutstandingBalanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class OutstandingBalanceControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OutstandingBalanceService outstandingBalanceService;

    // -------------------------------------------------------
    // 1) GET /outstanding-balance/contract/{contractId}/summary
    // -------------------------------------------------------
    @Test
    void getOutstandingBalanceSummary_shouldReturn200AndBody() throws Exception {
        Long contractId = 1L;
        // ใช้ mock object เพราะเราไม่รู้ constructor ของ OutstandingBalanceSummary
        OutstandingBalanceSummary summary = Mockito.mock(OutstandingBalanceSummary.class);

        when(outstandingBalanceService.getOutstandingBalanceSummary(contractId))
                .thenReturn(summary);

        mockMvc.perform(get("/outstanding-balance/contract/{id}/summary", contractId))
                .andExpect(status().isOk());
        // ไม่ assert field เพราะไม่รู้ structure ภายในของ Summary
    }

    @Test
    void getOutstandingBalanceSummary_whenServiceThrows_shouldReturn400() throws Exception {
        Long contractId = 99L;

        when(outstandingBalanceService.getOutstandingBalanceSummary(contractId))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/outstanding-balance/contract/{id}/summary", contractId))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 2) GET /outstanding-balance/contract/{contractId}/invoices
    // -------------------------------------------------------
    @Test
    void getOutstandingInvoices_shouldReturnListFromService() throws Exception {
        Long contractId = 1L;

        // เราไม่สน field ภายใน invoice แค่ให้มี 2 ตัว
        var invoice1 = Mockito.mock(com.organicnow.backend.model.Invoice.class);
        var invoice2 = Mockito.mock(com.organicnow.backend.model.Invoice.class);

        when(outstandingBalanceService.getOutstandingInvoices(contractId))
                .thenReturn(List.of(invoice1, invoice2));

        mockMvc.perform(get("/outstanding-balance/contract/{id}/invoices", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOutstandingInvoices_whenServiceThrows_shouldReturn400() throws Exception {
        Long contractId = 1L;

        when(outstandingBalanceService.getOutstandingInvoices(contractId))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/outstanding-balance/contract/{id}/invoices", contractId))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 3) GET /outstanding-balance/contract/{contractId}/calculate
    // -------------------------------------------------------
    @Test
    void calculateOutstandingBalance_shouldWrapInJsonObject() throws Exception {
        Long contractId = 1L;

        when(outstandingBalanceService.calculateOutstandingBalance(contractId))
                .thenReturn(5000);

        mockMvc.perform(get("/outstanding-balance/contract/{id}/calculate", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outstandingBalance").value(5000));
    }

    @Test
    void calculateOutstandingBalance_whenServiceThrows_shouldReturn400() throws Exception {
        Long contractId = 1L;

        when(outstandingBalanceService.calculateOutstandingBalance(contractId))
                .thenThrow(new RuntimeException("calc error"));

        mockMvc.perform(get("/outstanding-balance/contract/{id}/calculate", contractId))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 4) GET /outstanding-balance/calculate/{contractId}
    // -------------------------------------------------------
    @Test
    void calculateOutstandingBalanceShort_shouldReturnIntegerBody() throws Exception {
        Long contractId = 2L;

        when(outstandingBalanceService.calculateOutstandingBalance(contractId))
                .thenReturn(1234);

        mockMvc.perform(get("/outstanding-balance/calculate/{id}", contractId))
                .andExpect(status().isOk())
                .andExpect(content().string("1234"));
    }

    @Test
    void calculateOutstandingBalanceShort_whenServiceThrows_shouldReturn400() throws Exception {
        Long contractId = 2L;

        when(outstandingBalanceService.calculateOutstandingBalance(contractId))
                .thenThrow(new RuntimeException("short calc error"));

        mockMvc.perform(get("/outstanding-balance/calculate/{id}", contractId))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 5) POST /outstanding-balance/invoice/{invoiceId}/payment
    // -------------------------------------------------------
    @Test
    void recordPayment_shouldCallServiceAndReturnPaymentRecord() throws Exception {
        Long invoiceId = 10L;

        PaymentRecord paymentRecord = new PaymentRecord();
        // สมมติว่ามี setter ของ id
        paymentRecord.setId(100L);
        paymentRecord.setPaymentAmount(new BigDecimal("1500.50"));

        when(outstandingBalanceService.recordPayment(
                eq(invoiceId),
                eq(new BigDecimal("1500.50")),
                eq(PaymentRecord.PaymentMethod.CASH),
                eq("first payment")
        )).thenReturn(paymentRecord);

        String jsonBody = """
                {
                  "paymentAmount": 1500.50,
                  "paymentMethod": "CASH",
                  "notes": "first payment"
                }
                """;

        mockMvc.perform(
                        post("/outstanding-balance/invoice/{invoiceId}/payment", invoiceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.paymentAmount").value(1500.50));
    }

    @Test
    void recordPayment_whenServiceThrows_shouldReturn400() throws Exception {
        Long invoiceId = 10L;

        doThrow(new RuntimeException("payment failed")).when(outstandingBalanceService)
                .recordPayment(any(Long.class), any(BigDecimal.class), any(PaymentRecord.PaymentMethod.class), any(String.class));

        String jsonBody = """
                {
                  "paymentAmount": 999.00,
                  "paymentMethod": "BANK_TRANSFER",
                  "notes": "test failed"
                }
                """;

        mockMvc.perform(
                        post("/outstanding-balance/invoice/{invoiceId}/payment", invoiceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                )
                .andExpect(status().isBadRequest());
    }
}
