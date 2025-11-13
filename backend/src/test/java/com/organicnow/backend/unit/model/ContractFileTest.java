package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ContractFileTest {

    @Test
    void testGetterAndSetter() {
        ContractFile file = new ContractFile();
        Contract contract = new Contract();
        contract.setId(100L);

        byte[] pdfData = {1, 2, 3, 4, 5};
        LocalDateTime now = LocalDateTime.of(2025, 11, 13, 10, 0);

        file.setId(1L);
        file.setContract(contract);
        file.setSignedPdf(pdfData);
        file.setUploadedAt(now);

        assertEquals(1L, file.getId());
        assertEquals(100L, file.getContract().getId());
        assertArrayEquals(pdfData, file.getSignedPdf());
        assertEquals(now, file.getUploadedAt());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Contract contract = new Contract();
        contract.setId(200L);
        byte[] pdf = {10, 20, 30};

        LocalDateTime time = LocalDateTime.of(2025, 10, 1, 15, 30);
        ContractFile file = ContractFile.builder()
                .id(2L)
                .contract(contract)
                .signedPdf(pdf)
                .uploadedAt(time)
                .build();

        assertNotNull(file);
        assertEquals(2L, file.getId());
        assertEquals(200L, file.getContract().getId());
        assertArrayEquals(new byte[]{10, 20, 30}, file.getSignedPdf());
        assertEquals(time, file.getUploadedAt());
    }

    @Test
    void testAllArgsConstructor() {
        Contract contract = new Contract();
        contract.setId(300L);
        byte[] pdfBytes = {42, 43, 44};
        LocalDateTime uploadTime = LocalDateTime.of(2025, 11, 1, 9, 0);

        ContractFile file = new ContractFile(3L, contract, pdfBytes, uploadTime);

        assertEquals(3L, file.getId());
        assertEquals(300L, file.getContract().getId());
        assertArrayEquals(pdfBytes, file.getSignedPdf());
        assertEquals(uploadTime, file.getUploadedAt());
    }

    @Test
    void testBinaryPdfDataIntegrity() {
        byte[] pdf1 = {7, 8, 9};
        byte[] pdf2 = {7, 8, 9};
        ContractFile file1 = new ContractFile();
        ContractFile file2 = new ContractFile();

        file1.setSignedPdf(pdf1);
        file2.setSignedPdf(pdf2);

        assertTrue(Arrays.equals(file1.getSignedPdf(), file2.getSignedPdf()));
    }

    @Test
    void testToStringNotNull() {
        ContractFile file = new ContractFile();
        file.setId(99L);
        assertNotNull(file.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        ContractFile f1 = new ContractFile();
        ContractFile f3 = new ContractFile();

        assertSame(f1, f1);   // อ้างอิงเดียวกัน
        assertNotSame(f1, f3); // คนละอ้างอิง
    }
}
