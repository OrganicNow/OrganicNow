//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.*;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.repository.InvoiceRepository;
//import com.organicnow.backend.repository.RoomRepository;
//import com.organicnow.backend.service.InvoiceServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class InvoiceServiceTest {
//
//    @Mock private InvoiceRepository invoiceRepository;
//    @Mock private ContractRepository contractRepository;
//    @Mock private RoomRepository roomRepository;
//
//    @InjectMocks private InvoiceServiceImpl invoiceService;
//
//    private Invoice invoice;
//    private Contract contract;
//    private Tenant tenant;
//    private Room room;
//    private PackagePlan packagePlan;
//    private ContractType contractType;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        tenant = new Tenant();
//        tenant.setId(1L);
//        tenant.setFirstName("John");
//        tenant.setLastName("Doe");
//        tenant.setNationalId("1234567890123");
//        tenant.setPhoneNumber("0812345678");
//        tenant.setEmail("john@example.com");
//
//        room = new Room();
//        room.setId(1L);
//        room.setRoomNumber("101");
//        room.setRoomFloor(1);
//
//        contractType = new ContractType();
//        contractType.setId(1L);
//        contractType.setName("Monthly");
//
//        packagePlan = new PackagePlan();
//        packagePlan.setId(1L);
//        packagePlan.setContractType(contractType);
//
//        contract = new Contract();
//        contract.setId(1L);
//        contract.setTenant(tenant);
//        contract.setRoom(room);
//        contract.setPackagePlan(packagePlan);
//        contract.setSignDate(LocalDateTime.now().minusDays(30));
//        contract.setStartDate(LocalDateTime.now().minusDays(30));
//        contract.setEndDate(LocalDateTime.now().plusDays(335));
//        contract.setRentAmountSnapshot(BigDecimal.valueOf(5000.0));
//
//        invoice = new Invoice();
//        invoice.setId(1L);
//        invoice.setContact(contract);
//        invoice.setCreateDate(LocalDateTime.now());
//        invoice.setDueDate(LocalDateTime.now().plusDays(30));
//        invoice.setInvoiceStatus(0);
//        invoice.setSubTotal(6000);
//        invoice.setPenaltyTotal(0);
//        invoice.setNetAmount(6000);
//    }
//
//    // ✅ getAllInvoices
//    @Test
//    void testGetAllInvoices() {
//        InvoiceServiceImpl spyService = spy(invoiceService);
//        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));
//        doNothing().when(spyService).updateOverduePenalties();
//
//        List<InvoiceDto> result = spyService.getAllInvoices();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getId()).isEqualTo(1L);
//        verify(invoiceRepository, times(1)).findAll();
//    }
//
//    // ✅ getInvoiceById
//    @Test
//    void testGetInvoiceById_Found() {
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//        Optional<InvoiceDto> result = invoiceService.getInvoiceById(1L);
//        assertThat(result).isPresent();
//        assertThat(result.get().getId()).isEqualTo(1L);
//    }
//
//    @Test
//    void testGetInvoiceById_NotFound() {
//        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
//        assertThat(invoiceService.getInvoiceById(99L)).isEmpty();
//    }
//
//    // ✅ createInvoice (มี contract)
//    @Test
//    void testCreateInvoice_WithMinimalData() {
//        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
//                .contractId(1L)
//                .rentAmount(5000)
//                .build();
//
//        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
//        when(invoiceRepository.save(any())).thenReturn(invoice);
//
//        InvoiceDto result = invoiceService.createInvoice(req);
//
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(contractRepository).findById(1L);
//        verify(invoiceRepository).save(any());
//    }
//
//    @Test
//    void testCreateInvoice_ContractNotFound_Throws() {
//        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
//                .contractId(99L)
//                .build();
//
//        when(contractRepository.findById(99L)).thenReturn(Optional.empty());
//        assertThatThrownBy(() -> invoiceService.createInvoice(req))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Contract not found");
//    }
//
//    // ✅ createInvoice (ไม่มี contractId → ใช้ placeholder)
//    @Test
//    void testCreateInvoice_WithoutContractId_UsesPlaceholderContract() {
//        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
//                .floor("2")
//                .room("A201")
//                .rentAmount(3000)
//                .build();
//
//        when(contractRepository.findAll()).thenReturn(List.of(contract));
//        when(invoiceRepository.save(any())).thenReturn(invoice);
//
//        InvoiceDto result = invoiceService.createInvoice(req);
//
//        assertThat(result).isNotNull();
//        verify(contractRepository).findAll();
//        verify(invoiceRepository).save(any());
//    }
//
//    @Test
//    void testCreateInvoice_NoContractsAvailable_Throws() {
//        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
//                .room("B202")
//                .build();
//
//        when(contractRepository.findAll()).thenReturn(Collections.emptyList());
//        assertThatThrownBy(() -> invoiceService.createInvoice(req))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("No contracts available");
//    }
//
//    // ✅ updateInvoice
//    @Test
//    void testUpdateInvoice_UpdateFields() {
//        UpdateInvoiceRequest req = UpdateInvoiceRequest.builder()
//                .invoiceStatus(1)
//                .subTotal(7000)
//                .penaltyTotal(500)
//                .netAmount(7500)
//                .payMethod(1)
//                .build();
//
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//        when(invoiceRepository.save(any())).thenReturn(invoice);
//
//        InvoiceDto dto = invoiceService.updateInvoice(1L, req);
//
//        assertThat(dto.getNetAmount()).isEqualTo(7500); // ✅ fix
//        verify(invoiceRepository).save(any());
//    }
//
//    @Test
//    void testUpdateInvoice_NotFound_Throws() {
//        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
//        UpdateInvoiceRequest req = UpdateInvoiceRequest.builder().build();
//        assertThatThrownBy(() -> invoiceService.updateInvoice(99L, req))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Invoice not found");
//    }
//
//    // ✅ deleteInvoice
//    @Test
//    void testDeleteInvoice_Success() {
//        when(invoiceRepository.existsById(1L)).thenReturn(true);
//        invoiceService.deleteInvoice(1L);
//        verify(invoiceRepository).deleteById(1L);
//    }
//
//    @Test
//    void testDeleteInvoice_NotExist() {
//        when(invoiceRepository.existsById(99L)).thenReturn(false);
//        invoiceService.deleteInvoice(99L);
//        verify(invoiceRepository, never()).deleteById(any());
//    }
//
//    // ✅ updateOverduePenalties
//    @Test
//    void testUpdateOverduePenalties_ApplyPenalty() {
//        Invoice inv = new Invoice();
//        inv.setId(5L);
//        inv.setInvoiceStatus(0);
//        inv.setPenaltyTotal(0);
//        inv.setSubTotal(1000); // ✅ fix
//        inv.setDueDate(LocalDateTime.now().minusDays(5));
//        inv.setRequestedRent(1000);
//
//        when(invoiceRepository.findAll()).thenReturn(List.of(inv));
//
//        invoiceService.updateOverduePenalties();
//
//        assertThat(inv.getPenaltyTotal()).isEqualTo(100);
//        verify(invoiceRepository, atLeastOnce()).save(inv);
//    }
//
//    // ✅ unimplemented methods
//    @Test
//    void testUnimplementedMethods_ReturnEmptyOrThrow() {
//        assertThat(invoiceService.searchInvoices("test")).isEmpty();
//        assertThat(invoiceService.getInvoicesByContractId(1L)).isEmpty();
//        assertThat(invoiceService.getInvoicesByTenantId(1L)).isEmpty();
//        assertThat(invoiceService.getInvoicesByRoomId(1L)).isEmpty();
//        assertThat(invoiceService.getOverdueInvoices()).isEmpty();
//        assertThat(invoiceService.getUnpaidInvoices()).isEmpty();
//        assertThat(invoiceService.getInvoicesByDateRange(null, null)).isEmpty();
//
//        assertThatThrownBy(() -> invoiceService.markAsPaid(1L))
//                .isInstanceOf(UnsupportedOperationException.class);
//        assertThatThrownBy(() -> invoiceService.cancelInvoice(1L))
//                .isInstanceOf(UnsupportedOperationException.class);
//        assertThatThrownBy(() -> invoiceService.addPenalty(1L, 100))
//                .isInstanceOf(UnsupportedOperationException.class);
//    }
//
//    // ✅ convertToDto tests
//    @Test
//    void testConvertToDto_WithTenantData() {
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//        Optional<InvoiceDto> result = invoiceService.getInvoiceById(1L);
//
//        assertThat(result).isPresent();
//        InvoiceDto dto = result.get();
//        assertThat(dto.getFirstName()).isEqualTo("John");
//        assertThat(dto.getRoom()).isEqualTo("101");
//        assertThat(dto.getPackageName()).isEqualTo("Monthly");
//    }
//
//    @Test
//    void testConvertToDto_NullTenant() {
//        contract.setTenant(null);
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//
//        Optional<InvoiceDto> result = invoiceService.getInvoiceById(1L);
//        assertThat(result).isPresent();
//        assertThat(result.get().getFirstName()).isEqualTo("N/A");
//    }
//
//    @Test
//    void testConvertToDto_NullRoom() {
//        contract.setRoom(null);
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//        Optional<InvoiceDto> result = invoiceService.getInvoiceById(1L);
//        assertThat(result).isPresent();
//        assertThat(result.get().getRoom()).isEqualTo("N/A");
//    }
//
//    @Test
//    void testConvertToDto_NullContract() {
//        invoice.setContact(null);
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
//        Optional<InvoiceDto> result = invoiceService.getInvoiceById(1L);
//        assertThat(result).isPresent();
//        assertThat(result.get().getFirstName()).isEqualTo("N/A");
//    }
//}
