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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentRecordRepositoryIntegrationTest {

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

    private Long invoiceId;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        // Generate unique identifiers
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // Create ContractType
        ContractType contractType = new ContractType();
        contractType.setName("M-" + uniqueId);
        contractType.setDuration(1);
        contractType = contractTypeRepository.save(contractType);

        // Create PackagePlan
        PackagePlan packagePlan = new PackagePlan();
        packagePlan.setContractType(contractType);
        packagePlan.setPrice(BigDecimal.valueOf(5000));
        packagePlan.setIsActive(1);
        packagePlan.setRoomSize(0);
        packagePlan = packagePlanRepository.save(packagePlan);

        // Create Room
        Room room = new Room();
        room.setRoomNumber("R-" + uniqueId);
        room.setRoomFloor(1);
        room.setRoomSize(0);
        room = roomRepository.save(room);

        // Create Tenant
        Tenant tenant = Tenant.builder()
                .nationalId("NID-" + uniqueId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("0812345678")
                .build();
        tenant = tenantRepository.save(tenant);

        // Create Contract
        Contract contract = new Contract();
        contract.setStatus(0);
        contract.setPackagePlan(packagePlan);
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(LocalDateTime.now());
        contract.setEndDate(LocalDateTime.now().plusMonths(6));
        contract.setRentAmountSnapshot(BigDecimal.valueOf(5000));
        contract = contractRepository.save(contract);

        // Create Invoice
        invoice = new Invoice();
        invoice.setContact(contract);
        invoice.setSubTotal(5000);
        invoice.setCreateDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusMonths(1));
        invoice.setInvoiceStatus(0);
        invoice = invoiceRepository.save(invoice);
        invoiceId = invoice.getId();

        // Create test payment records
        createPaymentRecord(invoice, BigDecimal.valueOf(2000), PaymentRecord.PaymentStatus.CONFIRMED,
                PaymentRecord.PaymentMethod.BANK_TRANSFER, "TRX001");
        createPaymentRecord(invoice, BigDecimal.valueOf(1500), PaymentRecord.PaymentStatus.CONFIRMED,
                PaymentRecord.PaymentMethod.CREDIT_CARD, "TRX002");
        createPaymentRecord(invoice, BigDecimal.valueOf(1000), PaymentRecord.PaymentStatus.PENDING,
                PaymentRecord.PaymentMethod.CASH, "TRX003");
        createPaymentRecord(invoice, BigDecimal.valueOf(500), PaymentRecord.PaymentStatus.REJECTED,
                PaymentRecord.PaymentMethod.QR_CODE, "TRX004");
    }

    private void createPaymentRecord(Invoice invoice, BigDecimal amount, PaymentRecord.PaymentStatus status,
                                     PaymentRecord.PaymentMethod method, String txRef) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setInvoice(invoice);
        paymentRecord.setPaymentAmount(amount);
        paymentRecord.setPaymentDate(LocalDateTime.now());
        paymentRecord.setPaymentMethod(method);
        paymentRecord.setPaymentStatus(status);
        paymentRecord.setRecordedBy("testUser");
        paymentRecord.setTransactionReference(txRef);
        paymentRecordRepository.save(paymentRecord);
    }

    @Test
    void testFindByInvoiceIdOrderByPaymentDateDesc() {
        // when
        List<PaymentRecord> records = paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId);

        // then
        assertThat(records).hasSize(4);
        assertThat(records.get(0).getInvoice().getId()).isEqualTo(invoiceId);
    }

    @Test
    void testFindByPaymentStatusOrderByPaymentDateDesc() {
        // when
        List<PaymentRecord> confirmedRecords = paymentRecordRepository
                .findByPaymentStatusOrderByPaymentDateDesc(PaymentRecord.PaymentStatus.CONFIRMED);

        // then
        assertThat(confirmedRecords).hasSizeGreaterThanOrEqualTo(2);
        assertThat(confirmedRecords).allMatch(record ->
                record.getPaymentStatus() == PaymentRecord.PaymentStatus.CONFIRMED);
    }

    @Test
    void testFindByPaymentMethodOrderByPaymentDateDesc() {
        // when
        List<PaymentRecord> bankTransferRecords = paymentRecordRepository
                .findByPaymentMethodOrderByPaymentDateDesc(PaymentRecord.PaymentMethod.BANK_TRANSFER);

        // then
        assertThat(bankTransferRecords).hasSizeGreaterThanOrEqualTo(1);
        assertThat(bankTransferRecords).allMatch(record ->
                record.getPaymentMethod() == PaymentRecord.PaymentMethod.BANK_TRANSFER);
    }

    @Test
    void testFindByPaymentDateBetweenOrderByPaymentDateDesc() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when
        List<PaymentRecord> records = paymentRecordRepository
                .findByPaymentDateBetweenOrderByPaymentDateDesc(startDate, endDate);

        // then
        assertThat(records).hasSizeGreaterThanOrEqualTo(4);
        assertThat(records).allMatch(record ->
                !record.getPaymentDate().isBefore(startDate) &&
                        !record.getPaymentDate().isAfter(endDate));
    }

    @Test
    void testCalculateTotalPaidAmount() {
        // when
        BigDecimal totalPaid = paymentRecordRepository.calculateTotalPaidAmount(invoiceId);

        // then
        assertThat(totalPaid).isEqualByComparingTo(BigDecimal.valueOf(3500)); // 2000 + 1500
    }

    @Test
    void testCalculateTotalReceivedAmount() {
        // when
        BigDecimal totalReceived = paymentRecordRepository.calculateTotalReceivedAmount(invoiceId);

        // then
        assertThat(totalReceived).isEqualByComparingTo(BigDecimal.valueOf(4500)); // 2000 + 1500 + 1000
    }

    @Test
    void testFindTotalPaidAmountByInvoiceId() {
        // when
        BigDecimal totalPaid = paymentRecordRepository.findTotalPaidAmountByInvoiceId(invoiceId);

        // then
        assertThat(totalPaid).isEqualByComparingTo(BigDecimal.valueOf(3500)); // 2000 + 1500
    }

    @Test
    void testCalculateTotalPendingAmount() {
        // when
        BigDecimal totalPending = paymentRecordRepository.calculateTotalPendingAmount(invoiceId);

        // then
        assertThat(totalPending).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void testCalculateTotalPaidAmountWhenNoPayments() {
        // given - create new invoice without payments
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        ContractType contractType = new ContractType();
        contractType.setName("M-" + uniqueId);
        contractType.setDuration(1);
        contractType = contractTypeRepository.save(contractType);

        PackagePlan packagePlan = new PackagePlan();
        packagePlan.setContractType(contractType);
        packagePlan.setPrice(BigDecimal.valueOf(5000));
        packagePlan.setIsActive(1);
        packagePlan.setRoomSize(0);
        packagePlan = packagePlanRepository.save(packagePlan);

        Room room = new Room();
        room.setRoomNumber("R-" + uniqueId);
        room.setRoomFloor(1);
        room.setRoomSize(0);
        room = roomRepository.save(room);

        Tenant tenant = Tenant.builder()
                .nationalId("NID-" + uniqueId)
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0823456789")
                .build();
        tenant = tenantRepository.save(tenant);

        Contract contract = new Contract();
        contract.setStatus(0);
        contract.setPackagePlan(packagePlan);
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(LocalDateTime.now());
        contract.setEndDate(LocalDateTime.now().plusMonths(6));
        contract.setRentAmountSnapshot(BigDecimal.valueOf(5000));
        contract = contractRepository.save(contract);

        Invoice emptyInvoice = new Invoice();
        emptyInvoice.setContact(contract);
        emptyInvoice.setSubTotal(5000);
        emptyInvoice.setCreateDate(LocalDateTime.now());
        emptyInvoice.setDueDate(LocalDateTime.now().plusMonths(1));
        emptyInvoice.setInvoiceStatus(0);
        emptyInvoice = invoiceRepository.save(emptyInvoice);

        // when
        BigDecimal totalPaid = paymentRecordRepository.calculateTotalPaidAmount(emptyInvoice.getId());

        // then
        assertThat(totalPaid).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testFindByTransactionReferenceContainingIgnoreCaseOrderByPaymentDateDesc() {
        // when
        List<PaymentRecord> records = paymentRecordRepository
                .findByTransactionReferenceContainingIgnoreCaseOrderByPaymentDateDesc("trx");

        // then
        assertThat(records).hasSizeGreaterThanOrEqualTo(4);
        assertThat(records).allMatch(record ->
                record.getTransactionReference().toLowerCase().contains("trx"));
    }

    @Test
    void testDeleteByInvoiceId() {
        // given
        List<PaymentRecord> recordsBefore = paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId);
        assertThat(recordsBefore).hasSizeGreaterThan(0);

        // when
        paymentRecordRepository.deleteByInvoiceId(invoiceId);
        paymentRecordRepository.flush();

        // then
        List<PaymentRecord> recordsAfter = paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId);
        assertThat(recordsAfter).isEmpty();
    }
}