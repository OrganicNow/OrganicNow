package com.organicnow.backend.unit.service;

import com.lowagie.text.pdf.PdfReader;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.TenantContractService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantContractServiceTest {

    private TenantContractService service;

    private ContractRepository contractRepository;
    private TenantRepository tenantRepository;
    private RoomRepository roomRepository;
    private PackagePlanRepository packagePlanRepository;
    private InvoiceRepository invoiceRepository;

    private Contract contract;

    @BeforeEach
    void setup() {

        tenantRepository = mock(TenantRepository.class);
        roomRepository = mock(RoomRepository.class);
        packagePlanRepository = mock(PackagePlanRepository.class);
        contractRepository = mock(ContractRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);

        service = new TenantContractService(
                tenantRepository,
                roomRepository,
                packagePlanRepository,
                contractRepository,
                invoiceRepository
        );

        // ==== Mock data ====
        Tenant tenant = Tenant.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("0888888888")
                .nationalId("1234567890123")
                .build();

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();

        ContractType ct = new ContractType();
        ct.setName("Monthly");

        PackagePlan plan = new PackagePlan();
        plan.setId(55L);
        plan.setPrice(BigDecimal.valueOf(5500));
        plan.setContractType(ct);

        contract = Contract.builder()
                .id(99L)
                .tenant(tenant)
                .room(room)
                .packagePlan(plan)
                .deposit(BigDecimal.valueOf(2000))
                .rentAmountSnapshot(BigDecimal.valueOf(5500))
                .signDate(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .status(1)
                .build();

        when(contractRepository.findById(99L)).thenReturn(Optional.of(contract));
    }

    // ==========================================================================
    // 1) PDF สร้างได้จริง
    // ==========================================================================
    @Test
    void generateContractPdf_ShouldReturnValidPdf() throws Exception {

        byte[] pdf = service.generateContractPdf(99L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1500, "PDF should not be empty or too small");

        PdfReader reader = new PdfReader(pdf);
        assertEquals(3, reader.getNumberOfPages(), "PDF must contain 3 pages");
    }

    // ==========================================================================
    // 2) PDF ต้องมีองค์ประกอบพื้นฐาน
    // ==========================================================================
    @Test
    void generateContractPdf_ShouldContainBasicPdfStructure() {

        byte[] pdf = service.generateContractPdf(99L);
        String raw = new String(pdf);

        assertTrue(raw.startsWith("%PDF"), "Must start with %PDF header");

        assertTrue(raw.contains("/Font"), "Should contain font definitions");
        assertTrue(raw.contains("/Contents"), "Should contain content streams");
        assertTrue(raw.contains("/Page"), "Should contain page definitions");
    }

    // ==========================================================================
    // 3) ไม่ crash เมื่อข้อมูลมีค่านิดหน่อยเป็น null
    // ==========================================================================
    @Test
    void generateContractPdf_ShouldHandleNullableFields() {

        contract.getTenant().setEmail(null); // allowed
        contract.setDeposit(BigDecimal.valueOf(0)); // must not be null

        assertDoesNotThrow(() -> service.generateContractPdf(99L));
    }
}
