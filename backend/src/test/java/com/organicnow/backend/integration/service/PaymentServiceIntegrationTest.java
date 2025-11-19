package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.CreatePaymentRecordRequest;
import com.organicnow.backend.dto.PaymentRecordDto;
import com.organicnow.backend.dto.UpdatePaymentRecordRequest;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.PaymentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void setup(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private PaymentService paymentService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRecordRepository paymentRecordRepository;
    @Autowired private PaymentProofRepository paymentProofRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;

    private Invoice invoice;

    @BeforeEach
    void setupData() {

        Room room = roomRepository.save(Room.builder()
                .roomNumber("B101")
                .roomFloor(1)
                .roomSize(20)
                .build());

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .nationalId("123")
                .build());

        ContractType ct = contractTypeRepository.save(
                ContractType.builder().name("Monthly").duration(1).build()
        );

        PackagePlan plan = packagePlanRepository.save(
                PackagePlan.builder()
                        .contractType(ct)
                        .isActive(1)
                        .roomSize(1)
                        .price(BigDecimal.valueOf(1000))
                        .build()
        );

        Contract contract = contractRepository.save(
                Contract.builder()
                        .room(room)
                        .tenant(tenant)
                        .packagePlan(plan)
                        .signDate(LocalDateTime.now())
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(30))
                        .status(1)
                        .rentAmountSnapshot(BigDecimal.valueOf(1000))
                        .deposit(BigDecimal.ZERO)
                        .build()
        );

        invoice = invoiceRepository.save(
                Invoice.builder()
                        .contact(contract)
                        .createDate(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(10))
                        .invoiceStatus(0)
                        .subTotal(1000)
                        .penaltyTotal(0)
                        .netAmount(1000)
                        .remainingBalance(1000)
                        .requestedRent(1000)
                        .requestedWater(0)
                        .requestedElectricity(0)
                        .requestedFloor(1)
                        .requestedRoom("B101")
                        .build()
        );
    }

    // -------------------------------------------------------------
    @Test
    void testAddPaymentRecord() {

        CreatePaymentRecordRequest req = new CreatePaymentRecordRequest();
        req.setInvoiceId(invoice.getId());
        req.setPaymentAmount(BigDecimal.valueOf(500));
        req.setPaymentMethod(PaymentRecord.PaymentMethod.CASH);
        req.setRecordedBy("Tester");

        PaymentRecordDto dto = paymentService.addPaymentRecord(req);

        Assertions.assertThat(dto.getPaymentAmount()).isEqualTo(BigDecimal.valueOf(500));
        Assertions.assertThat(dto.getPaymentMethod()).isEqualTo(PaymentRecord.PaymentMethod.CASH);
        Assertions.assertThat(dto.getPaymentStatus()).isEqualTo(PaymentRecord.PaymentStatus.PENDING);

        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        Assertions.assertThat(updated.getInvoiceStatus()).isEqualTo(0); // partial
    }

    // -------------------------------------------------------------
    @Test
    void testUpdatePaymentRecord() {

        // Create record first
        PaymentRecord pr = paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(300))
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        UpdatePaymentRecordRequest req = new UpdatePaymentRecordRequest();
        req.setPaymentAmount(BigDecimal.valueOf(1000));
        req.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);

        PaymentRecordDto dto = paymentService.updatePaymentRecord(pr.getId(), req);

        Assertions.assertThat(dto.getPaymentAmount()).isEqualTo(BigDecimal.valueOf(1000));
        Assertions.assertThat(dto.getPaymentStatus()).isEqualTo(PaymentRecord.PaymentStatus.CONFIRMED);

        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        Assertions.assertThat(updated.getInvoiceStatus()).isEqualTo(1); // full paid
    }

    // -------------------------------------------------------------
    @Test
    void testDeletePaymentRecord() {

        PaymentRecord pr = paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(1000))
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentStatus(PaymentRecord.PaymentStatus.CONFIRMED)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        paymentService.deletePaymentRecord(pr.getId());

        Assertions.assertThat(paymentRecordRepository.findById(pr.getId())).isEmpty();

        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        Assertions.assertThat(updated.getInvoiceStatus()).isEqualTo(0); // unpaid now
    }

    // -------------------------------------------------------------
    @Test
    void testUploadPaymentProof() throws Exception {

        PaymentRecord pr = paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(200))
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "proof.jpg", "image/jpeg",
                "dummy-data".getBytes(StandardCharsets.UTF_8)
        );

        var result = paymentService.uploadPaymentProof(
                pr.getId(),
                mockFile,
                PaymentProof.ProofType.RECEIPT,
                "Test upload",
                "Tester"
        );

        Assertions.assertThat(result.getFileName()).isEqualTo("proof.jpg");
        Assertions.assertThat(result.getProofType()).isEqualTo(PaymentProof.ProofType.RECEIPT);
    }

    // -------------------------------------------------------------
    @Test
    void testDeletePaymentProof() throws Exception {

        PaymentRecord pr = paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(200))
                        .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "slip.jpg", "image/jpeg", "DATA".getBytes()
        );

        var proof = paymentService.uploadPaymentProof(
                pr.getId(),
                mockFile,
                PaymentProof.ProofType.BANK_SLIP,
                "Test",
                "Tester"
        );

        paymentService.deletePaymentProof(proof.getId());

        Assertions.assertThat(paymentProofRepository.findById(proof.getId())).isEmpty();
    }

    // -------------------------------------------------------------
    @Test
    void testGetPaymentRecordsByInvoice() {

        paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(300))
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        List<PaymentRecordDto> list = paymentService.getPaymentRecordsByInvoice(invoice.getId());

        Assertions.assertThat(list).hasSize(1);
    }

    // -------------------------------------------------------------
    @Test
    void testDownloadPaymentProof() throws Exception {

        PaymentRecord pr = paymentRecordRepository.save(
                PaymentRecord.builder()
                        .invoice(invoice)
                        .paymentAmount(BigDecimal.valueOf(200))
                        .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                        .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                        .paymentDate(LocalDateTime.now())
                        .build()
        );

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "abc.png", "image/png", "HELLO".getBytes()
        );

        var proof = paymentService.uploadPaymentProof(
                pr.getId(),
                mockFile,
                PaymentProof.ProofType.OTHER,
                "download-test",
                "tester"
        );

        var resource = paymentService.downloadPaymentProof(proof.getId());

        Assertions.assertThat(resource.exists()).isTrue();
        Assertions.assertThat(resource.isReadable()).isTrue();
    }
}
