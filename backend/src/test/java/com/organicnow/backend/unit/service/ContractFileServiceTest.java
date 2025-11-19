package com.organicnow.backend.unit.service;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import com.organicnow.backend.repository.ContractFileRepository;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.service.ContractFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContractFileServiceTest {

    private ContractFileRepository contractFileRepository;
    private ContractRepository contractRepository;
    private ContractFileService service;

    @BeforeEach
    void setup() {
        contractFileRepository = mock(ContractFileRepository.class);
        contractRepository = mock(ContractRepository.class);
        service = new ContractFileService(contractFileRepository, contractRepository);
    }

    // -------------------------------------------------------
    // ✅ uploadSignedFile() — success + replace old file
    // -------------------------------------------------------
    @Test
    void testUploadSignedFile_replacesOldFileAndSavesNewOne() throws IOException {

        Long contractId = 5L;

        Contract contract = new Contract();
        contract.setId(contractId);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("PDF_DATA".getBytes());
        when(file.getSize()).thenReturn(8L);

        ContractFile oldFile = new ContractFile();
        oldFile.setId(50L);
        oldFile.setContract(contract);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractFileRepository.findByContract(contract)).thenReturn(Optional.of(oldFile));

        service.uploadSignedFile(contractId, file);

        // old file must be deleted
        verify(contractFileRepository, times(1)).delete(oldFile);
        verify(contractFileRepository, times(1)).flush();

        // new file must be saved
        ArgumentCaptor<ContractFile> captor = ArgumentCaptor.forClass(ContractFile.class);
        verify(contractFileRepository).saveAndFlush(captor.capture());

        ContractFile saved = captor.getValue();

        assertEquals(contract, saved.getContract());
        assertArrayEquals("PDF_DATA".getBytes(), saved.getSignedPdf());
        assertNotNull(saved.getUploadedAt());
    }

    // -------------------------------------------------------
    // ❌ uploadSignedFile() — contract not found
    // -------------------------------------------------------
    @Test
    void testUploadSignedFile_contractNotFound_throwsException() {

        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        MultipartFile file = mock(MultipartFile.class);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadSignedFile(999L, file)
        );

        assertEquals("Contract not found with ID: 999", ex.getMessage());
    }

    // -------------------------------------------------------
    // 🟩 getSignedFile() — found
    // -------------------------------------------------------
    @Test
    void testGetSignedFile_success() {

        Contract contract = new Contract();
        contract.setId(10L);

        ContractFile cf = new ContractFile();
        cf.setSignedPdf("HELLO".getBytes());

        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractFileRepository.findByContract(contract)).thenReturn(Optional.of(cf));

        byte[] file = service.getSignedFile(10L);

        assertNotNull(file);
        assertArrayEquals("HELLO".getBytes(), file);
    }

    // -------------------------------------------------------
    // ⚠️ getSignedFile() — file record empty => return null
    // -------------------------------------------------------
    @Test
    void testGetSignedFile_notFound_returnsNull() {

        Contract contract = new Contract();
        contract.setId(20L);

        when(contractRepository.findById(20L)).thenReturn(Optional.of(contract));
        when(contractFileRepository.findByContract(contract)).thenReturn(Optional.empty());

        byte[] file = service.getSignedFile(20L);

        assertNull(file);
    }

    // -------------------------------------------------------
    // ⚠️ getSignedFile() — record exists but signedPdf = null
    // -------------------------------------------------------
    @Test
    void testGetSignedFile_recordExistsButNullPdf_returnsNull() {

        Contract contract = new Contract();
        contract.setId(30L);

        ContractFile cf = new ContractFile();
        cf.setSignedPdf(null);

        when(contractRepository.findById(30L)).thenReturn(Optional.of(contract));
        when(contractFileRepository.findByContract(contract)).thenReturn(Optional.of(cf));

        byte[] data = service.getSignedFile(30L);

        assertNull(data); // เพราะ signedPdf เป็น null
    }

    // -------------------------------------------------------
    // ❌ getSignedFile() — contract not found
    // -------------------------------------------------------
    @Test
    void testGetSignedFile_contractNotFound_throwsException() {

        when(contractRepository.findById(50L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getSignedFile(50L)
        );

        assertEquals("Contract not found with ID: 50", ex.getMessage());
    }

    // -------------------------------------------------------
    // 🔍 hasSignedFile()
    // -------------------------------------------------------
    @Test
    void testHasSignedFile_exists() {

        Contract c = new Contract();
        c.setId(5L);

        when(contractRepository.findById(5L)).thenReturn(Optional.of(c));
        when(contractFileRepository.existsByContract(c)).thenReturn(true);

        assertTrue(service.hasSignedFile(5L));
    }

    @Test
    void testHasSignedFile_notExists() {

        Contract c = new Contract();
        c.setId(5L);

        when(contractRepository.findById(5L)).thenReturn(Optional.of(c));
        when(contractFileRepository.existsByContract(c)).thenReturn(false);

        assertFalse(service.hasSignedFile(5L));
    }

    @Test
    void testHasSignedFile_contractNotFound_returnsFalse() {

        when(contractRepository.findById(5L)).thenReturn(Optional.empty());

        assertFalse(service.hasSignedFile(5L));
    }

    // -------------------------------------------------------
    // 🗑 deleteByContractId()
    // -------------------------------------------------------
    @Test
    void testDeleteByContractId_deletesFile() {

        Contract c = new Contract();
        c.setId(7L);

        when(contractRepository.findById(7L)).thenReturn(Optional.of(c));

        service.deleteByContractId(7L);

        verify(contractFileRepository).deleteByContract(c);
        verify(contractFileRepository).flush();
    }

    @Test
    void testDeleteByContractId_notFound_noDeleteCalled() {

        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        service.deleteByContractId(999L);

        verify(contractFileRepository, never()).deleteByContract(any());
    }
}
