package com.organicnow.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import com.organicnow.backend.dto.UtilityUsageDto;
import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.model.PackagePlan;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.InvoiceRepository;
import com.organicnow.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
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

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              ContractRepository contractRepository,
                              RoomRepository roomRepository) {
        this.invoiceRepository = invoiceRepository;
        this.contractRepository = contractRepository;
        this.roomRepository = roomRepository;
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

        // ✅ เก็บข้อมูลจาก request สำหรับการแสดงผล
        inv.setPackageId(request.getPackageId());
        
        // แปลง floor จาก String เป็น Integer
        Integer floorNum = null;
        try {
            if (request.getFloor() != null && !request.getFloor().trim().isEmpty()) {
                floorNum = Integer.parseInt(request.getFloor().trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid floor format: " + request.getFloor());
        }
        inv.setRequestedFloor(floorNum);
        inv.setRequestedRoom(request.getRoom());
        inv.setRequestedRent(rent);
        
        // เก็บค่าน้ำและค่าไฟจาก request
        inv.setRequestedWater(waterAmount);
        inv.setRequestedWaterUnit(waterUnit);
        inv.setRequestedElectricity(electricityAmount);
        inv.setRequestedElectricityUnit(electricityUnit);
        
        System.out.println("💾 Saving to DB - Water: " + waterAmount + " (" + waterUnit + " units), Electricity: " + electricityAmount + " (" + electricityUnit + " units)");

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

    @Override
    @Transactional
    public InvoiceDto updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice inv = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));

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
    public void deleteInvoice(Long id) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
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
                .netAmount(invoice.getSubTotal() + invoice.getPenaltyTotal()) // ✅ คำนวณ real-time
                .penaltyAppliedAt(invoice.getPenaltyAppliedAt())
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
                .waterUnit(invoice.getRequestedWaterUnit() != null && invoice.getRequestedWaterUnit() > 0 
                        ? invoice.getRequestedWaterUnit() 
                        : (invoice.getSubTotal() != null ? Math.round((invoice.getSubTotal() * 0.2f) / 30) : 0))
                .electricity(invoice.getRequestedElectricity() != null && invoice.getRequestedElectricity() > 0 
                        ? invoice.getRequestedElectricity() 
                        : (invoice.getSubTotal() != null ? Math.round(invoice.getSubTotal() * 0.8f) : 0))
                .electricityUnit(invoice.getRequestedElectricityUnit() != null && invoice.getRequestedElectricityUnit() > 0 
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
        
        // FIX 2: ดึงข้อมูลจาก Invoice entity (requested... fields) และ contract ที่เกี่ยวข้อง
        // ข้อมูล Tenant ยังคงดึงจาก contract ที่ผูกกับ invoice
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
            
            // กำหนด fonts - ใช้ default fonts แทนไฟล์ฟอนต์ไทยที่ไม่มี
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            
            // ===== หัวกระดาษ =====
            // ชื่อบริษัท
            Paragraph companyTitle = new Paragraph("ORGANIC NOW", titleFont);
            companyTitle.setAlignment(Element.ALIGN_CENTER);
            companyTitle.setSpacingAfter(5);
            document.add(companyTitle);
            
            Paragraph companySubtitle = new Paragraph("หอพักออร์แกนิคเนาว์", headerFont);
            companySubtitle.setAlignment(Element.ALIGN_CENTER);
            companySubtitle.setSpacingAfter(20);
            document.add(companySubtitle);
            
            // ===== ข้อมูลใบแจ้งหนี้ =====
            PdfPTable invoiceHeaderTable = new PdfPTable(2);
            invoiceHeaderTable.setWidthPercentage(100);
            invoiceHeaderTable.setWidths(new float[]{1, 1});
            invoiceHeaderTable.setSpacingAfter(20);
            
            // ข้อมูลบริษัท
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("ที่อยู่:", labelFont));
            companyCell.addElement(new Paragraph("123/45 ถนนราชดำเนิน", normalFont));
            companyCell.addElement(new Paragraph("กรุงเทพมหานคร 10200", normalFont));
            companyCell.addElement(new Paragraph("โทรศัพท์: 02-123-4567", normalFont));
            invoiceHeaderTable.addCell(companyCell);
            
            // ข้อมูลใบแจ้งหนี้
            PdfPCell invoiceInfoCell = new PdfPCell();
            invoiceInfoCell.setBorder(Rectangle.BOX);
            invoiceInfoCell.setPadding(10);
            invoiceInfoCell.setBackgroundColor(new Color(245, 245, 245));
            
            invoiceInfoCell.addElement(new Paragraph("ใบแจ้งหนี้เลขที่", labelFont));
            invoiceInfoCell.addElement(new Paragraph("INV-" + String.format("%06d", invoice.getId()), titleFont));
            invoiceInfoCell.addElement(new Paragraph(" ", normalFont)); // spacer
            invoiceInfoCell.addElement(new Paragraph("วันที่ออกบิล: " + invoice.getCreateDate().toLocalDate(), normalFont));
            invoiceInfoCell.addElement(new Paragraph("วันครบกำหนด: " + (invoice.getDueDate() != null ? 
                    invoice.getDueDate().toLocalDate() : "ไม่ระบุ"), normalFont));
            
            invoiceHeaderTable.addCell(invoiceInfoCell);
            document.add(invoiceHeaderTable);
            
            // ===== ข้อมูลผู้เช่า =====
            Paragraph customerHeader = new Paragraph("ข้อมูลลูกค้า", headerFont);
            customerHeader.setSpacingAfter(10);
            document.add(customerHeader);
            
            PdfPTable customerTable = new PdfPTable(2);
            customerTable.setWidthPercentage(100);
            customerTable.setWidths(new float[]{1, 2});
            customerTable.setSpacingAfter(20);
            
            customerTable.addCell(makeStyledLabelCell("ชื่อ-นามสกุล:", labelFont));
            customerTable.addCell(makeStyledValueCell((tenant.getFirstName() != null ? tenant.getFirstName() : "") + 
                    " " + (tenant.getLastName() != null ? tenant.getLastName() : ""), normalFont));
            
            customerTable.addCell(makeStyledLabelCell("เลขประจำตัวประชาชน:", labelFont));
            customerTable.addCell(makeStyledValueCell(tenant.getNationalId() != null ? tenant.getNationalId() : "ไม่ระบุ", normalFont));
            
            customerTable.addCell(makeStyledLabelCell("เบอร์โทรศัพท์:", labelFont));
            customerTable.addCell(makeStyledValueCell(tenant.getPhoneNumber() != null ? tenant.getPhoneNumber() : "ไม่ระบุ", normalFont));
            
            // FIX 2: ใช้ข้อมูลห้องจากตัว invoice (roomDisplay)
            customerTable.addCell(makeStyledLabelCell("หมายเลขห้อง:", labelFont));
            customerTable.addCell(makeStyledValueCell(roomDisplay, normalFont));
            
            // FIX 2: ใช้ข้อมูลแพ็คเกจจากตัวแปร (packageName)
            customerTable.addCell(makeStyledLabelCell("แพ็คเกจ:", labelFont));
            customerTable.addCell(makeStyledValueCell(packageName, normalFont));
            
            document.add(customerTable);
            
            // ===== รายการค่าใช้จ่าย =====
            Paragraph expenseHeader = new Paragraph("รายการค่าใช้จ่าย", headerFont);
            expenseHeader.setSpacingAfter(10);
            document.add(expenseHeader);
            
            PdfPTable expenseTable = new PdfPTable(4);
            expenseTable.setWidthPercentage(100);
            expenseTable.setWidths(new float[]{3f, 1.5f, 1.5f, 2f});
            expenseTable.setSpacingAfter(15);
            
            // Header ของตาราง
            expenseTable.addCell(makeStyledHeaderCell("รายการ", labelFont));
            expenseTable.addCell(makeStyledHeaderCell("จำนวน/หน่วย", labelFont));
            expenseTable.addCell(makeStyledHeaderCell("อัตรา (บาท)", labelFont));
            expenseTable.addCell(makeStyledHeaderCell("จำนวนเงิน (บาท)", labelFont));
            
            // ค่าเช่า
            // (โค้ดส่วนนี้ถูกต้องอยู่แล้ว ใช้ requestedRent)
            int rentAmount = invoice.getRequestedRent() != null ? invoice.getRequestedRent() : 0;
            expenseTable.addCell(makeStyledDataCell("ค่าเช่าห้องพัก", normalFont));
            expenseTable.addCell(makeStyledDataCell("1 เดือน", normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%,d", rentAmount), normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%,d", rentAmount), normalFont));
            
            // ค่าน้ำ
            // (โค้ดส่วนนี้ถูกต้องอยู่แล้ว ใช้ requestedWater/Unit)
            int waterUnit = invoice.getRequestedWaterUnit() != null ? invoice.getRequestedWaterUnit() : 0;
            int waterAmount = invoice.getRequestedWater() != null ? invoice.getRequestedWater() : 0;
            // FIX 2b: เปลี่ยน default rate ให้ตรงกับ createInvoice (30)
            int waterRate = (waterUnit > 0 && waterAmount > 0) ? (waterAmount / waterUnit) : 30;
            
            expenseTable.addCell(makeStyledDataCell("ค่าน้ำประปา", normalFont));
            expenseTable.addCell(makeStyledDataCell(waterUnit + " หน่วย", normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%d", waterRate), normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%,d", waterAmount), normalFont));
            
            // ค่าไฟ
            // (โค้ดส่วนนี้ถูกต้องอยู่แล้ว ใช้ requestedElectricity/Unit)
            int elecUnit = invoice.getRequestedElectricityUnit() != null ? invoice.getRequestedElectricityUnit() : 0;
            int elecAmount = invoice.getRequestedElectricity() != null ? invoice.getRequestedElectricity() : 0;
            int elecRate = (elecUnit > 0 && elecAmount > 0) ? (elecAmount / elecUnit) : 8;
            
            expenseTable.addCell(makeStyledDataCell("ค่าไฟฟ้า", normalFont));
            expenseTable.addCell(makeStyledDataCell(elecUnit + " หน่วย", normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%d", elecRate), normalFont));
            expenseTable.addCell(makeStyledDataCell(String.format("%,d", elecAmount), normalFont));
            
            // ค่าปรับ (ถ้ามี)
            int penaltyAmount = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
            if (penaltyAmount > 0) {
                expenseTable.addCell(makeStyledDataCell("ค่าปรับล่าช้า", normalFont));
                expenseTable.addCell(makeStyledDataCell("1 รายการ", normalFont));
                expenseTable.addCell(makeStyledDataCell(String.format("%,d", penaltyAmount), normalFont));
                expenseTable.addCell(makeStyledDataCell(String.format("%,d", penaltyAmount), normalFont));
            }
            
            document.add(expenseTable);
            
            // ===== สรุปยอดเงิน =====
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setWidths(new float[]{2, 1});
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingAfter(20);
            
            int subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 0;
            // FIX: ใช้ netAmount จาก invoice entity ที่คำนวณไว้แล้ว
            int netAmount = invoice.getNetAmount() != null ? invoice.getNetAmount() : (subTotal + penaltyAmount);
            
            summaryTable.addCell(makeStyledSummaryLabelCell("ยอดรวมค่าบริการ:", labelFont));
            summaryTable.addCell(makeStyledSummaryValueCell(String.format("%,d บาท", subTotal), normalFont));
            
            if (penaltyAmount > 0) {
                summaryTable.addCell(makeStyledSummaryLabelCell("ค่าปรับล่าช้า:", labelFont));
                summaryTable.addCell(makeStyledSummaryValueCell(String.format("%,d บาท", penaltyAmount), normalFont));
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
            
            summaryTable.addCell(makeStyledSummaryLabelCell("ยอดรวมสุทธิ:", titleFont));
            summaryTable.addCell(makeStyledSummaryValueCell(String.format("%,d บาท", netAmount), titleFont));
            
            document.add(summaryTable);
            
            // ===== สถานะการชำระเงิน =====
            Paragraph statusHeader = new Paragraph("สถานะการชำระเงิน", headerFont);
            statusHeader.setSpacingAfter(10);
            document.add(statusHeader);
            
            PdfPTable statusTable = new PdfPTable(1);
            statusTable.setWidthPercentage(100);
            statusTable.setSpacingAfter(20);
            
            PdfPCell statusCell = new PdfPCell();
            statusCell.setBorder(Rectangle.BOX);
            statusCell.setPadding(15);
            
            String statusText = "";
            if (invoice.getInvoiceStatus() != null) {
                switch (invoice.getInvoiceStatus()) {
                    case 0:
                        statusText = "สถานะ: ยังไม่ชำระเงิน";
                        statusCell.setBackgroundColor(new Color(255, 235, 235)); // Light red
                        break;
                    case 1:
                        statusText = "สถานะ: ชำระเงินเรียบร้อยแล้ว";
                        statusCell.setBackgroundColor(new Color(235, 255, 235)); // Light green
                        if (invoice.getPayDate() != null) {
                            statusText += "\nวันที่ชำระ: " + invoice.getPayDate().toLocalDate();
                        }
                        break;
                    case 2:
                        statusText = "สถานะ: ยกเลิกแล้ว";
                        statusCell.setBackgroundColor(new Color(245, 245, 245)); // Light gray
                        break;
                    default:
                        statusText = "สถานะ: ไม่ระบุ";
                        break;
                }
            } else {
                statusText = "สถานะ: ไม่ระบุ";
            }
            
            statusCell.addElement(new Paragraph(statusText, labelFont));
            statusTable.addCell(statusCell);
            document.add(statusTable);
            
            // ===== หมายเหตุ =====
            if (invoice.getInvoiceStatus() == null || invoice.getInvoiceStatus() == 0) { // ยังไม่ชำระ
                Paragraph noteHeader = new Paragraph("หมายเหตุ", headerFont);
                noteHeader.setSpacingAfter(5);
                document.add(noteHeader);
                
                Paragraph note = new Paragraph();
                note.add(new Phrase("• กรุณาชำระเงินภายในวันครบกำหนดที่ระบุข้างต้น\n", normalFont));
                // FIX 3: เปลี่ยนข้อความค่าปรับให้ตรงกับ logic (10% ของค่าเช่า)
                note.add(new Phrase("• หากชำระเงินล่าช้าจะมีค่าปรับ 10% ของยอดค่าเช่า\n", normalFont));
                note.add(new Phrase("• สำหรับการโอนเงิน กรุณาแจ้งสลิปการชำระเงิน\n", normalFont));
                note.add(new Phrase("• ติดต่อสอบถาม: โทร 02-123-4567\n", normalFont));
                note.setSpacingAfter(20);
                document.add(note);
            }
            
            // ===== Footer =====
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(10);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Phrase("ขอบคุณที่ใช้บริการหอพัก ORGANIC NOW\n", normalFont));
            footer.add(new Phrase("สอบถามเพิ่มเติม: โทร 02-123-4567 หรือ LINE: @organicnow", smallFont));
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
    
    private PdfPCell makeCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
    
    // ===== Helper Methods for PDF Cell Styling =====
    
    private PdfPCell makeStyledLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
    
    private PdfPCell makeStyledValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
    
    private PdfPCell makeStyledHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(10);
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    
    private PdfPCell makeStyledDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    
    private PdfPCell makeStyledSummaryLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
    
    private PdfPCell makeStyledSummaryValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
    
    // Legacy methods (kept for compatibility)
    private PdfPCell makeLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setBackgroundColor(new Color(240, 240, 240)); // Light gray
        return cell;
    }
    
    private PdfPCell makeValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
    
    private PdfPCell makeHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setBackgroundColor(new Color(200, 200, 200)); // Dark gray
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    
    private PdfPCell makeDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    
    private PdfPCell makeTotalLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
    
    private PdfPCell makeTotalValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}