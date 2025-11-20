package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentProofRepositoryIntegrationTest {

    @Autowired
    private PaymentProofRepository paymentProofRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PackagePlanRepository packagePlanRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ContractTypeRepository contractTypeRepository;

    private Long paymentRecordId;
    private PaymentRecord paymentRecord;
    private Long invoiceId;

    @BeforeEach
    void setUp() {
        // Generate unique identifiers (use first 8 chars of UUID to keep within field limits)
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // Create and save ContractType
        ContractType contractType = new ContractType();
        contractType.setName("M-" + uniqueId);
        contractType.setDuration(1);
        contractType = contractTypeRepository.save(contractType);

        // Create and save PackagePlan
        PackagePlan packagePlan = new PackagePlan();
        packagePlan.setContractType(contractType);
        packagePlan.setPrice(BigDecimal.valueOf(5000));
        packagePlan.setIsActive(1);
        packagePlan.setRoomSize(0);
        packagePlan = packagePlanRepository.save(packagePlan);

        // Create and save Room with unique room number (max 30 chars)
        Room room = new Room();
        room.setRoomNumber("R-" + uniqueId);
        room.setRoomFloor(1);
        room.setRoomSize(0);
        room = roomRepository.save(room);

        // Create and save Tenant with unique national_id (max 50 chars)
        Tenant tenant = Tenant.builder()
                .nationalId("NID-" + uniqueId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("0812345678")
                .build();
        tenant = tenantRepository.save(tenant);

        // Create and save Contract
        Contract contract = new Contract();
        contract.setStatus(0);
        contract.setPackagePlan(packagePlan);
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(LocalDateTime.now());
        contract.setEndDate(LocalDateTime.now().plusMonths(6));
        contract.setRentAmountSnapshot(BigDecimal.valueOf(5000));
        contract = contractRepository.save(contract);

        // Create and save Invoice
        Invoice invoice = new Invoice();
        invoice.setContact(contract);
        invoice.setSubTotal(1500);
        invoice.setCreateDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusMonths(1));
        invoice.setInvoiceStatus(0);
        invoice = invoiceRepository.save(invoice);
        invoiceId = invoice.getId();

        // Create and save PaymentRecord
        paymentRecord = new PaymentRecord();
        paymentRecord.setInvoice(invoice);
        paymentRecord.setPaymentAmount(BigDecimal.valueOf(1000.00));
        paymentRecord.setPaymentDate(LocalDateTime.now());
        paymentRecord.setPaymentMethod(PaymentRecord.PaymentMethod.CREDIT_CARD);
        paymentRecord.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);
        paymentRecord.setRecordedBy("testUser");
        paymentRecord.setTransactionReference("TRX123456");
        paymentRecord = paymentRecordRepository.save(paymentRecord);
        paymentRecordId = paymentRecord.getId();

        // Create and save PaymentProof
        PaymentProof paymentProof = new PaymentProof();
        paymentProof.setPaymentRecord(paymentRecord);
        paymentProof.setProofType(PaymentProof.ProofType.RECEIPT);
        paymentProof.setFilePath("path/to/file");
        paymentProof.setFileName("test-receipt.pdf");
        paymentProof.setUploadedBy("testUser");
        paymentProofRepository.save(paymentProof);
    }

    @Test
    void testFindByPaymentRecordIdOrderByUploadedAtDesc() {
        // when
        List<PaymentProof> paymentProofs = paymentProofRepository.findByPaymentRecordIdOrderByUploadedAtDesc(paymentRecordId);

        // then
        assertThat(paymentProofs).isNotEmpty();
        assertThat(paymentProofs.get(0).getPaymentRecord().getId()).isEqualTo(paymentRecordId);
    }

    @Test
    void testFindByProofTypeOrderByUploadedAtDesc() {
        // when
        List<PaymentProof> paymentProofs = paymentProofRepository.findByProofTypeOrderByUploadedAtDesc(PaymentProof.ProofType.RECEIPT);

        // then
        assertThat(paymentProofs).isNotEmpty();
        assertThat(paymentProofs.get(0).getProofType()).isEqualTo(PaymentProof.ProofType.RECEIPT);
    }

    @Test
    void testFindByFilePath() {
        // when
        Optional<PaymentProof> foundPaymentProof = paymentProofRepository.findByFilePath("path/to/file");

        // then
        assertThat(foundPaymentProof).isPresent();
        assertThat(foundPaymentProof.get().getFilePath()).isEqualTo("path/to/file");
    }

    @Test
    void testFindByInvoiceId() {
        // when
        List<PaymentProof> paymentProofs = paymentProofRepository.findByInvoiceId(invoiceId);

        // then
        assertThat(paymentProofs).isNotEmpty();
        assertThat(paymentProofs.get(0).getPaymentRecord().getInvoice().getId()).isEqualTo(invoiceId);
    }

    @Test
    void testFindByUploadedByContainingIgnoreCaseOrderByUploadedAtDesc() {
        // when
        List<PaymentProof> paymentProofs = paymentProofRepository.findByUploadedByContainingIgnoreCaseOrderByUploadedAtDesc("testuser");

        // then
        assertThat(paymentProofs).isNotEmpty();
        assertThat(paymentProofs.get(0).getUploadedBy()).isEqualToIgnoringCase("testuser");
    }

    @Test
    void testCountByPaymentRecordId() {
        // when
        long count = paymentProofRepository.countByPaymentRecordId(paymentRecordId);

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void testDeleteByPaymentRecordId() {
        // when
        paymentProofRepository.deleteByPaymentRecordId(paymentRecordId);

        // then
        long count = paymentProofRepository.countByPaymentRecordId(paymentRecordId);
        assertThat(count).isEqualTo(0);
    }
}