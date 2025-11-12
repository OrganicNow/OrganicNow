//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.CreateTenantContractRequest;
//import com.organicnow.backend.dto.TenantDetailDto;
//import com.organicnow.backend.dto.TenantDto;
//import com.organicnow.backend.dto.UpdateTenantContractRequest;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import com.organicnow.backend.service.TenantContractService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TenantContractServiceTest {
//
//    @Mock
//    private TenantRepository tenantRepository;
//    @Mock
//    private RoomRepository roomRepository;
//    @Mock
//    private PackagePlanRepository packagePlanRepository;
//    @Mock
//    private ContractRepository contractRepository;
//    @Mock
//    private InvoiceRepository invoiceRepository;
//
//    @InjectMocks
//    private TenantContractService tenantContractService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // -------------------- CREATE --------------------
//    @Test
//    void create_shouldSaveTenantAndContract() {
//        // Mock request data
//        CreateTenantContractRequest req = new CreateTenantContractRequest();
//        req.setFirstName("John");
//        req.setLastName("Doe");
//        req.setEmail("john@example.com");
//        req.setPhoneNumber("0812345678");
//        req.setNationalId("1234567890123");
//        req.setRoomId(1L);
//        req.setPackageId(2L);
//        req.setStartDate(LocalDateTime.now());
//        req.setEndDate(LocalDateTime.now().plusMonths(6));
//        req.setDeposit(BigDecimal.valueOf(5000));
//        req.setRentAmountSnapshot(BigDecimal.valueOf(10000));
//
//        // Mock the tenant data
//        Tenant tenant = Tenant.builder().id(1L).firstName("John").lastName("Doe").build();
//        Room room = Room.builder().id(1L).roomNumber("101").roomFloor(1).build();
//        PackagePlan plan = PackagePlan.builder().id(2L).price(BigDecimal.valueOf(10000)).build();
//        Contract contract = Contract.builder().id(10L).tenant(tenant).room(room).packagePlan(plan).build();
//
//        // Mocking repository methods
//        when(tenantRepository.findByNationalId("1234567890123")).thenReturn(Optional.of(tenant)); // Return tenant when nationalId is searched
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room)); // Mock room search
//        when(packagePlanRepository.findById(2L)).thenReturn(Optional.of(plan)); // Mock package plan search
//        when(contractRepository.save(any(Contract.class))).thenReturn(contract); // Mock contract saving
//
//        // Call the service method
//        TenantDto result = tenantContractService.create(req);
//
//        // Assertions
//        assertNotNull(result);
//        assertEquals("John", result.getFirstName());
//        assertEquals("Doe", result.getLastName());
//        assertEquals("101", result.getRoom()); // Ensure that the room number matches
//        assertEquals("John", result.getFirstName(), "First name should match");
//        assertEquals("Doe", result.getLastName(), "Last name should match");
//
//        // Verify that the repository methods were called
//        verify(tenantRepository).findByNationalId("1234567890123");
//        verify(contractRepository).save(any(Contract.class));
//    }
//
//    // -------------------- UPDATE --------------------
//    @Test
//    void update_shouldModifyTenantAndContract() {
//        Tenant tenant = Tenant.builder().id(1L).firstName("Old").lastName("Name").build();
//        Room room = Room.builder().id(1L).roomNumber("101").roomFloor(1).build();
//        PackagePlan plan = PackagePlan.builder().id(2L).price(BigDecimal.valueOf(10000)).build();
//        Contract contract = Contract.builder().id(10L).tenant(tenant).room(room).packagePlan(plan).build();
//
//        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
//        when(contractRepository.save(any(Contract.class))).thenReturn(contract);
//
//        UpdateTenantContractRequest req = new UpdateTenantContractRequest();
//        req.setFirstName("New");
//        req.setLastName("Name");
//        req.setEmail("new@example.com");
//        req.setPhoneNumber("0898765432");
//        req.setStatus(2);
//
//        var result = tenantContractService.update(10L, req);
//
//        assertEquals("New", result.getFirstName());
//        assertEquals("Name", result.getLastName());
//        assertEquals("new@example.com", result.getEmail());
//        assertEquals(2, contract.getStatus());
//
//        verify(tenantRepository).save(any(Tenant.class));
//        verify(contractRepository).save(contract);
//    }
//
//    // -------------------- DELETE --------------------
//    @Test
//    void delete_shouldRemoveContract() {
//        when(contractRepository.existsById(10L)).thenReturn(true);
//
//        tenantContractService.delete(10L);
//
//        verify(contractRepository).deleteById(10L);
//    }
//
//    @Test
//    void delete_shouldThrowIfContractNotFound() {
//        when(contractRepository.existsById(99L)).thenReturn(false);
//
//        assertThrows(RuntimeException.class, () -> tenantContractService.delete(99L));
//    }
//
//    // -------------------- GET DETAIL --------------------
//    @Test
//    void getDetail_shouldReturnTenantDetailDto() {
//        // Mock tenant, room, contract
//        Tenant tenant = Tenant.builder()
//                .id(1L).firstName("John").lastName("Doe")
//                .email("john@example.com").phoneNumber("0812345678")
//                .nationalId("1234567890123").build();
//
//        Room room = Room.builder().id(1L).roomNumber("101").roomFloor(1).build();
//        ContractType type = ContractType.builder().id(1L).name("6 เดือน").build();
//        PackagePlan plan = PackagePlan.builder().id(2L).price(BigDecimal.valueOf(10000)).contractType(type).build();
//        Contract contract = Contract.builder()
//                .id(10L).tenant(tenant).room(room).packagePlan(plan)
//                .startDate(LocalDateTime.now()).endDate(LocalDateTime.now().plusMonths(6))
//                .status(1).deposit(BigDecimal.valueOf(5000)).rentAmountSnapshot(BigDecimal.valueOf(10000))
//                .build();
//
//        // Mock invoice
//        Invoice invoice = Invoice.builder()
//                .id(100L)
//                .invoiceStatus(1)
//                .subTotal(1000)
//                .build();
//
//        // 🔹 Mock repository responses
//        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
//        when(invoiceRepository.findByContact_IdOrderByIdDesc(10L)).thenReturn(List.of(invoice)); // <-- ต้องตรงกับ service
//
//        // Call service
//        TenantDetailDto detail = tenantContractService.getDetail(10L);
//
//        // Assertions
//        assertNotNull(detail);
//        assertEquals("John", detail.getFirstName());
//        assertEquals("101", detail.getRoom());
//        assertEquals("6 เดือน", detail.getPackageName());
//        assertEquals(1, detail.getInvoices().size());
//
//        // Verify repository interactions
//        verify(contractRepository).findById(10L);
//        verify(invoiceRepository).findByContact_IdOrderByIdDesc(10L);
//    }
//
//}
