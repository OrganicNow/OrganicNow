package com.organicnow.backend.unit.service;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.OutstandingBalanceService;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OutstandingBalanceServiceTest {

    private AutoCloseable closeable;

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private OutstandingBalanceService service;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // =====================================================================
    // 1) calculateOutstandingBalance()
    // =====================================================================

    @Test
    void testCalculateOutstandingBalance_FullCalculation() {

        Invoice inv = Invoice.builder()
                .id(1L)
                .requestedRent(2000)
                .requestedWater(100)
                .requestedElectricity(200)
                .penaltyTotal(50)
                .invoiceStatus(0)
                .requestedFloor(2)
                .requestedRoom("201")
                .build();

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(10L, 0))
                .thenReturn(List.of(inv));

        when(paymentRecordRepository.calculateTotalReceivedAmount(1L))
                .thenReturn(new BigDecimal("1000"));

        Room room = Room.builder().id(99L).build();
        when(roomRepository.findByRoomFloorAndRoomNumber(2, "201"))
                .thenReturn(Optional.of(room));

        when(assetRepository.findMonthlyAddonFeeByRoomId(99L))
                .thenReturn(
                        List.<Object[]>of(
                                new Object[]{ new BigDecimal("300") }
                        )
                );
        // addon = 300

        Integer result = service.calculateOutstandingBalance(10L);

        // subTotal = 2000 + 100 + 200 + 300 = 2600
        // + penalty 50 = 2650
        // paid = 1000 → remain 1650
        assertEquals(1650, result);
    }

    @Test
    void testCalculateOutstandingBalance_NoAddonFee() {

        Invoice inv = Invoice.builder()
                .id(2L)
                .requestedRent(1500)
                .requestedFloor(1)
                .requestedRoom("101")
                .invoiceStatus(0)
                .build();

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(10L, 0))
                .thenReturn(List.of(inv));

        when(paymentRecordRepository.calculateTotalReceivedAmount(2L))
                .thenReturn(null); // paid = 0

        when(roomRepository.findByRoomFloorAndRoomNumber(1, "101"))
                .thenReturn(Optional.empty());  // ❌ no room → addon = 0

        Integer result = service.calculateOutstandingBalance(10L);

        assertEquals(1500, result);
    }

    @Test
    void testCalculateOutstandingBalance_MarkInvoiceAsPaid() {

        Invoice inv = Invoice.builder()
                .id(3L)
                .requestedRent(1000)
                .invoiceStatus(0)
                .build();

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(20L, 0))
                .thenReturn(List.of(inv));

        when(paymentRecordRepository.calculateTotalReceivedAmount(3L))
                .thenReturn(new BigDecimal("1000")); // paid fully

        Integer result = service.calculateOutstandingBalance(20L);

        assertEquals(0, result);
        assertEquals(1, inv.getInvoiceStatus());  // ✔ marked as paid
        verify(invoiceRepository).save(inv);
    }

    @Test
    void testCalculateOutstandingBalance_InvalidFloor() {

        Invoice inv = Invoice.builder()
                .id(5L)
                .requestedRent(800)
                .requestedFloor(null)
                .requestedRoom("202")
                .invoiceStatus(0)
                .build();

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(30L, 0))
                .thenReturn(List.of(inv));

        when(paymentRecordRepository.calculateTotalReceivedAmount(5L))
                .thenReturn(null);

        Integer result = service.calculateOutstandingBalance(30L);

        assertEquals(800, result);   // addon = 0
    }

    @Test
    void testCalculateOutstandingBalance_MultipleInvoices() {

        Invoice inv1 = Invoice.builder()
                .id(11L)
                .requestedRent(1000)
                .invoiceStatus(0)
                .requestedFloor(1)
                .requestedRoom("A")
                .build();

        Invoice inv2 = Invoice.builder()
                .id(12L)
                .requestedRent(2000)
                .invoiceStatus(0)
                .requestedFloor(2)
                .requestedRoom("B")
                .build();

        when(invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(77L, 0))
                .thenReturn(List.of(inv1, inv2));

        when(paymentRecordRepository.calculateTotalReceivedAmount(11L))
                .thenReturn(new BigDecimal("500")); // remain 500

        when(paymentRecordRepository.calculateTotalReceivedAmount(12L))
                .thenReturn(new BigDecimal("700")); // remain 1300

        Room r1 = Room.builder().id(1L).build();
        Room r2 = Room.builder().id(2L).build();
        when(roomRepository.findByRoomFloorAndRoomNumber(anyInt(), anyString()))
                .thenReturn(Optional.of(r1))    // first invoice
                .thenReturn(Optional.of(r2));   // second invoice

        when(assetRepository.findMonthlyAddonFeeByRoomId(anyLong()))
                .thenReturn(
                        List.<Object[]>of(
                                new Object[]{ new BigDecimal("100") }
                        )
                );


        Integer result = service.calculateOutstandingBalance(77L);

        assertEquals(600 + 1400, result);  // = 2000

    }

    // =====================================================================
    // 2) createInvoiceWithOutstandingBalance()
    // =====================================================================

    @Test
    void testCreateInvoiceWithOutstandingBalance() {

        // mock outstanding
        OutstandingBalanceService spyService = Mockito.spy(service);
        doReturn(400).when(spyService).calculateOutstandingBalance(5L);

        Contract c = new Contract();
        when(contractRepository.findById(5L)).thenReturn(Optional.of(c));

        Invoice saved = new Invoice();
        saved.setId(999L);
        when(invoiceRepository.save(any())).thenReturn(saved);

        Invoice result = spyService.createInvoiceWithOutstandingBalance(5L, 2000);

        assertEquals(999L, result.getId());
        verify(invoiceRepository).save(any());
    }

    // =====================================================================
    // 3) recordPayment()
    // =====================================================================

    @Test
    void testRecordPayment() {

        Invoice inv = new Invoice();
        inv.setId(88L);
        inv.setNetAmount(2000);

        when(invoiceRepository.findById(88L)).thenReturn(Optional.of(inv));

        PaymentRecord savedRecord = PaymentRecord.builder().id(1L).build();
        when(paymentRecordRepository.save(any())).thenReturn(savedRecord);

        when(paymentRecordRepository.findTotalPaidAmountByInvoiceId(88L))
                .thenReturn(new BigDecimal("1500"));

        PaymentRecord result = service.recordPayment(
                88L,
                new BigDecimal("1500"),
                PaymentRecord.PaymentMethod.CASH,
                "Paid"
        );

        assertEquals(1L, result.getId());
        assertEquals(500, inv.getRemainingBalance()); // unpaid left
        assertEquals(0, inv.getInvoiceStatus());      // still unpaid
        verify(invoiceRepository).save(inv);
    }

    // =====================================================================
    // 4) getOutstandingInvoices()
    // =====================================================================

    @Test
    void testGetOutstandingInvoices() {
        Invoice inv = new Invoice();

        when(invoiceRepository.findByContact_IdAndRemainingBalanceGreaterThanOrderByCreateDateAsc(55L, 0))
                .thenReturn(List.of(inv));

        List<Invoice> result = service.getOutstandingInvoices(55L);

        assertEquals(1, result.size());
    }

    // =====================================================================
    // 5) OutstandingBalanceSummary
    // =====================================================================

    @Test
    void testOutstandingBalanceSummary() {

        Invoice i1 = new Invoice();
        i1.setRemainingBalance(300);
        i1.setPenaltyTotal(10);
        i1.setDueDate(LocalDateTime.now().minusDays(1)); // overdue

        Invoice i2 = new Invoice();
        i2.setRemainingBalance(600);
        i2.setPenaltyTotal(20);
        i2.setDueDate(LocalDateTime.now().plusDays(2)); // OK

        when(invoiceRepository.findByContact_IdAndRemainingBalanceGreaterThanOrderByCreateDateAsc(40L, 0))
                .thenReturn(List.of(i1, i2));

        OutstandingBalanceService.OutstandingBalanceSummary summary =
                service.getOutstandingBalanceSummary(40L);

        assertEquals(900, summary.getTotalOutstanding());
        assertEquals(30, summary.getTotalPenalty());
        assertEquals(1, summary.getOverdueCount());
        assertEquals(2, summary.getTotalInvoices());
    }
}
