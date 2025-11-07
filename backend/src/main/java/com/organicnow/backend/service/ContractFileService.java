package com.organicnow.backend.service;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import com.organicnow.backend.repository.ContractFileRepository;
import com.organicnow.backend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
     * ✅ อัปโหลดไฟล์ PDF ที่เซ็นแล้ว
     */
    public void uploadSignedFile(Long contractId, MultipartFile file) throws IOException {
        // ตรวจว่ามี Contract จริงไหม
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found with ID: " + contractId));

        // ถ้ามี record เดิมอยู่แล้วให้ update ทับ
        ContractFile cf = contractFileRepository.findByContract(contract).orElse(null);
        if (cf == null) {
            cf = new ContractFile();
            cf.setContract(contract);
        }

        // ✅ set binary data และ timestamp
        byte[] fileBytes = file.getBytes();
        cf.setSignedPdf(fileBytes);
        cf.setUploadedAt(LocalDateTime.now());

        // 🧩 DEBUG log เพื่อดูชนิดข้อมูลก่อน save
        log.info(">>> [DEBUG] signedPdf type before save = {}",
                (cf.getSignedPdf() == null ? "null" : cf.getSignedPdf().getClass().getName()));
        log.info(">>> [DEBUG] file size = {} bytes", fileBytes.length);

        // ✅ save ลง database
        contractFileRepository.save(cf);

        log.info("✅ Signed contract uploaded successfully for contractId = {}", contractId);
    }

    /**
     * ✅ ดึงไฟล์ PDF ที่เซ็นแล้ว (สำหรับดาวน์โหลด)
     * แก้ไข: เพิ่ม @Transactional(readOnly = true)
     * เพื่อให้ Hibernate session เปิดระหว่างอ่าน bytea
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
        if (contractOpt.isEmpty()) {
            return false;
        }

        boolean exists = contractFileRepository.existsByContract(contractOpt.get());
        log.debug("🔍 hasSignedFile(contractId={}) = {}", contractId, exists);
        return exists;
    }

    /**
     * ✅ ลบไฟล์เซ็นแล้ว (เวลา contract ถูกลบ)
     */
    public void deleteByContractId(Long contractId) {
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        contractOpt.ifPresent(contract -> {
            log.info("🗑️ Deleting signed contract file for contractId = {}", contractId);
            contractFileRepository.deleteByContract(contract);
        });
    }
}