package com.organicnow.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.organicnow.backend.dto.*;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final OutstandingBalanceService outstandingBalanceService;
    private final QRCodeService qrCodeService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              ContractRepository contractRepository,
                              RoomRepository roomRepository,
                              PaymentRecordRepository paymentRecordRepository,
                              OutstandingBalanceService outstandingBalanceService,
                              QRCodeService qrCodeService) {
        this.invoiceRepository = invoiceRepository;
        this.contractRepository = contractRepository;
        this.roomRepository = roomRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.outstandingBalanceService = outstandingBalanceService;
        this.qrCodeService = qrCodeService;
    }

    // ===== CRUD =====
    @Override
    public List<InvoiceDto> getAllInvoices() {
        // อัปเดต penalty อัตโนมัติก่อนส่งข้อมูล
        updateOverduePenalties();
        
        // ✅ ใช้วิธีเดิม (รีเวิร์ท)
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<InvoiceDto> getInvoiceById(Long id) {
        return invoiceRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public InvoiceDto createInvoice(CreateInvoiceRequest request) {
        System.out.println("🚀 Received request: " + request);
        
        // ✅ ตรวจสอบว่าต้องการสร้างใบแจ้งหนี้แบบรวมยอดค้างหรือไม่
        if (request.getContractId() != null && request.getIncludeOutstandingBalance() != null && request.getIncludeOutstandingBalance()) {
            return createInvoiceWithOutstandingBalance(request);
        }
        
        // ✅ วิธีเดิม - สร้างใบแจ้งหนี้ปกติ
        return createRegularInvoice(request);
    }

    /**
     * สร้างใบแจ้งหนี้แบบรวมยอดค้างชำระ
     */
    private InvoiceDto createInvoiceWithOutstandingBalance(CreateInvoiceRequest request) {
        System.out.println("💰 Creating invoice with outstanding balance for contract: " + request.getContractId());
        
        // คำนวณค่าใช้จ่ายเดือนปัจจุบัน
        int rent = nullSafeInt(request.getRentAmount());
        int waterAmount = nullSafeInt(request.getWater());
        int electricityAmount = nullSafeInt(request.getElectricity());
        int currentMonthCharges = rent + waterAmount + electricityAmount;
        
        // ใช้ OutstandingBalanceService สร้างใบแจ้งหนี้
        Invoice invoice = outstandingBalanceService.createInvoiceWithOutstandingBalance(
            request.getContractId(), 
            currentMonthCharges
        );
        
        // เพิ่มข้อมูลเพิ่มเติมจาก request
        populateInvoiceFromRequest(invoice, request);
        invoice = invoiceRepository.save(invoice);
        
        System.out.println("✅ Invoice created with outstanding balance - Total: " + invoice.getNetAmount());
        return convertToDto(invoice);
    }

    /**
     * สร้างใบแจ้งหนี้ปกติ (วิธีเดิม)
     */
    private InvoiceDto createRegularInvoice(CreateInvoiceRequest request) {
        System.out.println("📋 Package ID: " + request.getPackageId() + ", Floor: " + request.getFloor() + ", Room: " + request.getRoom());
        System.out.println("💰 Rent: " + request.getRentAmount() + ", Water Unit: " + request.getWaterUnit() + ", Elec Unit: " + request.getElectricityUnit());
        System.out.println("🔧 Water Bill: " + request.getWater() + ", Electricity Bill: " + request.getElectricity());
        System.out.println("📊 SubTotal: " + request.getSubTotal() + ", NET: " + request.getNetAmount());
        
        // ----- 1) เตรียมอินพุต -----
        LocalDateTime createDate = parseCreateDateOrNow(request.getCreateDate());

        int penalty = nullSafeInt(request.getPenaltyTotal());
        
        // ✅ ใช้ข้อมูลจาก request โดยตรง ไม่ต้องพึ่ง contract
        int rent = nullSafeInt(request.getRentAmount());

        Integer uiElecUnit = request.getElecUnit(); // alias จาก UI
        int waterUnit = request.getWaterUnit() != null ? request.getWaterUnit() : 0;
        int waterRate = request.getWaterRate() != null ? request.getWaterRate() : 30; // default rate
        int electricityUnit = request.getElectricityUnit() != null ? request.getElectricityUnit()
                : (uiElecUnit != null ? uiElecUnit : 0);
        int electricityRate = request.getElectricityRate() != null ? request.getElectricityRate() : 8; // default rate

        Integer waterAmountFromUi = request.getWater();
        Integer elecAmountFromUi = request.getElectricity();
        int waterAmount = (waterAmountFromUi != null) ? waterAmountFromUi : waterUnit * waterRate;
        int electricityAmount = (elecAmountFromUi != null) ? elecAmountFromUi : electricityUnit * electricityRate;

        Integer subTotal = request.getSubTotal();
        if (subTotal == null) subTotal = rent + waterAmount + electricityAmount;

        Integer netAmount = request.getNetAmount();
        if (netAmount == null) netAmount = subTotal + penalty;

        Integer invoiceStatus = request.getInvoiceStatus() != null ? request.getInvoiceStatus() : 0;

        LocalDateTime dueDate = (request.getDueDate() != null) ? request.getDueDate()
                : createDate.plusDays(30);

        // ----- Auto Penalty Calculation -----
        // คำนวณ penalty อัตโนมัติถ้าเกินวันครบกำหนดและ status = Incomplete (0)
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(dueDate);
        boolean isIncomplete = invoiceStatus == 0; // 0 = Incomplete
        
        if (isOverdue && isIncomplete && penalty == 0) {
            // คิด penalty 10% ของค่าเช่า
            penalty = Math.round(rent * 0.1f);
            System.out.println("⚠️ Auto penalty applied: " + penalty + " (10% of rent: " + rent + ") - Status: Incomplete, Overdue");
        }
        
        // อัปเดต netAmount ใหม่รวม penalty (override จาก request)
        netAmount = subTotal + penalty;

        // ----- 2) สร้าง/บันทึก Entity -----
        Invoice inv = new Invoice();
        inv.setCreateDate(createDate);
        inv.setDueDate(dueDate);
        inv.setInvoiceStatus(invoiceStatus);
        inv.setSubTotal(subTotal);
        inv.setPenaltyTotal(penalty);
        inv.setNetAmount(netAmount);

        // ต้องผูก Contract (contact) เพราะ nullable=false
        Contract contract = null;
        
        // หาก contractId มีค่า ใช้วิธีเดิม
        if (request.getContractId() != null) {
            contract = contractRepository.findById(request.getContractId())
                    .orElseThrow(() -> new RuntimeException("Contract not found: " + request.getContractId()));
        }
        // หากไม่มี contractId ให้ใช้ contract ใดๆ เป็น placeholder เนื่องจาก DB constraint
        else {
            List<Contract> existingContracts = contractRepository.findAll();
            if (!existingContracts.isEmpty()) {
                contract = existingContracts.get(0); // ใช้ contract แรกเป็น placeholder
                System.out.println("⚠️ Using placeholder contract: " + contract.getId() + 
                        " for request floor: " + request.getFloor() + " room: " + request.getRoom());
            } else {
                throw new RuntimeException("No contracts available in system");
            }
        }
        
        inv.setContact(contract);

        // เพิ่มข้อมูลเพิ่มเติมจาก request
        populateInvoiceFromRequest(inv, request);
        
        Invoice saved = invoiceRepository.save(inv);
        
        // ✅ สร้าง DTO response โดยใช้ข้อมูลจาก request แทนข้อมูลจาก contract
        InvoiceDto result = convertToDto(saved);
        
        // ✅ Override ข้อมูลที่สำคัญด้วยข้อมูลจาก request
        if (request.getFloor() != null) {
            try {
                result.setFloor(Integer.parseInt(request.getFloor()));
            } catch (NumberFormatException e) {
                 System.out.println("⚠️ Invalid floor format for DTO override: " + request.getFloor());
            }
        }
        if (request.getRoom() != null) {
            result.setRoom(request.getRoom());
        }
        result.setRent(rent);
        result.setWaterUnit(waterUnit);
        result.setWater(waterAmount);
        result.setElectricityUnit(electricityUnit);
        result.setElectricity(electricityAmount);
        
        System.out.println("✅ Final result DTO: Floor=" + result.getFloor() + 
                ", Room=" + result.getRoom() + ", Rent=" + result.getRent());
        
        return result;
    }

    /**
     * Helper method สำหรับเพิ่มข้อมูลจาก request ลงใน invoice
     */
    private void populateInvoiceFromRequest(Invoice invoice, CreateInvoiceRequest request) {
        // ✅ เก็บข้อมูลจาก request สำหรับการแสดงผล
        invoice.setPackageId(request.getPackageId());
        
        // แปลง floor จาก String เป็น Integer
        Integer floorNum = null;
        try {
            if (request.getFloor() != null && !request.getFloor().trim().isEmpty()) {
                floorNum = Integer.parseInt(request.getFloor().trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid floor format: " + request.getFloor());
        }
        invoice.setRequestedFloor(floorNum);
        invoice.setRequestedRoom(request.getRoom());
        
        // คำนวณและเก็บข้อมูลจาก request
        int rent = nullSafeInt(request.getRentAmount());
        Integer uiElecUnit = request.getElecUnit();
        int waterUnit = request.getWaterUnit() != null ? request.getWaterUnit() : 0;
        int waterRate = request.getWaterRate() != null ? request.getWaterRate() : 30;
        int electricityUnit = request.getElectricityUnit() != null ? request.getElectricityUnit()
                : (uiElecUnit != null ? uiElecUnit : 0);

        Integer waterAmountFromUi = request.getWater();
        Integer elecAmountFromUi = request.getElectricity();
        int waterAmount = (waterAmountFromUi != null) ? waterAmountFromUi : waterUnit * waterRate;
        int electricityAmount = (elecAmountFromUi != null) ? elecAmountFromUi : electricityUnit * 8;

        invoice.setRequestedRent(rent);
        invoice.setRequestedWater(waterAmount);
        invoice.setRequestedWaterUnit(waterUnit);
        invoice.setRequestedElectricity(electricityAmount);
        invoice.setRequestedElectricityUnit(electricityUnit);
        
        System.out.println("💾 Populating from request - Water: " + waterAmount + " (" + waterUnit + " units), Electricity: " + electricityAmount + " (" + electricityUnit + " units)");
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));

        // ===== วันที่สร้าง (สำหรับทดสอบ) =====
        if (request.getCreateDate() != null) {
            inv.setCreateDate(request.getCreateDate());
        }

        // ===== วันที่ครบกำหนด =====
        if (request.getDueDate() != null) {
            inv.setDueDate(request.getDueDate());
        }

        // ===== สถานะ / วันจ่ายจริง =====
        if (request.getInvoiceStatus() != null) {
            inv.setInvoiceStatus(request.getInvoiceStatus());

            // ถ้า set เป็นชำระแล้ว(1) แต่ไม่ส่ง payDate และยังไม่มีค่า → ตั้งเป็น now()
            if (request.getInvoiceStatus() == 1 && request.getPayDate() == null && inv.getPayDate() == null) {
                inv.setPayDate(LocalDateTime.now());
            }
        }
        if (request.getPayDate() != null) {
            inv.setPayDate(request.getPayDate());
        }

        // ===== วิธีชำระ =====
        if (request.getPayMethod() != null) {
            inv.setPayMethod(request.getPayMethod());
        }

        // ===== ยอดเงิน =====
        boolean amountTouched = false;
        if (request.getSubTotal() != null) {
            inv.setSubTotal(Math.max(0, request.getSubTotal()));
            amountTouched = true;
        }
        if (request.getPenaltyTotal() != null) {
            inv.setPenaltyTotal(Math.max(0, request.getPenaltyTotal()));
            amountTouched = true;
        }

        // ===== หน่วยน้ำและไฟ =====
        if (request.getWaterUnit() != null) {
            int waterUnit = Math.max(0, request.getWaterUnit());
            inv.setRequestedWaterUnit(waterUnit);
            // คำนวณค่าน้ำจากหน่วย (30 บาท/หน่วย)
            inv.setRequestedWater(waterUnit * 30);
        }
        if (request.getElectricityUnit() != null) {
            int electricityUnit = Math.max(0, request.getElectricityUnit());
            inv.setRequestedElectricityUnit(electricityUnit);
            // คำนวณค่าไฟจากหน่วย (6.5 บาท/หน่วย)
            inv.setRequestedElectricity((int) Math.round(electricityUnit * 6.5));
        }

        if (request.getPenaltyAppliedAt() != null) {
            inv.setPenaltyAppliedAt(request.getPenaltyAppliedAt());
        }

        if (request.getNetAmount() != null) {
            inv.setNetAmount(Math.max(0, request.getNetAmount()));
        } else if (amountTouched) {
            int st = inv.getSubTotal() != null ? inv.getSubTotal() : 0;
            int pt = inv.getPenaltyTotal() != null ? inv.getPenaltyTotal() : 0;
            inv.setNetAmount(st + pt);
        }

        // notes: Entity ยังไม่มีฟิลด์นี้ — ไม่ทำอะไร

        Invoice saved = invoiceRepository.save(inv);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        try {
            if (!invoiceRepository.existsById(id)) {
                throw new RuntimeException("Invoice not found: " + id);
            }
            
            System.out.println("🗑️ Starting delete process for Invoice ID: " + id);
            
            // 1. ลบ PaymentRecord ที่เกี่ยวข้องก่อน
            paymentRecordRepository.deleteByInvoiceId(id);
            System.out.println("✅ Deleted PaymentRecords for Invoice ID: " + id);
            
            // 2. แล้วค่อยลบ Invoice
            invoiceRepository.deleteById(id);
            System.out.println("✅ Deleted Invoice ID: " + id);
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting Invoice ID: " + id + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("ไม่สามารถลบใบแจ้งหนี้ได้: " + e.getMessage(), e);
        }
    }

    // ===== Search/Filter (ยังไม่ implement) =====
    @Override public List<InvoiceDto> searchInvoices(String query) { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByContractId(Long contractId) { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByRoomId(Long roomId) { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByTenantId(Long tenantId) { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByStatus(Integer status) { return List.of(); }
    @Override public List<InvoiceDto> getUnpaidInvoices() { return List.of(); }
    @Override public List<InvoiceDto> getPaidInvoices() { return List.of(); }
    @Override public List<InvoiceDto> getOverdueInvoices() { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) { return List.of(); }
    @Override public List<InvoiceDto> getInvoicesByNetAmountRange(Integer minAmount, Integer maxAmount) { return List.of(); }
    @Override public InvoiceDto markAsPaid(Long id) { throw new UnsupportedOperationException("markAsPaid not implemented yet"); }
    @Override public InvoiceDto cancelInvoice(Long id) { throw new UnsupportedOperationException("cancelInvoice not implemented yet"); }
    @Override public InvoiceDto addPenalty(Long id, Integer penaltyAmount) { throw new UnsupportedOperationException("addPenalty not implemented yet"); }

    // ===== Utils =====
    private int nullSafeInt(Integer v) { return v != null ? v : 0; }

    private LocalDateTime parseCreateDateOrNow(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        try {
            LocalDate d = LocalDate.parse(dateStr);
            return d.atStartOfDay();
        } catch (Exception ex) {
            return LocalDateTime.now();
        }
    }

    // แปลง Invoice -> InvoiceDto
    private InvoiceDto convertToDto(Invoice invoice) {
        // ✅ ดึงข้อมูล tenant ล่าสุดจาก room assignment แทนการใช้ contract เก่า
        Contract currentContract = null;
        String currentFirstName = "N/A";
        String currentLastName = "";
        String currentNationalId = "";
        String currentPhoneNumber = "";
        String currentEmail = "";
        String currentPackageName = "N/A";
        
        // หาข้อมูล tenant ปัจจุบันจาก room
        if (invoice.getRequestedFloor() != null && invoice.getRequestedRoom() != null) {
            currentContract = roomRepository.findCurrentContractByRoomFloorAndNumber(
                    invoice.getRequestedFloor(), 
                    invoice.getRequestedRoom()
            );
        }
        
        // ถ้าไม่เจอจาก requested room ให้ลองจาก contract เดิม
        if (currentContract == null && invoice.getContact() != null && invoice.getContact().getRoom() != null) {
            currentContract = roomRepository.findCurrentContractByRoomFloorAndNumber(
                    invoice.getContact().getRoom().getRoomFloor(),
                    invoice.getContact().getRoom().getRoomNumber()
            );
        }
        
        // ใช้ current contract ถ้าเจอ, ไม่งั้นใช้ contract เดิม
        Contract dataSource = currentContract != null ? currentContract : invoice.getContact();
        
        if (dataSource != null && dataSource.getTenant() != null) {
            currentFirstName = dataSource.getTenant().getFirstName();
            currentLastName = dataSource.getTenant().getLastName();
            currentNationalId = dataSource.getTenant().getNationalId();
            currentPhoneNumber = dataSource.getTenant().getPhoneNumber();
            currentEmail = dataSource.getTenant().getEmail();
            
            if (dataSource.getPackagePlan() != null && dataSource.getPackagePlan().getContractType() != null) {
                currentPackageName = dataSource.getPackagePlan().getContractType().getName();
            }
        }

        // ดึงข้อมูล Payment Records
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(invoice.getId());
        List<PaymentRecordDto> paymentRecordDtos = paymentRecords.stream()
                .map(PaymentRecordDto::fromEntity)
                .toList();
        
        // คำนวณยอดเงินการชำระ - แก้ไขให้ถูกต้อง 🔥
        BigDecimal totalPaid = paymentRecordRepository.calculateTotalPaidAmount(invoice.getId()); // CONFIRMED only
        BigDecimal totalPending = paymentRecordRepository.calculateTotalPendingAmount(invoice.getId()); // PENDING only
        BigDecimal totalReceived = paymentRecordRepository.calculateTotalReceivedAmount(invoice.getId()); // CONFIRMED + PENDING
        
        // 🔧 แก้ไข: ใช้ netAmount ที่คำนวณจริง ไม่ใช่ค่าที่บันทึกไว้
        int realSubTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 0;
        int realPenalty = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
        int realNetAmount = realSubTotal + realPenalty;
        
        BigDecimal invoiceAmount = BigDecimal.valueOf(realNetAmount);
        BigDecimal remainingAmount = invoiceAmount.subtract(totalReceived != null ? totalReceived : BigDecimal.ZERO);
        
        System.out.println("💰 Invoice #" + invoice.getId() + " - SubTotal: " + realSubTotal + 
                         ", Penalty: " + realPenalty + ", NetAmount: " + realNetAmount + 
                         ", Paid: " + (totalReceived != null ? totalReceived.intValue() : 0) + 
                         ", Remaining: " + remainingAmount.intValue());

        // 🔥 Outstanding Balance Logic - แยกระหว่างยอดค้างของ Invoice นี้ กับยอดค้างจากใบอื่น
        int contractId = invoice.getContact() != null ? invoice.getContact().getId().intValue() : 0;
        int outstandingFromOtherInvoices = 0; // ยอดค้างจากใบแจ้งหนี้อื่น
        boolean hasOutstandingFromOthers = false;
        
        // คำนวณยอดคงเหลือของ Invoice นี้
        int currentInvoiceRemaining = remainingAmount.intValue(); // ยอดคงเหลือของใบนี้
        boolean hasCurrentRemaining = currentInvoiceRemaining > 0;
        
        try {
            if (contractId > 0) {
                // 🔧 แก้ไข: คำนวณยอดค้างจากใบแจ้งหนี้ที่สร้างก่อนหน้านี้เท่านั้น (ใช้ createDate)
                List<Invoice> earlierUnpaidInvoices = invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(Long.valueOf(contractId), 0);
                for (Invoice otherInvoice : earlierUnpaidInvoices) {
                    // ✅ เฉพาะใบที่สร้างก่อนหน้า และไม่ใช่ใบปัจจุบัน
                    if (!otherInvoice.getId().equals(invoice.getId()) && 
                        otherInvoice.getCreateDate().isBefore(invoice.getCreateDate())) {
                        
                        // 🔧 คำนวณยอดคงเหลือจริงของใบก่อนหน้า
                        BigDecimal otherReceived = paymentRecordRepository.calculateTotalReceivedAmount(otherInvoice.getId());
                        int otherReceivedAmount = otherReceived != null ? otherReceived.intValue() : 0;
                        
                        // คำนวณ netAmount จริงของใบก่อนหน้า
                        int otherSubTotal = otherInvoice.getSubTotal() != null ? otherInvoice.getSubTotal() : 0;
                        int otherPenalty = otherInvoice.getPenaltyTotal() != null ? otherInvoice.getPenaltyTotal() : 0;
                        int otherNetAmount = otherSubTotal + otherPenalty;
                        
                        // ยอดคงเหลือ = NetAmount - ยอดที่ได้รับ (รวม pending)
                        int otherRemaining = otherNetAmount - otherReceivedAmount;
                        
                        System.out.println("🔍 Previous Invoice #" + otherInvoice.getId() + 
                                         " - SubTotal: " + otherSubTotal + 
                                         ", Penalty: " + otherPenalty + 
                                         ", NetAmount: " + otherNetAmount + 
                                         ", Received: " + otherReceivedAmount + 
                                         ", Remaining: " + otherRemaining);
                        
                        if (otherRemaining > 0) {
                            outstandingFromOtherInvoices += otherRemaining;
                        }
                    }
                }
                hasOutstandingFromOthers = outstandingFromOtherInvoices > 0;
                System.out.println("🔍 Invoice #" + invoice.getId() + " (Created: " + invoice.getCreateDate() + 
                                 ") Current Remaining: " + currentInvoiceRemaining + 
                                 " บาท, Outstanding from Earlier Invoices: " + outstandingFromOtherInvoices + " บาท");
            }
        } catch (Exception e) {
            System.err.println("❌ Error calculating outstanding balance for Invoice #" + invoice.getId() + ": " + e.getMessage());
        }

        return InvoiceDto.builder()
                .id(invoice.getId())
                .contractId(invoice.getContact() != null ? invoice.getContact().getId() : null)
                .createDate(invoice.getCreateDate())
                .dueDate(invoice.getDueDate())
                .invoiceStatus(invoice.getInvoiceStatus())
                .payDate(invoice.getPayDate())
                .payMethod(invoice.getPayMethod())
                .subTotal(invoice.getSubTotal())
                .penaltyTotal(invoice.getPenaltyTotal())
                .netAmount(realNetAmount) // ✅ ใช้ค่าที่คำนวณใหม่
                .penaltyAppliedAt(invoice.getPenaltyAppliedAt())
                // Payment Information
                .paymentRecords(paymentRecordDtos)
                .totalPaidAmount(totalPaid)
                .totalPendingAmount(totalPending)
                .remainingAmount(remainingAmount) // ✅ ใช้ค่าที่คำนวณใหม่
                // Outstanding Balance Information - แสดงยอดคงเหลือของใบนี้และยอดค้างจากใบอื่น 🔥
                .previousBalance(outstandingFromOtherInvoices) // ยอดค้างจากใบแจ้งหนี้อื่น
                .paidAmount(totalReceived.intValue()) // ยอดที่ชำระแล้วของใบนี้
                .outstandingBalance(currentInvoiceRemaining + outstandingFromOtherInvoices) // ยอดคงเหลือรวมทั้งหมด
                .hasOutstandingBalance(hasOutstandingFromOthers || hasCurrentRemaining) // มียอดค้างรวมหรือไม่
                // ✅ ใช้ข้อมูล tenant ปัจจุบัน
                .firstName(currentFirstName)
                .lastName(currentLastName)
                .nationalId(currentNationalId)
                .phoneNumber(currentPhoneNumber)
                .email(currentEmail)
                .packageName(currentPackageName)
                // Contract dates (ใช้ข้อมูลจาก invoice contract เดิม)
                .signDate(invoice.getContact() != null ? invoice.getContact().getSignDate() : null)
                .startDate(invoice.getContact() != null ? invoice.getContact().getStartDate() : null)
                .endDate(invoice.getContact() != null ? invoice.getContact().getEndDate() : null)
                // Room info - ใช้ข้อมูลจาก request หากมี, ไม่งั้นดึงจาก contract
                .floor(invoice.getRequestedFloor() != null 
                        ? invoice.getRequestedFloor() 
                        : (invoice.getContact() != null && invoice.getContact().getRoom() != null
                            ? invoice.getContact().getRoom().getRoomFloor() : null))
                .room(invoice.getRequestedRoom() != null 
                        ? invoice.getRequestedRoom()
                        : (invoice.getContact() != null && invoice.getContact().getRoom() != null
                            ? invoice.getContact().getRoom().getRoomNumber() : "N/A"))
                .rent(invoice.getRequestedRent() != null 
                        ? invoice.getRequestedRent()
                        : (invoice.getContact() != null && invoice.getContact().getRentAmountSnapshot() != null
                            ? invoice.getContact().getRentAmountSnapshot().intValue() : 0))
                // ใช้ค่าน้ำและค่าไฟจาก request ที่บันทึกไว้ หรือคำนวณจาก subTotal สำหรับข้อมูลเก่า
                .water(invoice.getRequestedWater() != null && invoice.getRequestedWater() > 0 
                        ? invoice.getRequestedWater() 
                        : (invoice.getSubTotal() != null ? Math.round(invoice.getSubTotal() * 0.2f) : 0))
                .waterUnit(invoice.getRequestedWaterUnit() != null 
                        ? invoice.getRequestedWaterUnit() 
                        : (invoice.getSubTotal() != null ? Math.round((invoice.getSubTotal() * 0.2f) / 30) : 0))
                .electricity(invoice.getRequestedElectricity() != null && invoice.getRequestedElectricity() > 0 
                        ? invoice.getRequestedElectricity() 
                        : (invoice.getSubTotal() != null ? Math.round(invoice.getSubTotal() * 0.8f) : 0))
                .electricityUnit(invoice.getRequestedElectricityUnit() != null 
                        ? invoice.getRequestedElectricityUnit() 
                        : (invoice.getSubTotal() != null ? Math.round((invoice.getSubTotal() * 0.8f) / 8) : 0))
                // Penalty info
                .penalty(invoice.getPenaltyTotal() != null && invoice.getPenaltyTotal() > 0 ? 1 : 0)
                .penaltyDate(invoice.getPenaltyAppliedAt())
                .build();
    }

    /**
     * คำนวณและอัปเดต penalty สำหรับ invoice ที่เกินวันครบกำหนด
     */
    @Transactional
    public void updateOverduePenalties() {
        LocalDateTime now = LocalDateTime.now();
        List<Invoice> overdueInvoices = invoiceRepository.findAll()
                .stream()
                .filter(invoice -> {
                    // ใช้ penaltyAppliedAt เป็น penalty due date, หากไม่มีใช้ dueDate
                    LocalDateTime penaltyDueDate = invoice.getPenaltyAppliedAt() != null ? 
                            invoice.getPenaltyAppliedAt() : invoice.getDueDate();
                    
                    return penaltyDueDate.isBefore(now) && 
                           invoice.getInvoiceStatus() == 0 && // ยังไม่ชำระ
                           invoice.getPenaltyTotal() == 0; // ยังไม่มี penalty
                })
                .toList();

        for (Invoice invoice : overdueInvoices) {
            // คำนวณ penalty 10% ของค่าเช่า
            int rent = invoice.getRequestedRent() != null ? invoice.getRequestedRent() : 
                        (invoice.getContact() != null && invoice.getContact().getRentAmountSnapshot() != null ? 
                         invoice.getContact().getRentAmountSnapshot().intValue() : 0);
            
            int penalty = Math.round(rent * 0.1f);
            
            System.out.println("🔍 Processing Invoice #" + invoice.getId() + 
                    " - Status: " + invoice.getInvoiceStatus() + 
                    " - Penalty Date: " + (invoice.getPenaltyAppliedAt() != null ? invoice.getPenaltyAppliedAt() : invoice.getDueDate()) +
                    " - Rent: " + rent + " - Penalty: " + penalty);
            
            invoice.setPenaltyTotal(penalty);
            if (invoice.getPenaltyAppliedAt() == null) {
                invoice.setPenaltyAppliedAt(now);
            }
            invoice.setNetAmount(invoice.getSubTotal() + penalty);
            
            invoiceRepository.save(invoice);
            System.out.println("📋 Applied penalty to Invoice #" + invoice.getId() + ": " + penalty);
        }
    }
    
    // ===== CSV Import Implementation =====
    
    @Override
    @Transactional
    public String importUtilityUsageFromCsv(MultipartFile file) {
        List<UtilityUsageDto> utilityData = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            // อ่าน header line
            if ((line = reader.readLine()) != null) {
                lineNumber++;
                System.out.println("CSV Header: " + line);
            }
            
            // อ่านข้อมูลแต่ละบรรทัด
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    UtilityUsageDto usage = parseCsvLine(line, lineNumber);
                    if (usage != null) {
                        utilityData.add(usage);
                        
                        // Process และบันทึกข้อมูลทันที
                        boolean success = processUtilityUsage(usage);
                        if (success) {
                            successCount++;
                        } else {
                            errorCount++;
                            errors.add("Line " + lineNumber + ": ไม่พบห้องหมายเลข " + usage.getRoomNumber());
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage());
        }
        
        // สร้าง summary message
        StringBuilder result = new StringBuilder();
        result.append("CSV Import completed successfully!\n");
        result.append("Total processed: ").append(successCount + errorCount).append(" records\n");
        result.append("Successful: ").append(successCount).append(" records\n");
        result.append("Errors: ").append(errorCount).append(" records\n");
        
        if (!errors.isEmpty()) {
            result.append("\nError details:\n");
            for (String error : errors) {
                result.append("- ").append(error).append("\n");
            }
        }
        
        return result.toString();
    }
    
    private UtilityUsageDto parseCsvLine(String line, int lineNumber) {
        String[] data = line.split(",");
        
        // ตรวจสอบจำนวน columns ที่คาดหวัง
        // Format: RoomNumber,WaterUsage,ElectricityUsage,BillingMonth,WaterRate,ElectricityRate
        if (data.length < 4) {
            throw new RuntimeException("Invalid CSV format. Expected at least 4 columns: RoomNumber,WaterUsage,ElectricityUsage,BillingMonth");
        }
        
        try {
            String roomNumber = data[0].trim();
            Integer waterUsage = Integer.parseInt(data[1].trim());
            Integer electricityUsage = Integer.parseInt(data[2].trim());
            String billingMonth = data[3].trim(); // Format: YYYY-MM
            
            // Optional: Water and Electricity rates (defaults if not provided)
            Integer waterRate = data.length > 4 ? Integer.parseInt(data[4].trim()) : 20; // Default 20 บาท/หน่วย
            Integer electricityRate = data.length > 5 ? Integer.parseInt(data[5].trim()) : 8; // Default 8 บาท/หน่วย
            
            return UtilityUsageDto.builder()
                    .roomNumber(roomNumber)
                    .waterUsage(waterUsage)
                    .electricityUsage(electricityUsage)
                    .billingMonth(billingMonth)
                    .waterRate(waterRate)
                    .electricityRate(electricityRate)
                    .build();
                    
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format in CSV data");
        }
    }
    
    private boolean processUtilityUsage(UtilityUsageDto usage) {
        try {
            // หาห้องจากหมายเลขห้อง
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(usage.getRoomNumber());
            if (roomOpt.isEmpty()) {
                System.err.println("Room not found: " + usage.getRoomNumber());
                return false;
            }
            
            Room room = roomOpt.get();
            
            // หา contract ที่ active สำหรับห้องนี้
            Optional<Contract> contractOpt = contractRepository.findActiveContractByRoomId(room.getId());
            if (contractOpt.isEmpty()) {
                System.err.println("No active contract for room: " + usage.getRoomNumber());
                return false;
            }
            
            Contract contract = contractOpt.get();
            
            // ปรับปรุงหรือสร้าง invoice สำหรับเดือนนี้
            updateOrCreateInvoiceWithUtilityUsage(contract, usage);
            
            System.out.println("✅ Updated utility usage for room " + usage.getRoomNumber() + 
                               " - Water: " + usage.getWaterUsage() + " units, " +
                               "Electricity: " + usage.getElectricityUsage() + " units");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error processing utility usage for room " + usage.getRoomNumber() + ": " + e.getMessage());
            return false;
        }
    }
    
    private void updateOrCreateInvoiceWithUtilityUsage(Contract contract, UtilityUsageDto usage) {
        // Parse billing month (YYYY-MM)
        String[] monthParts = usage.getBillingMonth().split("-");
        if (monthParts.length != 2) {
            throw new RuntimeException("Invalid billing month format. Expected YYYY-MM");
        }
        
        int year = Integer.parseInt(monthParts[0]);
        int month = Integer.parseInt(monthParts[1]);
        
        // หา invoice ที่มีอยู่แล้วสำหรับเดือนนี้
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        Optional<Invoice> existingInvoice = invoiceRepository.findByContractAndDateRange(
                contract.getId(), startOfMonth, endOfMonth);
        
        Invoice invoice;
        if (existingInvoice.isPresent()) {
            // อัปเดต invoice ที่มีอยู่
            invoice = existingInvoice.get();
        } else {
            // สร้าง invoice ใหม่
            invoice = createNewInvoiceForMonth(contract, year, month);
        }
        
        // อัปเดตข้อมูลการใช้น้ำ/ไฟ
        invoice.setRequestedWaterUnit(usage.getWaterUsage());
        invoice.setRequestedElectricityUnit(usage.getElectricityUsage());
        
        // คำนวณค่าน้ำ/ไฟ
        Integer waterCost = usage.getWaterUsage() * usage.getWaterRate();
        Integer electricityCost = usage.getElectricityUsage() * usage.getElectricityRate();
        
        invoice.setRequestedWater(waterCost);
        invoice.setRequestedElectricity(electricityCost);
        
        // คำนวณยอดรวมใหม่
        Integer rentAmount = invoice.getRequestedRent() != null ? invoice.getRequestedRent() : 0;
        Integer subTotal = rentAmount + waterCost + electricityCost;
        invoice.setSubTotal(subTotal);
        invoice.setNetAmount(subTotal + (invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0));
        
        // บันทึก
        invoiceRepository.save(invoice);
    }
    
    private Invoice createNewInvoiceForMonth(Contract contract, int year, int month) {
        Invoice invoice = new Invoice();
        invoice.setContact(contract);
        invoice.setCreateDate(LocalDateTime.of(year, month, 1, 0, 0));
        invoice.setDueDate(LocalDateTime.of(year, month, 15, 23, 59)); // กำหนดชำระวันที่ 15
        
        // เก็บข้อมูลจาก contract
        invoice.setRequestedRoom(contract.getRoom().getRoomNumber());
        invoice.setRequestedFloor(contract.getRoom().getRoomFloor());
        invoice.setRequestedRent(contract.getRentAmountSnapshot() != null ? 
                                 contract.getRentAmountSnapshot().intValue() : 0);
        
        // ตั้งค่าเริ่มต้น
        invoice.setInvoiceStatus(0); // ยังไม่ชำระ
        invoice.setSubTotal(0); // จะถูกคำนวณใหม่
        invoice.setPenaltyTotal(0);
        invoice.setNetAmount(0); // จะถูกคำนวณใหม่
        
        return invoice;
    }
    
    // ===== PDF Generation Feature =====
    
    @Override
    public byte[] generateInvoicePdf(Long invoiceId) {
        System.out.println(">>> [InvoiceService] Generating PDF for invoiceId=" + invoiceId);
        
        // ดึงข้อมูล invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
        
        Contract contract = invoice.getContact();
        if (contract == null) {
            throw new RuntimeException("Contract not found for invoice: " + invoiceId);
        }
        
        Tenant tenant = contract.getTenant();
        if (tenant == null) {
            throw new RuntimeException("Tenant not found for contract: " + contract.getId());
        }

        // ใช้ข้อมูลห้องและชั้นจาก 'requested' fields บนตัว invoice
        String floor = (invoice.getRequestedFloor() != null) ? String.valueOf(invoice.getRequestedFloor()) : "N/A";
        String roomNumber = (invoice.getRequestedRoom() != null) ? invoice.getRequestedRoom() : "N/A";
        String roomDisplay = "ชั้น " + floor + " ห้อง " + roomNumber;

        // ข้อมูล Package ดึงจาก contract
        PackagePlan packagePlan = contract.getPackagePlan();
        String packageName = (packagePlan != null && packagePlan.getContractType() != null) ?
                             packagePlan.getContractType().getName() : "ไม่ระบุ";
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // สร้าง PDF document
            Document document = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // สร้างฟอนต์ที่ใช้ในระบบ (อ้างอิงจาก TenantContract)
            Font[] fonts = PdfStyleService.createInvoiceFonts();
            Font titleFont = fonts[0];
            Font headerFont = fonts[1];
            Font labelFont = fonts[2];
            Font normalFont = fonts[3];
            Font smallFont = fonts[4];
            
            // เพิ่ม Company Header
            PdfStyleService.addCompanyHeader(document, titleFont, headerFont);
            
            // หัวข้อใบแจ้งหนี้
            Paragraph invoiceTitle = new Paragraph("SERVICE INVOICE", titleFont);
            invoiceTitle.setAlignment(Element.ALIGN_CENTER);
            invoiceTitle.setSpacingAfter(5);
            document.add(invoiceTitle);
            
            Paragraph invoiceSubtitle = new Paragraph("INVOICE", headerFont);
            invoiceSubtitle.setAlignment(Element.ALIGN_CENTER);
            invoiceSubtitle.setSpacingAfter(20);
            document.add(invoiceSubtitle);
            
            PdfStyleService.addSeparatorLine(document);
            
            // ===== ข้อมูลใบแจ้งหนี้ =====
            PdfPTable invoiceHeaderTable = new PdfPTable(2);
            invoiceHeaderTable.setWidthPercentage(100);
            invoiceHeaderTable.setWidths(new float[]{1, 1});
            invoiceHeaderTable.setSpacingAfter(20);
            
            // ข้อมูลใบแจ้งหนี้
            PdfPCell invoiceInfoCell = new PdfPCell();
            invoiceInfoCell.setBorder(Rectangle.BOX);
            invoiceInfoCell.setPadding(10);
            invoiceInfoCell.setBackgroundColor(PdfStyleService.LIGHT_GRAY);
            
            invoiceInfoCell.addElement(new Paragraph("Invoice Number", labelFont));
            invoiceInfoCell.addElement(new Paragraph("INV-" + String.format("%06d", invoice.getId()), titleFont));
            invoiceInfoCell.addElement(new Paragraph(" ", normalFont)); // spacer
            invoiceInfoCell.addElement(new Paragraph("Issue Date: " + 
                (invoice.getCreateDate() != null ? invoice.getCreateDate().toLocalDate() : "N/A"), normalFont));
            invoiceInfoCell.addElement(new Paragraph("Due Date: " + 
                (invoice.getDueDate() != null ? invoice.getDueDate().toLocalDate() : "N/A"), normalFont));
            
            invoiceHeaderTable.addCell(invoiceInfoCell);
            document.add(invoiceHeaderTable);
            
            // ===== ข้อมูลผู้เช่า =====
            Paragraph customerHeader = new Paragraph("Customer Information", headerFont);
            customerHeader.setSpacingAfter(10);
            document.add(customerHeader);
            
            PdfPTable customerTable = new PdfPTable(2);
            customerTable.setWidthPercentage(100);
            customerTable.setWidths(new float[]{1, 2});
            customerTable.setSpacingAfter(20);
            
            customerTable.addCell(PdfStyleService.createLabelCell("Name:", labelFont));
            customerTable.addCell(PdfStyleService.createValueCell(
                PdfStyleService.nvl(tenant.getFirstName()) + " " + PdfStyleService.nvl(tenant.getLastName()), normalFont));
            
            customerTable.addCell(PdfStyleService.createLabelCell("ID Number:", labelFont));
            customerTable.addCell(PdfStyleService.createValueCell(PdfStyleService.nvl(tenant.getNationalId()), normalFont));
            
            customerTable.addCell(PdfStyleService.createLabelCell("Phone Number:", labelFont));
            customerTable.addCell(PdfStyleService.createValueCell(PdfStyleService.nvl(tenant.getPhoneNumber()), normalFont));
            
            customerTable.addCell(PdfStyleService.createLabelCell("Room Number:", labelFont));
            customerTable.addCell(PdfStyleService.createValueCell(roomDisplay, normalFont));
            
            customerTable.addCell(PdfStyleService.createLabelCell("Package:", labelFont));
            customerTable.addCell(PdfStyleService.createValueCell(packageName, normalFont));
            
            document.add(customerTable);
            
            // ===== รายการค่าใช้จ่าย =====
            Paragraph expenseHeader = new Paragraph("Service Charges", headerFont);
            expenseHeader.setSpacingAfter(10);
            document.add(expenseHeader);
            
            PdfPTable expenseTable = new PdfPTable(4);
            expenseTable.setWidthPercentage(100);
            expenseTable.setWidths(new float[]{3f, 1.5f, 1.5f, 2f});
            expenseTable.setSpacingAfter(15);
            
            // Header ของตาราง
            expenseTable.addCell(PdfStyleService.createHeaderCell("Description", labelFont));
            expenseTable.addCell(PdfStyleService.createHeaderCell("Qty/Unit", labelFont));
            expenseTable.addCell(PdfStyleService.createHeaderCell("Rate (THB)", labelFont));
            expenseTable.addCell(PdfStyleService.createHeaderCell("Amount (THB)", labelFont));
            
            // ยอดค้างจากเดือนก่อน (ถ้ามี)
            int previousBalance = invoice.getPreviousBalance() != null ? invoice.getPreviousBalance() : 0;
            if (previousBalance > 0) {
                expenseTable.addCell(PdfStyleService.createDataCell("Outstanding Balance from Previous Month", normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell("1 item", normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell("-", normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(previousBalance), normalFont));
            }
            
            // ค่าเช่า
            int rentAmount = invoice.getRequestedRent() != null ? invoice.getRequestedRent() : 0;
            expenseTable.addCell(PdfStyleService.createDataCell("Room Rental", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell("1 month", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(rentAmount), normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(rentAmount), normalFont));
            
            // ค่าน้ำ
            int waterUnit = invoice.getRequestedWaterUnit() != null ? invoice.getRequestedWaterUnit() : 0;
            int waterAmount = invoice.getRequestedWater() != null ? invoice.getRequestedWater() : 0;
            int waterRate = (waterUnit > 0 && waterAmount > 0) ? (waterAmount / waterUnit) : 30;
            
            expenseTable.addCell(PdfStyleService.createDataCell("Water Supply", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(waterUnit + " units", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(String.valueOf(waterRate), normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(waterAmount), normalFont));
            
            // ค่าไฟ
            int elecUnit = invoice.getRequestedElectricityUnit() != null ? invoice.getRequestedElectricityUnit() : 0;
            int elecAmount = invoice.getRequestedElectricity() != null ? invoice.getRequestedElectricity() : 0;
            int elecRate = (elecUnit > 0 && elecAmount > 0) ? (elecAmount / elecUnit) : 8;
            
            expenseTable.addCell(PdfStyleService.createDataCell("Electricity", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(elecUnit + " units", normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(String.valueOf(elecRate), normalFont));
            expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(elecAmount), normalFont));
            
            // ค่าปรับ (ถ้ามี)
            int penaltyAmount = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
            if (penaltyAmount > 0) {
                expenseTable.addCell(PdfStyleService.createDataCell("Late Payment Penalty", normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell("1 item", normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(penaltyAmount), normalFont));
                expenseTable.addCell(PdfStyleService.createDataCell(PdfStyleService.formatMoney(penaltyAmount), normalFont));
            }
            
            document.add(expenseTable);
            
            // ===== สรุปยอดเงิน =====
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setWidths(new float[]{2, 1});
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingAfter(20);
            
            int subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 0;
            int previousBalanceAmount = invoice.getPreviousBalance() != null ? invoice.getPreviousBalance() : 0;
            int penaltyTotalAmount = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
            int netAmount = invoice.getNetAmount() != null ? invoice.getNetAmount() : (subTotal + previousBalanceAmount + penaltyTotalAmount);
            
            // แสดงยอดค้างจากเดือนก่อน (ถ้ามี)
            if (previousBalanceAmount > 0) {
                summaryTable.addCell(PdfStyleService.createSummaryLabelCell("Outstanding from Previous Month:", labelFont));
                summaryTable.addCell(PdfStyleService.createSummaryValueCell(PdfStyleService.formatMoney(previousBalanceAmount) + " THB", normalFont));
            }
            
            summaryTable.addCell(PdfStyleService.createSummaryLabelCell("Current Month Charges:", labelFont));
            summaryTable.addCell(PdfStyleService.createSummaryValueCell(PdfStyleService.formatMoney(subTotal) + " THB", normalFont));
            
            if (penaltyTotalAmount > 0) {
                summaryTable.addCell(PdfStyleService.createSummaryLabelCell("Late Payment Penalty:", labelFont));
                summaryTable.addCell(PdfStyleService.createSummaryValueCell(PdfStyleService.formatMoney(penaltyTotalAmount) + " THB", normalFont));
            }
            
            // เส้นแบ่ง
            PdfPCell lineCell1 = new PdfPCell(new Phrase("", normalFont));
            lineCell1.setBorder(Rectangle.TOP);
            lineCell1.setFixedHeight(10);
            summaryTable.addCell(lineCell1);
            PdfPCell lineCell2 = new PdfPCell(new Phrase("", normalFont));
            lineCell2.setBorder(Rectangle.TOP);
            lineCell2.setFixedHeight(10);
            summaryTable.addCell(lineCell2);
            
            summaryTable.addCell(PdfStyleService.createSummaryLabelCell("Total Amount:", titleFont));
            summaryTable.addCell(PdfStyleService.createSummaryValueCell(PdfStyleService.formatMoney(netAmount) + " THB", titleFont));
            
            document.add(summaryTable);
            
            // ===== สถานะการชำระเงิน =====
            Paragraph statusHeader = new Paragraph("Payment Status", headerFont);
            statusHeader.setSpacingAfter(10);
            document.add(statusHeader);
            
            PdfPTable statusTable = new PdfPTable(1);
            statusTable.setWidthPercentage(100);
            statusTable.setSpacingAfter(20);
            
            String statusText = "";
            int status = invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus() : 0;
            
            switch (status) {
                case 0:
                    statusText = "Status: Unpaid";
                    break;
                case 1:
                    statusText = "Status: Paid";
                    if (invoice.getPayDate() != null) {
                        statusText += "\nPayment Date: " + invoice.getPayDate().toLocalDate();
                    }
                    break;
                case 2:
                    statusText = "Status: Cancelled";
                    break;
                default:
                    statusText = "Status: Unknown";
                    break;
            }
            
            PdfPCell statusCell = PdfStyleService.createStatusCell(statusText, status, labelFont);
            statusTable.addCell(statusCell);
            document.add(statusTable);
            
            // ===== หมายเหตุ =====
            if (status == 0) { // ยังไม่ชำระ
                Paragraph noteHeader = new Paragraph("Notes", headerFont);
                noteHeader.setSpacingAfter(5);
                document.add(noteHeader);
                
                Paragraph note = new Paragraph();
                note.add(new Phrase("• Please pay by the due date specified above\n", normalFont));
                note.add(new Phrase("• Late payment penalty: 10% of rental amount\n", normalFont));
                note.add(new Phrase("• For bank transfers, please provide payment slip\n", normalFont));
                note.add(new Phrase("• Contact: Phone 02-123-4567\n", normalFont));
                note.setSpacingAfter(20);
                document.add(note);
                
                // ===== ข้อมูลการชำระเงิน =====
                Paragraph paymentHeader = new Paragraph("Payment Information / ข้อมูลการชำระเงิน", headerFont);
                paymentHeader.setSpacingAfter(10);
                document.add(paymentHeader);
                
                // ตารางข้อมูลธนาคาร
                PdfPTable paymentTable = new PdfPTable(2);
                paymentTable.setWidthPercentage(100);
                paymentTable.setWidths(new float[]{1, 1});
                paymentTable.setSpacingAfter(15);
                
                // ข้อมูลธนาคาร
                PdfPCell bankInfoCell = new PdfPCell();
                bankInfoCell.setBorder(Rectangle.BOX);
                bankInfoCell.setPadding(10);
                bankInfoCell.setBackgroundColor(PdfStyleService.LIGHT_GRAY);
                
                bankInfoCell.addElement(new Paragraph("Bangkok Bank", labelFont));
                bankInfoCell.addElement(new Paragraph("Account Name: OrganicNow Property Management", normalFont));
                bankInfoCell.addElement(new Paragraph("Account Number: 123-4-56789-0", normalFont));
                bankInfoCell.addElement(new Paragraph("Branch: Central Plaza Branch", normalFont));
                bankInfoCell.addElement(new Paragraph("SWIFT Code: BKKBTHBK", normalFont));
                bankInfoCell.addElement(new Paragraph("PromptPay ID: 0123456789", normalFont));
                
                paymentTable.addCell(bankInfoCell);
                
                // QR Code สำหรับชำระเงิน (แสดงข้อความใน table)
                PdfPCell qrCodeCell = new PdfPCell();
                qrCodeCell.setBorder(Rectangle.BOX);
                qrCodeCell.setPadding(10);
                qrCodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qrCodeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                qrCodeCell.setMinimumHeight(120);
                
                qrCodeCell.addElement(new Paragraph("QR Code สำหรับชำระเงิน", labelFont));
                qrCodeCell.addElement(new Paragraph("Scan to Pay", normalFont));
                qrCodeCell.addElement(new Paragraph("จำนวนเงิน: " + PdfStyleService.formatMoney(netAmount) + " บาท", normalFont));
                qrCodeCell.addElement(new Paragraph("รหัสอ้างอิง: INV-" + String.format("%06d", invoice.getId()), smallFont));
                qrCodeCell.addElement(new Paragraph("QR Code แสดงด้านล่าง", smallFont));
                
                paymentTable.addCell(qrCodeCell);
                document.add(paymentTable);
                
                // เพิ่ม QR Code จริงหลัง payment table
                try {
                    Paragraph qrHeader = new Paragraph("QR Code สำหรับชำระเงิน", headerFont);
                    qrHeader.setAlignment(Element.ALIGN_CENTER);
                    qrHeader.setSpacingAfter(10);
                    document.add(qrHeader);
                    
                    // สร้าง QR Code placeholder ที่สวยงาม
                    PdfPTable qrTable = new PdfPTable(1);
                    qrTable.setWidthPercentage(40);
                    qrTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qrTable.setSpacingAfter(10);
                    
                    PdfPCell qrCell = new PdfPCell();
                    qrCell.setBorder(Rectangle.BOX);
                    qrCell.setPadding(15);
                    qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qrCell.setMinimumHeight(120);
                    qrCell.setBackgroundColor(new Color(250, 250, 250));
                    
                    // ASCII QR Code pattern
                    qrCell.addElement(new Paragraph("█ █ █ █ █ █ █", new Font(Font.COURIER, 10, Font.NORMAL)));
                    qrCell.addElement(new Paragraph("█       █   █", new Font(Font.COURIER, 10, Font.NORMAL)));
                    qrCell.addElement(new Paragraph("█ █ █ █ █ █ █", new Font(Font.COURIER, 10, Font.NORMAL)));
                    qrCell.addElement(new Paragraph("█   █   █   █", new Font(Font.COURIER, 10, Font.NORMAL)));
                    qrCell.addElement(new Paragraph("█ █ █ █ █ █ █", new Font(Font.COURIER, 10, Font.NORMAL)));
                    qrCell.addElement(new Paragraph(" ", normalFont));
                    qrCell.addElement(new Paragraph("Scan to Pay", normalFont));
                    
                    qrTable.addCell(qrCell);
                    document.add(qrTable);
                    
                    // ข้อมูลการชำระเงิน
                    double amountValue = (double) netAmount;
                    Paragraph qrInfo = new Paragraph("จำนวนเงิน: " + PdfStyleService.formatMoney(netAmount) + " บาท", normalFont);
                    qrInfo.setAlignment(Element.ALIGN_CENTER);
                    qrInfo.setSpacingAfter(5);
                    document.add(qrInfo);
                    
                    Paragraph qrRef = new Paragraph("รหัสอ้างอิง: INV-" + String.format("%06d", invoice.getId()), normalFont);
                    qrRef.setAlignment(Element.ALIGN_CENTER);
                    qrRef.setSpacingAfter(5);
                    document.add(qrRef);
                    
                    Paragraph promptPayInfo = new Paragraph("PromptPay ID: 0123456789", normalFont);
                    promptPayInfo.setAlignment(Element.ALIGN_CENTER);
                    promptPayInfo.setSpacingAfter(10);
                    document.add(promptPayInfo);
                    
                    Paragraph urlInfo = new Paragraph("URL: https://promptpay.io/0123456789/" + String.format("%.2f", amountValue), smallFont);
                    urlInfo.setAlignment(Element.ALIGN_CENTER);
                    urlInfo.setSpacingAfter(20);
                    document.add(urlInfo);
                    
                } catch (Exception e) {
                    System.err.println("Error adding QR Code to PDF: " + e.getMessage());
                    e.printStackTrace();
                    
                    // ถ้าสร้าง QR Code ไม่ได้ ให้ใช้ placeholder แทน
                    PdfPTable qrPlaceholderTable = new PdfPTable(1);
                    qrPlaceholderTable.setWidthPercentage(50);
                    qrPlaceholderTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                    qrPlaceholderTable.setSpacingAfter(10);
                    
                    PdfPCell placeholderCell = new PdfPCell();
                    placeholderCell.setBorder(Rectangle.BOX);
                    placeholderCell.setPadding(20);
                    placeholderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    placeholderCell.setMinimumHeight(80);
                    placeholderCell.setBackgroundColor(PdfStyleService.LIGHT_GRAY);
                    
                    placeholderCell.addElement(new Paragraph("[ QR CODE ]", titleFont));
                    placeholderCell.addElement(new Paragraph("Scan with mobile app", normalFont));
                    qrPlaceholderTable.addCell(placeholderCell);
                    
                    document.add(qrPlaceholderTable);
                    
                    Paragraph qrInfo = new Paragraph("Scan to Pay - จำนวนเงิน: " + PdfStyleService.formatMoney(netAmount) + " บาท", normalFont);
                    qrInfo.setAlignment(Element.ALIGN_CENTER);
                    qrInfo.setSpacingAfter(5);
                    document.add(qrInfo);
                    
                    Paragraph qrRef = new Paragraph("รหัสอ้างอิง: INV-" + String.format("%06d", invoice.getId()), normalFont);
                    qrRef.setAlignment(Element.ALIGN_CENTER);
                    qrRef.setSpacingAfter(10);
                    document.add(qrRef);
                    
                    Paragraph promptPayInfo = new Paragraph("PromptPay ID: 0123456789", normalFont);
                    promptPayInfo.setAlignment(Element.ALIGN_CENTER);
                    promptPayInfo.setSpacingAfter(20);
                    document.add(promptPayInfo);
                }
                
                // คำแนะนำการชำระเงิน
                Paragraph paymentInstructions = new Paragraph("Payment Instructions", labelFont);
                paymentInstructions.setSpacingAfter(5);
                document.add(paymentInstructions);
                
                Paragraph instructions = new Paragraph();
                instructions.add(new Phrase("1. Transfer the specified amount: " + PdfStyleService.formatMoney(netAmount) + " THB\n", normalFont));
                instructions.add(new Phrase("2. Reference Number: INV-" + String.format("%06d", invoice.getId()) + "\n", normalFont));
                instructions.add(new Phrase("3. Save transfer receipt and send to staff\n", normalFont));
                instructions.add(new Phrase("4. Payment verification within 1-2 business days\n", normalFont));
                instructions.setSpacingAfter(20);
                document.add(instructions);
            }
            
            // ===== Footer =====
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(10);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Phrase("Thank you for using ORGANIC NOW Apartment Services\n", normalFont));
            footer.add(new Phrase("Contact us: Tel 02-123-4567 or LINE: @organicnow", smallFont));
            document.add(footer);
            
            document.close();
            
            System.out.println(">>> [InvoiceService] PDF generated successfully, size: " + baos.size() + " bytes");
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println(">>> [InvoiceService] Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }
}