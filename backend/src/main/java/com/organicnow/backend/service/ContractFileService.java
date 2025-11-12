package com.organicnow.backend.service;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import com.organicnow.backend.repository.ContractFileRepository;
import com.organicnow.backend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractFileService {

    private final ContractFileRepository contractFileRepository;
    private final ContractRepository contractRepository;

    /**
     * ✅ อัปโหลดไฟล์ PDF ที่เซ็นแล้ว (รองรับ re-upload แบบไม่พัง PostgreSQL LOB)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadSignedFile(Long contractId, MultipartFile file) throws IOException {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found with ID: " + contractId));

        // 🔹 ขั้นตอน 1: ลบไฟล์เก่าก่อน และบังคับ flush ให้ DB ลบทันที
        contractFileRepository.findByContract(contract).ifPresent(existing -> {
            log.info("♻️ Found existing signed contract — deleting old record for contractId = {}", contractId);
            contractFileRepository.delete(existing);
            contractFileRepository.flush(); // 💥 สำคัญมาก: บังคับให้ DELETE ทันที
        });

        // 🔹 ขั้นตอน 2: สร้าง record ใหม่
        ContractFile newFile = new ContractFile();
        newFile.setContract(contract);
        newFile.setSignedPdf(file.getBytes());
        newFile.setUploadedAt(LocalDateTime.now());

        // 🧩 DEBUG
        log.info(">>> [DEBUG] signedPdf type before save = {}",
                (newFile.getSignedPdf() == null ? "null" : newFile.getSignedPdf().getClass().getName()));
        log.info(">>> [DEBUG] file size = {} bytes", file.getSize());

        // ✅ Save ลง DB
        contractFileRepository.saveAndFlush(newFile); // flush เพื่อ commit insert ใหม่ทันที
        log.info("✅ Signed contract uploaded successfully for contractId = {}", contractId);
    }

    /**
     * ✅ ดึงไฟล์ PDF ที่เซ็นแล้ว
     */
    @Transactional(readOnly = true)
    public byte[] getSignedFile(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found with ID: " + contractId));

        Optional<ContractFile> fileOpt = contractFileRepository.findByContract(contract);
        if (fileOpt.isEmpty()) {
            log.warn("⚠️ No signed contract found for ID: {}", contractId);
            return null;
        }

        ContractFile cf = fileOpt.get();
        byte[] data = cf.getSignedPdf();

        if (data == null) {
            log.warn("⚠️ ContractFile record found but signedPdf is null for ID: {}", contractId);
        } else {
            log.info("📄 Retrieved signed contract file for contractId = {}, size = {} bytes",
                    contractId, data.length);
        }

        return data;
    }

    /**
     * ✅ ตรวจว่า contract มีไฟล์เซ็นแล้วหรือไม่
     */
    public boolean hasSignedFile(Long contractId) {
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) return false;

        boolean exists = contractFileRepository.existsByContract(contractOpt.get());
        log.debug("🔍 hasSignedFile(contractId={}) = {}", contractId, exists);
        return exists;
    }

    /**
     * ✅ ลบไฟล์เซ็นแล้ว (เวลา contract ถูกลบ)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteByContractId(Long contractId) {
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        contractOpt.ifPresent(contract -> {
            log.info("🗑️ Deleting signed contract file for contractId = {}", contractId);
            contractFileRepository.deleteByContract(contract);
            contractFileRepository.flush();
        });
    }
}