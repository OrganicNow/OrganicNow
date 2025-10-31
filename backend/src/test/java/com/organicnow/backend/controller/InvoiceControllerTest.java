//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.CreateInvoiceRequest;
//import com.organicnow.backend.dto.InvoiceDto;
//import com.organicnow.backend.dto.UpdateInvoiceRequest;
//import com.organicnow.backend.service.InvoiceService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class InvoiceControllerTest {
//
//    @Mock
//    private InvoiceService invoiceService;
//
//    @InjectMocks
//    private InvoiceController invoiceController;
//
//    private InvoiceDto sampleInvoice;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        sampleInvoice = new InvoiceDto();
//        try {
//            sampleInvoice.getClass().getMethod("setInvoiceId", Long.class).invoke(sampleInvoice, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleInvoice.getClass().getMethod("setStatus", Integer.class).invoke(sampleInvoice, 1);
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ Get all invoices
//    @Test
//    void testGetAllInvoices() {
//        when(invoiceService.getAllInvoices()).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getAllInvoices();
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(1, response.getBody().size());
//        verify(invoiceService).getAllInvoices();
//    }
//
//    // ✅ Get invoice by ID (found)
//    @Test
//    void testGetInvoiceByIdFound() {
//        when(invoiceService.getInvoiceById(1L)).thenReturn(Optional.of(sampleInvoice));
//        ResponseEntity<InvoiceDto> response = invoiceController.getInvoiceById(1L);
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        verify(invoiceService).getInvoiceById(1L);
//    }
//
//    // ✅ Get invoice by ID (not found)
//    @Test
//    void testGetInvoiceByIdNotFound() {
//        when(invoiceService.getInvoiceById(99L)).thenReturn(Optional.empty());
//        ResponseEntity<InvoiceDto> response = invoiceController.getInvoiceById(99L);
//        assertEquals(404, response.getStatusCode().value());
//        verify(invoiceService).getInvoiceById(99L);
//    }
//
//    // ✅ Search invoices
//    @Test
//    void testSearchInvoices() {
//        when(invoiceService.searchInvoices("electric")).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.searchInvoices("electric");
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).searchInvoices("electric");
//    }
//
//    // ✅ Get invoices by contract ID
//    @Test
//    void testGetInvoicesByContractId() {
//        when(invoiceService.getInvoicesByContractId(1L)).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getInvoicesByContractId(1L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByContractId(1L);
//    }
//
//    // ✅ Get invoices by room ID
//    @Test
//    void testGetInvoicesByRoomId() {
//        when(invoiceService.getInvoicesByRoomId(2L)).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getInvoicesByRoomId(2L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByRoomId(2L);
//    }
//
//    // ✅ Get invoices by tenant ID
//    @Test
//    void testGetInvoicesByTenantId() {
//        when(invoiceService.getInvoicesByTenantId(3L)).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getInvoicesByTenantId(3L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByTenantId(3L);
//    }
//
//    // ✅ Get invoices by status
//    @Test
//    void testGetInvoicesByStatus() {
//        when(invoiceService.getInvoicesByStatus(1)).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getInvoicesByStatus(1);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByStatus(1);
//    }
//
//    // ✅ Get unpaid invoices
//    @Test
//    void testGetUnpaidInvoices() {
//        when(invoiceService.getUnpaidInvoices()).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getUnpaidInvoices();
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getUnpaidInvoices();
//    }
//
//    // ✅ Get paid invoices
//    @Test
//    void testGetPaidInvoices() {
//        when(invoiceService.getPaidInvoices()).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getPaidInvoices();
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getPaidInvoices();
//    }
//
//    // ✅ Get overdue invoices
//    @Test
//    void testGetOverdueInvoices() {
//        when(invoiceService.getOverdueInvoices()).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getOverdueInvoices();
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getOverdueInvoices();
//    }
//
//    // ✅ Create new invoice
//    @Test
//    void testCreateInvoice() {
//        CreateInvoiceRequest req = new CreateInvoiceRequest();
//        when(invoiceService.createInvoice(req)).thenReturn(sampleInvoice);
//        ResponseEntity<?> response = invoiceController.createInvoice(req);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).createInvoice(req);
//    }
//
//    // ✅ Update invoice
//    @Test
//    void testUpdateInvoice() {
//        UpdateInvoiceRequest req = new UpdateInvoiceRequest();
//        when(invoiceService.updateInvoice(1L, req)).thenReturn(sampleInvoice);
//        ResponseEntity<InvoiceDto> response = invoiceController.updateInvoice(1L, req);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).updateInvoice(1L, req);
//    }
//
//    // ✅ Mark as paid
//    @Test
//    void testMarkAsPaid() {
//        when(invoiceService.markAsPaid(1L)).thenReturn(sampleInvoice);
//        ResponseEntity<InvoiceDto> response = invoiceController.markAsPaid(1L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).markAsPaid(1L);
//    }
//
//    // ✅ Cancel invoice
//    @Test
//    void testCancelInvoice() {
//        when(invoiceService.cancelInvoice(1L)).thenReturn(sampleInvoice);
//        ResponseEntity<InvoiceDto> response = invoiceController.cancelInvoice(1L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).cancelInvoice(1L);
//    }
//
//    // ✅ Add penalty
//    @Test
//    void testAddPenalty() {
//        when(invoiceService.addPenalty(1L, 200)).thenReturn(sampleInvoice);
//        ResponseEntity<InvoiceDto> response = invoiceController.addPenalty(1L, 200);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).addPenalty(1L, 200);
//    }
//
//    // ✅ Delete invoice
//    @Test
//    void testDeleteInvoice() {
//        doNothing().when(invoiceService).deleteInvoice(1L);
//        ResponseEntity<Void> response = invoiceController.deleteInvoice(1L);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).deleteInvoice(1L);
//    }
//
//    // ✅ Get invoices by date range
//    @Test
//    void testGetInvoicesByDateRange() {
//        when(invoiceService.getInvoicesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
//                .thenReturn(List.of(sampleInvoice));
//
//        ResponseEntity<List<InvoiceDto>> response =
//                invoiceController.getInvoicesByDateRange("2025-01-01", "2025-01-31");
//
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
//    }
//
//    // ✅ Get invoices by net amount range
//    @Test
//    void testGetInvoicesByNetAmountRange() {
//        when(invoiceService.getInvoicesByNetAmountRange(1000, 5000)).thenReturn(List.of(sampleInvoice));
//        ResponseEntity<List<InvoiceDto>> response = invoiceController.getInvoicesByNetAmountRange(1000, 5000);
//        assertEquals(200, response.getStatusCode().value());
//        verify(invoiceService).getInvoicesByNetAmountRange(1000, 5000);
//    }
//}
