package com.organicnow.backend.controller;

import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import com.organicnow.backend.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/invoice")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4173", "http://app.localtest.me"}, allowCredentials = "true")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Get all invoices
    @GetMapping("/list")
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        try {
            List<InvoiceDto> invoices = invoiceService.getAllInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        try {
            Optional<InvoiceDto> invoice = invoiceService.getInvoiceById(id);
            return invoice.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Search invoices
    @GetMapping("/search")
    public ResponseEntity<List<InvoiceDto>> searchInvoices(@RequestParam String query) {
        try {
            List<InvoiceDto> invoices = invoiceService.searchInvoices(query);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by contract ID
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByContractId(@PathVariable Long contractId) {
        try {
            List<InvoiceDto> invoices = invoiceService.getInvoicesByContractId(contractId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by room ID
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByRoomId(@PathVariable Long roomId) {
        try {
            List<InvoiceDto> invoices = invoiceService.getInvoicesByRoomId(roomId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by tenant ID
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByTenantId(@PathVariable Long tenantId) {
        try {
            List<InvoiceDto> invoices = invoiceService.getInvoicesByTenantId(tenantId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByStatus(@PathVariable Integer status) {
        try {
            List<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(status);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get unpaid invoices
    @GetMapping("/unpaid")
    public ResponseEntity<List<InvoiceDto>> getUnpaidInvoices() {
        try {
            List<InvoiceDto> invoices = invoiceService.getUnpaidInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get paid invoices
    @GetMapping("/paid")
    public ResponseEntity<List<InvoiceDto>> getPaidInvoices() {
        try {
            List<InvoiceDto> invoices = invoiceService.getPaidInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get overdue invoices
    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        try {
            List<InvoiceDto> invoices = invoiceService.getOverdueInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create new invoice
    @PostMapping("/create")
    public ResponseEntity<?> createInvoice(@RequestBody CreateInvoiceRequest request) {
        try {
            InvoiceDto saved = invoiceService.createInvoice(request);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Create invoice failed: " + e.getMessage());
        }
    }

    // Update invoice
    @PutMapping("/update/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @RequestBody UpdateInvoiceRequest request) {
        try {
            InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, request);
            return ResponseEntity.ok(updatedInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Mark invoice as paid
    @PutMapping("/pay/{id}")
    public ResponseEntity<InvoiceDto> markAsPaid(@PathVariable Long id) {
        try {
            InvoiceDto paidInvoice = invoiceService.markAsPaid(id);
            return ResponseEntity.ok(paidInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cancel invoice
    @PutMapping("/cancel/{id}")
    public ResponseEntity<InvoiceDto> cancelInvoice(@PathVariable Long id) {
        try {
            InvoiceDto cancelledInvoice = invoiceService.cancelInvoice(id);
            return ResponseEntity.ok(cancelledInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Add penalty to invoice
    @PutMapping("/penalty/{id}")
    public ResponseEntity<InvoiceDto> addPenalty(@PathVariable Long id, @RequestParam Integer penaltyAmount) {
        try {
            InvoiceDto invoiceWithPenalty = invoiceService.addPenalty(id, penaltyAmount);
            return ResponseEntity.ok(invoiceWithPenalty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete invoice
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            List<InvoiceDto> invoices = invoiceService.getInvoicesByDateRange(start, end);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get invoices by net amount range
    @GetMapping("/amount-range")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByNetAmountRange(
            @RequestParam Integer minAmount,
            @RequestParam Integer maxAmount) {
        try {
            List<InvoiceDto> invoices = invoiceService.getInvoicesByNetAmountRange(minAmount, maxAmount);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
