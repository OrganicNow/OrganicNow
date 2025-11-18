package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.*;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @Mock
    private PaymentProofRepository paymentProofRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService service;

    private Invoice invoice;

    @BeforeEach
    void setup() {
        invoice = Invoice.builder()
                .id(1L)
                .subTotal(1000)
                .penaltyTotal(50)
                .invoiceStatus(0)
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // ป้องกัน NPE ทุกกรณี
        lenient().when(paymentRecordRepository.calculateTotalPaidAmount(anyLong()))
                .thenReturn(BigDecimal.ZERO);
    }

    // ============================================================
    // addPaymentRecord
    // ============================================================
    @Test
    void testAddPaymentRecord_Success() {

        CreatePaymentRecordRequest req = new CreatePaymentRecordRequest();
        req.setInvoiceId(1L);
        req.setPaymentAmount(new BigDecimal("500"));
        req.setPaymentMethod(PaymentRecord.PaymentMethod.BANK_TRANSFER);
        req.setPaymentDate(LocalDateTime.now());
        req.setNotes("test");
        req.setRecordedBy("tester");

        PaymentRecord saved = PaymentRecord.builder()
                .id(10L)
                .invoice(invoice)
                .paymentAmount(req.getPaymentAmount())
                .paymentDate(req.getPaymentDate())
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                .notes("test")
                .recordedBy("tester")
                .build();

        when(paymentRecordRepository.save(any())).thenReturn(saved);
        when(paymentRecordRepository.calculateTotalPaidAmount(1L))
                .thenReturn(new BigDecimal("500"));

        PaymentRecordDto dto = service.addPaymentRecord(req);

        assertEquals(10L, dto.getId());
        assertEquals(new BigDecimal("500"), dto.getPaymentAmount());
        assertEquals("โอนเงินผ่านธนาคาร", dto.getPaymentMethodDisplay());
        verify(paymentRecordRepository).save(any());
    }

    // ============================================================
    // getPaymentRecordsByInvoice
    // ============================================================
    @Test
    void testGetPaymentRecordsByInvoice_ShouldMapToDtoWithProofs() {

        PaymentRecord pr = PaymentRecord.builder()
                .id(30L)
                .invoice(invoice)
                .paymentAmount(new BigDecimal("800"))
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                .paymentStatus(PaymentRecord.PaymentStatus.CONFIRMED)
                .build();

        when(paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(1L))
                .thenReturn(List.of(pr));

        PaymentProof proof = PaymentProof.builder()
                .id(100L)
                .paymentRecord(pr)
                .fileName("proof.jpg")
                .filePath("uploads/proof.jpg")
                .fileSize(50L)
                .contentType("image/jpeg")
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .description("desc")
                .uploadedBy("tester")
                .uploadedAt(LocalDateTime.now())
                .build();

        when(paymentProofRepository.findByPaymentRecordIdOrderByUploadedAtDesc(30L))
                .thenReturn(List.of(proof));

        List<PaymentRecordDto> result = service.getPaymentRecordsByInvoice(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPaymentProofs().size());
        assertEquals("สลิปการโอนเงิน", result.get(0).getPaymentProofs().get(0).getProofTypeDisplay());
    }

    // ============================================================
    // updatePaymentRecord
    // ============================================================
    @Test
    void testUpdatePaymentRecord_Success() {

        PaymentRecord existing = PaymentRecord.builder()
                .id(55L)
                .invoice(invoice)
                .paymentAmount(new BigDecimal("300"))
                .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                .build();

        when(paymentRecordRepository.findById(55L)).thenReturn(Optional.of(existing));
        when(paymentRecordRepository.calculateTotalPaidAmount(1L))
                .thenReturn(new BigDecimal("300"));

        UpdatePaymentRecordRequest req = new UpdatePaymentRecordRequest();
        req.setPaymentAmount(new BigDecimal("500"));
        req.setPaymentMethod(PaymentRecord.PaymentMethod.BANK_TRANSFER);
        req.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);

        when(paymentRecordRepository.save(any())).thenReturn(existing);

        PaymentRecordDto result = service.updatePaymentRecord(55L, req);

        assertEquals(new BigDecimal("500"), result.getPaymentAmount());
        assertEquals("โอนเงินผ่านธนาคาร", result.getPaymentMethodDisplay());
        assertEquals("ยืนยันแล้ว", result.getPaymentStatusDisplay());
    }

    // ============================================================
    // deletePaymentRecord
    // ============================================================
    @Test
    void testDeletePaymentRecord_Success() {

        PaymentRecord pr = PaymentRecord.builder()
                .id(70L)
                .invoice(invoice)
                .build();

        when(paymentRecordRepository.findById(70L)).thenReturn(Optional.of(pr));

        service.deletePaymentRecord(70L);

        verify(paymentProofRepository).deleteByPaymentRecordId(70L);
        verify(paymentRecordRepository).deleteById(70L);
    }

    // ============================================================
    // uploadPaymentProof
    // ============================================================
    @Test
    void testUploadPaymentProof_Success() throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file", "slip.jpg", "image/jpeg", "DATA".getBytes()
        );

        PaymentRecord pr = PaymentRecord.builder()
                .id(88L)
                .invoice(invoice)
                .build();

        when(paymentRecordRepository.findById(88L))
                .thenReturn(Optional.of(pr));

        PaymentProof proof = PaymentProof.builder()
                .id(200L)
                .paymentRecord(pr)
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .fileName("slip.jpg")
                .filePath("uploads/payment-proofs/xxx.jpg")
                .fileSize(100L)
                .contentType("image/jpeg")
                .uploadedBy("tester")
                .uploadedAt(LocalDateTime.now())
                .build();

        when(paymentProofRepository.save(any())).thenReturn(proof);

        PaymentProofDto dto = service.uploadPaymentProof(
                88L, file, PaymentProof.ProofType.BANK_SLIP, "desc", "tester"
        );

        assertEquals("slip.jpg", dto.getFileName());
        assertEquals("สลิปการโอนเงิน", dto.getProofTypeDisplay());
    }

    // ============================================================
    // getPaymentProofsByPaymentRecord
    // ============================================================
    @Test
    void testGetPaymentProofsByPaymentRecord() {

        PaymentRecord pr = PaymentRecord.builder().id(123L).build();

        PaymentProof proof = PaymentProof.builder()
                .id(1L)
                .paymentRecord(pr)
                .fileName("aaa.jpg")
                .filePath("uploads/aaa.jpg")
                .fileSize(100L)
                .contentType("image/jpeg")
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .description("desc")
                .uploadedBy("tester")
                .uploadedAt(LocalDateTime.now())
                .build();

        when(paymentProofRepository.findByPaymentRecordIdOrderByUploadedAtDesc(123L))
                .thenReturn(List.of(proof));

        List<PaymentProofDto> result = service.getPaymentProofsByPaymentRecord(123L);

        assertEquals(1, result.size());
        assertEquals("aaa.jpg", result.get(0).getFileName());
        assertEquals("สลิปการโอนเงิน", result.get(0).getProofTypeDisplay());
    }

    // ============================================================
    // getPaymentProofById
    // ============================================================
    @Test
    void testGetPaymentProofById_Success() {

        PaymentRecord pr = PaymentRecord.builder().id(500L).build();

        PaymentProof proof = PaymentProof.builder()
                .id(999L)
                .paymentRecord(pr)
                .fileName("test.png")
                .filePath("uploads/test.png")
                .fileSize(100L)
                .contentType("image/png")
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .uploadedAt(LocalDateTime.now())
                .build();

        when(paymentProofRepository.findById(999L))
                .thenReturn(Optional.of(proof));

        PaymentProofDto dto = service.getPaymentProofById(999L);

        assertEquals("test.png", dto.getFileName());
        assertEquals("สลิปการโอนเงิน", dto.getProofTypeDisplay());
    }

    // ============================================================
    // deletePaymentProof
    // ============================================================
    @Test
    void testDeletePaymentProof_Success() {

        PaymentProof proof = PaymentProof.builder()
                .id(400L)
                .filePath("uploads/payment-proofs/p1.jpg")
                .build();

        when(paymentProofRepository.findById(400L))
                .thenReturn(Optional.of(proof));

        service.deletePaymentProof(400L);

        verify(paymentProofRepository).deleteById(400L);
    }

    // ============================================================
    // downloadPaymentProof
    // ============================================================
    @Test
    void testDownloadPaymentProof_Success() throws Exception {

        Path temp = Files.createTempFile("proof", ".jpg");
        Files.write(temp, "IMG".getBytes());

        PaymentRecord pr = PaymentRecord.builder().id(10L).build();

        PaymentProof proof = PaymentProof.builder()
                .id(777L)
                .filePath(temp.toString())
                .fileName("proof.jpg")
                .paymentRecord(pr)
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .build();

        when(paymentProofRepository.findById(777L))
                .thenReturn(Optional.of(proof));

        Resource res = service.downloadPaymentProof(777L);

        assertTrue(res.exists());
        assertTrue(res.isReadable());
    }

    // ============================================================
    // getTotalPaid / Pending
    // ============================================================
    @Test
    void testGetTotalPaidAmount() {
        when(paymentRecordRepository.calculateTotalPaidAmount(1L))
                .thenReturn(new BigDecimal("900"));

        assertEquals(new BigDecimal("900"), service.getTotalPaidAmount(1L));
    }

    @Test
    void testGetTotalPendingAmount() {
        when(paymentRecordRepository.calculateTotalPendingAmount(1L))
                .thenReturn(new BigDecimal("300"));

        assertEquals(new BigDecimal("300"), service.getTotalPendingAmount(1L));
    }
}
