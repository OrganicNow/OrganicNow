package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.InvoiceServiceImpl;
import com.organicnow.backend.service.OutstandingBalanceService;
import com.organicnow.backend.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * High-coverage test class for InvoiceServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private OutstandingBalanceService outstandingBalanceService;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AssetGroupRepository assetGroupRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Contract baseContract;
    private Room baseRoom;
    private Tenant baseTenant;

    @BeforeEach
    void init() {
        // --------- Default entities used in several tests ---------
        baseRoom = new Room();
        baseRoom.setId(10L);
        baseRoom.setRoomFloor(3);
        baseRoom.setRoomNumber("305");

        baseTenant = new Tenant();
        baseTenant.setId(20L);
        baseTenant.setFirstName("John");
        baseTenant.setLastName("Doe");
        baseTenant.setNationalId("1234567890123");
        baseTenant.setPhoneNumber("0800000000");
        baseTenant.setEmail("john@example.com");

        ContractType ct = new ContractType();
        ct.setName("Basic");

        PackagePlan pp = new PackagePlan();
        pp.setContractType(ct);

        baseContract = new Contract();
        baseContract.setId(5L);
        baseContract.setRoom(baseRoom);
        baseContract.setTenant(baseTenant);
        baseContract.setPackagePlan(pp);
        baseContract.setRentAmountSnapshot(BigDecimal.valueOf(4000));
        baseContract.setSignDate(LocalDate.of(2024, 1, 1).atStartOfDay());
        baseContract.setStartDate(LocalDate.of(2024, 1, 1).atStartOfDay());
        baseContract.setEndDate(LocalDate.of(2024, 12, 31).atStartOfDay());



        // --------- Default stubbing to avoid NPE in convertToDto() ---------
        when(paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(anyLong()))
                .thenReturn(List.of());
        when(paymentRecordRepository.calculateTotalPaidAmount(anyLong()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRecordRepository.calculateTotalPendingAmount(anyLong()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRecordRepository.calculateTotalReceivedAmount(anyLong()))
                .thenReturn(BigDecimal.ZERO);

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(anyLong(), anyInt()))
                .thenReturn(List.of());

        when(assetRepository.findMonthlyAddonFeeByRoomId(anyLong()))
                .thenReturn(List.of());
    }

    // ----------------------------------------------------------------------
    // Helper: generate Invoice saved by repository
    // ----------------------------------------------------------------------
    private Invoice savedInvoice(Invoice inv, long id) {
        inv.setId(id);
        return inv;
    }

    private CreateInvoiceRequest buildBasicCreateRequest() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setContractId(baseContract.getId());
        req.setPackageId(1L);
        req.setFloor("3");
        req.setRoom("305");
        req.setRentAmount(4000);
        req.setWaterUnit(10);
        req.setWaterRate(30);
        req.setElectricityUnit(50);
        req.setElectricityRate(8);
        req.setCreateDate("2024-01-01"); // เกิน 30 วัน เพื่อให้เป็น overdue
        req.setPenaltyTotal(0);
        req.setIncludeOutstandingBalance(false);
        return req;
    }

    // ----------------------------------------------------------------------
    // createInvoice(): regular path (no outstanding)
    // ----------------------------------------------------------------------
    @Test
    void createInvoice_regular_shouldCalculateAmountsAndAutoPenaltyIfOverdue() {
        CreateInvoiceRequest req = buildBasicCreateRequest();

        when(contractRepository.findById(baseContract.getId()))
                .thenReturn(Optional.of(baseContract));

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> savedInvoice(invocation.getArgument(0), 1L));

        InvoiceDto dto = invoiceService.createInvoice(req);

        // Verify mapping
        assertNotNull(dto.getId());
        assertEquals("305", dto.getRoom());
        assertEquals(3, dto.getFloor());
        assertEquals(4000, dto.getRent());
        assertEquals(10, dto.getWaterUnit());
        assertEquals(300, dto.getWater());              // 10 * 30
        assertEquals(50, dto.getElectricityUnit());
        assertEquals(400, dto.getElectricity());        // 50 * 8

        // penalty auto 10% ของค่าเช่า
        assertTrue(dto.getPenaltyTotal() > 0);
        assertEquals(dto.getSubTotal() + dto.getPenaltyTotal(), dto.getNetAmount());
    }

    // ----------------------------------------------------------------------
    // createInvoice(): includeOutstandingBalance = true
    // ----------------------------------------------------------------------
    @Test
    void createInvoice_withOutstandingBalance_shouldUseOutstandingBalanceService() {

        // ----- Prepare request -----
        CreateInvoiceRequest req = buildBasicCreateRequest();
        req.setIncludeOutstandingBalance(true);
        req.setRentAmount(5000);
        req.setWater(300);
        req.setElectricity(400);

        // ----- Expected calculations -----
        int expectedCurrentCharges = 5000 + 300 + 400;

        // ----- Mock OutstandingBalanceService -----
        Invoice invFromObs = new Invoice();
        invFromObs.setId(100L);
        invFromObs.setContact(baseContract);
        invFromObs.setRequestedRent(5000);
        invFromObs.setRequestedWater(300);
        invFromObs.setRequestedElectricity(400);
        invFromObs.setPenaltyTotal(0);
        invFromObs.setCreateDate(LocalDateTime.now());

        when(outstandingBalanceService.createInvoiceWithOutstandingBalance(
                eq(baseContract.getId()),
                eq(expectedCurrentCharges)
        )).thenReturn(invFromObs);

        // ----- Mock repository save -----
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(inv -> {
                    Invoice i = inv.getArgument(0);
                    i.setId(100L);  // simulate DB generated ID
                    return i;
                });

        // ----- Call service -----
        InvoiceDto dto = invoiceService.createInvoice(req);

        // ----- Verify -----
        verify(outstandingBalanceService)
                .createInvoiceWithOutstandingBalance(eq(baseContract.getId()), eq(expectedCurrentCharges));

        assertEquals(100L, dto.getId());
        assertEquals(5000, dto.getRent());
        assertEquals(300, dto.getWater());
        assertEquals(400, dto.getElectricity());
        assertEquals(5700, dto.getNetAmount());  // 5000+300+400
    }


    // ----------------------------------------------------------------------
    // getInvoiceById()
    // ----------------------------------------------------------------------
    @Test
    void getInvoiceById_shouldReturnDtoWhenFound() {
        Invoice inv = new Invoice();
        inv.setId(10L);
        inv.setContact(baseContract);
        inv.setCreateDate(LocalDateTime.now());
        inv.setRequestedRent(2000);
        inv.setRequestedWater(300);
        inv.setRequestedElectricity(65);
        inv.setPenaltyTotal(100);

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(inv));

        Optional<InvoiceDto> result = invoiceService.getInvoiceById(10L);

        assertTrue(result.isPresent());
        InvoiceDto dto = result.get();
        assertEquals(10L, dto.getId());
        assertEquals(2000, dto.getRent());
        assertEquals(300, dto.getWater());
        assertEquals(65, dto.getElectricity());
        assertEquals(2365, dto.getSubTotal());
        assertEquals(2465, dto.getNetAmount());
    }

    // ----------------------------------------------------------------------
    // getAllInvoices()
    // ----------------------------------------------------------------------
    @Test
    void getAllInvoices_shouldReturnDtosAndCallRepositories() {

        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setContact(baseContract);
        inv.setRequestedRent(1000);
        inv.setRequestedWater(0);
        inv.setRequestedElectricity(0);
        inv.setPenaltyTotal(0);
        inv.setSubTotal(1000);
        inv.setNetAmount(1000);
        inv.setCreateDate(LocalDateTime.now());

        // สำคัญ! ต้องมี dueDate ไม่งั้น updatePenalty จะ NPE
        inv.setDueDate(LocalDateTime.now().plusDays(5));

        when(invoiceRepository.findAll()).thenReturn(List.of(inv));

        List<InvoiceDto> list = invoiceService.getAllInvoices();
        assertEquals(1, list.size());

        InvoiceDto dto = list.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(1000, dto.getRent());
        assertEquals(1000, dto.getSubTotal());
        assertEquals(1000, dto.getNetAmount());
    }

    // ----------------------------------------------------------------------
    // updateInvoice(): recalc water/elec & net amount, set payDate
    // ----------------------------------------------------------------------
    @Test
    void updateInvoice_shouldRecalculateAmountsAndNetAndSetPayDateWhenPaid() {
        Invoice inv = new Invoice();
        inv.setId(10L);
        inv.setContact(baseContract);
        inv.setCreateDate(LocalDateTime.now().minusDays(10));
        inv.setDueDate(LocalDateTime.now().minusDays(5));
        inv.setInvoiceStatus(0);
        inv.setRequestedRent(2000);
        inv.setSubTotal(2000);
        inv.setPenaltyTotal(0);
        inv.setNetAmount(2000);

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(i -> i.getArgument(0));

        UpdateInvoiceRequest req = new UpdateInvoiceRequest();
        req.setInvoiceStatus(1); // paid
        req.setSubTotal(3000);
        req.setPenaltyTotal(50);
        req.setWaterUnit(10);        // => 10 * 30 = 300
        req.setElectricityUnit(20);  // => 20 * 6.5 ≈ 130

        InvoiceDto dto = invoiceService.updateInvoice(10L, req);

        assertEquals(1, dto.getInvoiceStatus());
        assertNotNull(dto.getPayDate());
        assertEquals(10, dto.getWaterUnit());
        assertEquals(300, dto.getWater());
        assertEquals(20, dto.getElectricityUnit());
        assertEquals(130, dto.getElectricity());
        assertEquals(dto.getSubTotal() + dto.getPenaltyTotal(), dto.getNetAmount());
    }

    // ----------------------------------------------------------------------
    // deleteInvoice(): success path
    // ----------------------------------------------------------------------
    @Test
    void deleteInvoice_shouldDeletePaymentRecordsThenInvoice() {
        long id = 88L;
        when(invoiceRepository.existsById(id)).thenReturn(true);

        invoiceService.deleteInvoice(id);

        InOrder inOrder = inOrder(paymentRecordRepository, invoiceRepository);
        inOrder.verify(paymentRecordRepository).deleteByInvoiceId(id);
        inOrder.verify(invoiceRepository).deleteById(id);
    }

    // ----------------------------------------------------------------------
    // deleteInvoice(): not found
    // ----------------------------------------------------------------------
    @Test
    void deleteInvoice_shouldThrowWhenInvoiceNotFound() {
        when(invoiceRepository.existsById(999L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> invoiceService.deleteInvoice(999L));

        assertTrue(ex.getMessage().contains("Invoice not found"));
    }

    // ----------------------------------------------------------------------
    // updateOverduePenalties()
    // ----------------------------------------------------------------------
    @Test
    void updateOverduePenalties_shouldApplyPenaltyForOverdueIncompleteInvoices() {
        Invoice overdue = new Invoice();
        overdue.setId(1L);
        overdue.setContact(baseContract);
        overdue.setRequestedRent(1000);
        overdue.setSubTotal(1000);
        overdue.setPenaltyTotal(0);
        overdue.setInvoiceStatus(0);
        overdue.setDueDate(LocalDateTime.now().minusDays(2));

        Invoice notOverdue = new Invoice();
        notOverdue.setId(2L);
        notOverdue.setContact(baseContract);
        notOverdue.setRequestedRent(1000);
        notOverdue.setSubTotal(1000);
        notOverdue.setPenaltyTotal(0);
        notOverdue.setInvoiceStatus(0);
        notOverdue.setDueDate(LocalDateTime.now().plusDays(2));

        when(invoiceRepository.findAll()).thenReturn(List.of(overdue, notOverdue));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(i -> i.getArgument(0));

        invoiceService.updateOverduePenalties();

        assertEquals(100, overdue.getPenaltyTotal()); // 10% of 1000
        assertEquals(1100, overdue.getNetAmount());
        assertEquals(0, notOverdue.getPenaltyTotal());
    }

    // ----------------------------------------------------------------------
    // CSV import: happy path + one invalid line
    // ----------------------------------------------------------------------
    @Test
    void importUtilityUsageFromCsv_shouldProcessValidAndInvalidLines() {
        String csvContent =
                "RoomNumber,WaterUsage,ElectricityUsage,BillingMonth,WaterRate,ElectricityRate\n" +
                        "101,10,20,2025-02,30,8\n" +         // valid
                        "BAD_LINE";                          // invalid format

        MockMultipartFile file = new MockMultipartFile(
                "file", "usage.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        Room room = new Room();
        room.setId(50L);
        room.setRoomNumber("101");
        room.setRoomFloor(2);

        when(roomRepository.findByRoomNumber("101"))
                .thenReturn(Optional.of(room));

        when(contractRepository.findActiveContractByRoomId(50L))
                .thenReturn(Optional.of(baseContract));

        Invoice existing = new Invoice();
        existing.setId(999L);
        existing.setContact(baseContract);
        existing.setRequestedRent(5000);
        existing.setPenaltyTotal(0);
        existing.setSubTotal(0);
        existing.setNetAmount(0);

        // month 2025-02 -> start/end range
        when(invoiceRepository.findByContractAndDateRange(eq(baseContract.getId()),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existing));

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(i -> i.getArgument(0));

        String result = invoiceService.importUtilityUsageFromCsv(file);

        assertTrue(result.contains("Successful: 1"));
        assertTrue(result.contains("Errors: 1"));
        assertTrue(result.contains("Line 3"));
        // verify invoice values updated
        assertEquals(10, existing.getRequestedWaterUnit());
        assertEquals(20, existing.getRequestedElectricityUnit());
        assertEquals(10 * 30, existing.getRequestedWater());
        assertEquals(20 * 8, existing.getRequestedElectricity());
        assertTrue(existing.getSubTotal() > 0);
        assertEquals(existing.getSubTotal(),
                existing.getNetAmount()); // no penalty
    }

    // ----------------------------------------------------------------------
    // CSV import: room not found & contract not found
    // ----------------------------------------------------------------------
    @Test
    void importUtilityUsageFromCsv_shouldHandleMissingRoomOrContract() {
        String csvContent =
                "RoomNumber,WaterUsage,ElectricityUsage,BillingMonth\n" +
                        "999,5,5,2025-03\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usage2.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        // room not found -> Optional.empty
        when(roomRepository.findByRoomNumber("999"))
                .thenReturn(Optional.empty());

        String result = invoiceService.importUtilityUsageFromCsv(file);

        assertTrue(result.contains("Errors: 1"));
        assertTrue(result.contains("ไม่พบห้องหมายเลข 999"));
    }

    // ----------------------------------------------------------------------
    // generateInvoicePdf()
    // ----------------------------------------------------------------------
    @Test
    void generateInvoicePdf_shouldReturnNonEmptyPdfBytes() {
        long invoiceId = 50L;

        Invoice inv = new Invoice();
        inv.setId(invoiceId);
        inv.setContact(baseContract);
        inv.setCreateDate(LocalDateTime.of(2025, 2, 1, 0, 0));
        inv.setDueDate(LocalDateTime.of(2025, 2, 15, 23, 59));
        inv.setRequestedRent(5000);
        inv.setRequestedWaterUnit(10);
        inv.setRequestedWater(300);
        inv.setRequestedElectricityUnit(30);
        inv.setRequestedElectricity(240);
        inv.setPenaltyTotal(0);
        inv.setNetAmount(5540);
        inv.setPreviousBalance(0);

        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(inv));
        // flush() is void – doNothing() is default

        when(roomRepository.findByRoomFloorAndRoomNumber(2, "201"))
                .thenReturn(Optional.of(baseRoom));

        // call
        byte[] pdf = invoiceService.generateInvoicePdf(invoiceId);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    // ----------------------------------------------------------------------
    // createInvoice(): path with includeOutstandingBalance false but without contractId
    // (should use placeholder contract when available)
    // ----------------------------------------------------------------------
    @Test
    void createInvoice_withoutContractId_shouldUsePlaceholderContract() {
        CreateInvoiceRequest req = buildBasicCreateRequest();
        req.setContractId(null); // ไม่มี contractId
        req.setFloor("3");
        req.setRoom("305");

        // มี contract ในระบบ 1 อัน -> ใช้เป็น placeholder
        when(contractRepository.findAll())
                .thenReturn(List.of(baseContract));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> savedInvoice(invocation.getArgument(0), 20L));

        InvoiceDto dto = invoiceService.createInvoice(req);

        assertEquals(20L, dto.getId());
        assertEquals("305", dto.getRoom());
        assertEquals(3, dto.getFloor());
    }

    // ----------------------------------------------------------------------
    // Tiny methods that currently return defaults / throw
    // ----------------------------------------------------------------------
    @Test
    void searchInvoices_shouldReturnEmptyList() {
        assertTrue(invoiceService.searchInvoices("anything").isEmpty());
    }

    @Test
    void getInvoicesByContractId_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByContractId(1L).isEmpty());
    }

    @Test
    void getInvoicesByRoomId_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByRoomId(1L).isEmpty());
    }

    @Test
    void getInvoicesByTenantId_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByTenantId(1L).isEmpty());
    }

    @Test
    void getInvoicesByStatus_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByStatus(1).isEmpty());
    }

    @Test
    void getUnpaidInvoices_shouldReturnEmptyList() {
        assertTrue(invoiceService.getUnpaidInvoices().isEmpty());
    }

    @Test
    void getPaidInvoices_shouldReturnEmptyList() {
        assertTrue(invoiceService.getPaidInvoices().isEmpty());
    }

    @Test
    void getOverdueInvoices_shouldReturnEmptyList() {
        assertTrue(invoiceService.getOverdueInvoices().isEmpty());
    }

    @Test
    void getInvoicesByDateRange_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByDateRange(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()).isEmpty());
    }

    @Test
    void getInvoicesByNetAmountRange_shouldReturnEmptyList() {
        assertTrue(invoiceService.getInvoicesByNetAmountRange(0, 1000).isEmpty());
    }

    @Test
    void markAsPaid_shouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class,
                () -> invoiceService.markAsPaid(1L));
    }

    @Test
    void cancelInvoice_shouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class,
                () -> invoiceService.cancelInvoice(1L));
    }

    @Test
    void addPenalty_shouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class,
                () -> invoiceService.addPenalty(1L, 100));
    }
}
