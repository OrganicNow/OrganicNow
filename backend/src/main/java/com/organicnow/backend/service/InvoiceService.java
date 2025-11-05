package com.organicnow.backend.service;

import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface InvoiceService {

    // CRUD operations
    List<InvoiceDto> getAllInvoices();
    Optional<InvoiceDto> getInvoiceById(Long id);
    InvoiceDto createInvoice(CreateInvoiceRequest request);
    InvoiceDto updateInvoice(Long id, UpdateInvoiceRequest request);
    void deleteInvoice(Long id);

    // Search and filter operationsa
    List<InvoiceDto> searchInvoices(String query);
    List<InvoiceDto> getInvoicesByContractId(Long contractId);
    List<InvoiceDto> getInvoicesByRoomId(Long roomId);
    List<InvoiceDto> getInvoicesByTenantId(Long tenantId);
    List<InvoiceDto> getInvoicesByStatus(Integer status);
    List<InvoiceDto> getUnpaidInvoices();
    List<InvoiceDto> getPaidInvoices();
    List<InvoiceDto> getOverdueInvoices();
    List<InvoiceDto> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<InvoiceDto> getInvoicesByNetAmountRange(Integer minAmount, Integer maxAmount);

    // Payment operations
    InvoiceDto markAsPaid(Long id);
    InvoiceDto cancelInvoice(Long id);
    InvoiceDto addPenalty(Long id, Integer penaltyAmount);
    
    // ===== CSV Import Feature =====
    
    // Import utility usage from CSV file
    String importUtilityUsageFromCsv(MultipartFile file);
    
    // ===== PDF Generation Feature =====
    
    // Generate invoice PDF
    byte[] generateInvoicePdf(Long invoiceId);
}
