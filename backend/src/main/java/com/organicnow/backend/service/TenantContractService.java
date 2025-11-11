package com.organicnow.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import com.lowagie.text.pdf.draw.LineSeparator;

@Service
public class TenantContractService {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final PackagePlanRepository packagePlanRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;

    public TenantContractService(TenantRepository tenantRepository,
                                 RoomRepository roomRepository,
                                 PackagePlanRepository packagePlanRepository,
                                 ContractRepository contractRepository,
                                 InvoiceRepository invoiceRepository) {
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.packagePlanRepository = packagePlanRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // ➕ CREATE
    @Transactional
    public TenantDto create(CreateTenantContractRequest req) {
        Tenant tenant = tenantRepository.findByNationalId(req.getNationalId()).orElse(null);

        if (tenant != null) {
            boolean hasActive = contractRepository
                    .existsByTenant_IdAndStatusAndEndDateAfter(tenant.getId(), 1, LocalDateTime.now());
            if (hasActive) {
                throw new RuntimeException("tenant_already_has_active_contract");
            }
        } else {
            tenant = Tenant.builder()
                    .firstName(req.getFirstName())
                    .lastName(req.getLastName())
                    .email(req.getEmail())
                    .phoneNumber(req.getPhoneNumber())
                    .nationalId(req.getNationalId())
                    .build();
            tenant = tenantRepository.saveAndFlush(tenant);
        }

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found: " + req.getRoomId()));
        PackagePlan plan = packagePlanRepository.findById(req.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package plan not found: " + req.getPackageId()));

        Contract contract = Contract.builder()
                .tenant(tenant)
                .room(room)
                .packagePlan(plan)
                .signDate(req.getSignDate() != null ? req.getSignDate() : LocalDateTime.now())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(1)
                .deposit(req.getDeposit())
                .rentAmountSnapshot(req.getRentAmountSnapshot())
                .build();
        contractRepository.save(contract);

        return TenantDto.builder()
                .contractId(contract.getId())
                .firstName(tenant.getFirstName())
                .lastName(tenant.getLastName())
                .email(tenant.getEmail())
                .floor(room.getRoomFloor())
                .room(room.getRoomNumber())
                .packageId(plan.getId())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .phoneNumber(tenant.getPhoneNumber())
                .build();
    }

    // ✏️ UPDATE
    @Transactional
    public TenantDto update(Long contractId, UpdateTenantContractRequest req) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        Tenant tenant = contract.getTenant();
        if (req.getFirstName() != null) tenant.setFirstName(req.getFirstName());
        if (req.getLastName() != null) tenant.setLastName(req.getLastName());
        if (req.getEmail() != null) tenant.setEmail(req.getEmail());
        if (req.getPhoneNumber() != null) tenant.setPhoneNumber(req.getPhoneNumber());
        if (req.getNationalId() != null) tenant.setNationalId(req.getNationalId());
        tenantRepository.save(tenant);

        if (req.getRoomId() != null) {
            Room room = roomRepository.findById(req.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found: " + req.getRoomId()));
            contract.setRoom(room);
        }
        if (req.getPackageId() != null) {
            PackagePlan plan = packagePlanRepository.findById(req.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package plan not found: " + req.getPackageId()));
            contract.setPackagePlan(plan);
        }

        if (req.getSignDate() != null) contract.setSignDate(req.getSignDate());
        if (req.getStartDate() != null) contract.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) contract.setEndDate(req.getEndDate());
        if (req.getStatus() != null) contract.setStatus(req.getStatus());
        if (req.getDeposit() != null) contract.setDeposit(req.getDeposit());
        if (req.getRentAmountSnapshot() != null) contract.setRentAmountSnapshot(req.getRentAmountSnapshot());

        Contract saved = contractRepository.save(contract);

        return TenantDto.builder()
                .contractId(saved.getId())
                .firstName(saved.getTenant().getFirstName())
                .lastName(saved.getTenant().getLastName())
                .email(saved.getTenant().getEmail())
                .floor(saved.getRoom().getRoomFloor())
                .room(saved.getRoom().getRoomNumber())
                .packageId(saved.getPackagePlan().getId())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .phoneNumber(saved.getTenant().getPhoneNumber())
                .build();
    }

    // ❌ DELETE (ลบ invoice ทั้งหมด + ลบ contract)
    @Transactional
    public void delete(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        // ลบอินวอยซ์ทั้งหมดของสัญญานี้ก่อน (เพื่อกัน FK)
        List<Invoice> invoices = invoiceRepository.findByContact_IdOrderByIdDesc(contractId);
        if (!invoices.isEmpty()) {
            invoiceRepository.deleteAll(invoices);
        }
        
        
            
        // จากนั้นลบ contract
        contractRepository.deleteById(contractId);
    }

    // 📄 GET DETAIL
    @Transactional(readOnly = true)
    public TenantDetailDto getDetail(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        Tenant tenant = contract.getTenant();
        Room room = contract.getRoom();
        PackagePlan plan = contract.getPackagePlan();

        List<Invoice> invoices = invoiceRepository.findByContact_IdOrderByIdDesc(contractId);

        List<TenantDetailDto.InvoiceDto> invoiceDtos = invoices.stream()
                .map(inv -> TenantDetailDto.InvoiceDto.builder()
                        .invoiceId(inv.getId())
                        .createDate(inv.getCreateDate())
                        .dueDate(inv.getDueDate())
                        .invoiceStatus(inv.getInvoiceStatus())
                        .netAmount(inv.getNetAmount())
                        .payDate(inv.getPayDate())
                        .payMethod(inv.getPayMethod())
                        .penaltyTotal(inv.getPenaltyTotal())
                        .subTotal(inv.getSubTotal())
                        .build()
                ).toList();

        return TenantDetailDto.builder()
                .contractId(contract.getId())
                .firstName(tenant.getFirstName())
                .lastName(tenant.getLastName())
                .email(tenant.getEmail())
                .phoneNumber(tenant.getPhoneNumber())
                .nationalId(tenant.getNationalId())
                .floor(room.getRoomFloor())
                .room(room.getRoomNumber())
                .packageName(plan.getContractType().getName())
                .packagePrice(plan.getPrice())
                .signDate(contract.getSignDate())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .status(contract.getStatus())
                .deposit(contract.getDeposit())
                .rentAmountSnapshot(contract.getRentAmountSnapshot())
                .invoices(invoiceDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] generateContractPdf(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        Tenant tenant = contract.getTenant();
        Room room = contract.getRoom();
        PackagePlan plan = contract.getPackagePlan();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new PageFooterEvent());
            document.open();

            // ====== Fonts ======
            String regularFontPath = this.getClass().getClassLoader().getResource("fonts/Sarabun-Regular.ttf").getPath();
            String boldFontPath = this.getClass().getClassLoader().getResource("fonts/Sarabun-Bold.ttf").getPath();
            BaseFont bfRegular = BaseFont.createFont(regularFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(boldFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font titleFont = new Font(bfBold, 18, Font.BOLD, new Color(40, 40, 40));
            Font subTitleFont = new Font(bfRegular, 13, Font.NORMAL, new Color(80, 80, 80));
            Font labelFont = new Font(bfBold, 12, Font.BOLD, new Color(40, 40, 40));
            Font valueFont = new Font(bfRegular, 12, Font.NORMAL, new Color(30, 30, 30));
            Font tableHeaderFont = new Font(bfBold, 11, Font.NORMAL, new Color(20, 20, 20));
            Font tableFont = new Font(bfRegular, 11, Font.NORMAL, new Color(30, 30, 30));

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            NumberFormat moneyFmt = NumberFormat.getNumberInstance(Locale.US);
            moneyFmt.setMinimumFractionDigits(2);

            // ====== PAGE 1 ======
            Paragraph title = new Paragraph("RENTAL CONTRACT AGREEMENT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("สัญญาเช่าหอพัก", subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(6);
            document.add(subtitle);

            LineSeparator line = new LineSeparator(0.7f, 100, new Color(120, 120, 120), Element.ALIGN_CENTER, -2);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Contract ID ขวาบน
            Paragraph contractIdP = new Paragraph("Contract ID: " + contract.getId(),
                    new Font(bfRegular, 11, Font.NORMAL, new Color(90, 90, 90)));
            contractIdP.setAlignment(Element.ALIGN_RIGHT);
            document.add(contractIdP);
            document.add(Chunk.NEWLINE);

            // Tenant info
            document.add(new Paragraph("Full Name: " + tenant.getFirstName() + " " + tenant.getLastName(), valueFont));
            document.add(new Paragraph("National ID: " + nvl(tenant.getNationalId()), valueFont));
            document.add(new Paragraph("Phone: " + nvl(tenant.getPhoneNumber()), valueFont));
            document.add(new Paragraph("Email: " + nvl(tenant.getEmail()), valueFont));
            document.add(Chunk.NEWLINE);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);

            // Table (Room info)
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 10, 10, 20, 15, 15, 20});

            Color pastelBlue = new Color(216, 239, 255);
            Color borderGray = new Color(200, 200, 200);

            String[] headers = {"Room", "Floor", "Size", "Package", "Rent", "Deposit", "End Date"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(pastelBlue);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                cell.setBorderColor(borderGray);
                table.addCell(cell);
            }

            String roomSize = switch (room.getRoomSize()) {
                case 0 -> "Studio";
                case 1 -> "Superior";
                case 2 -> "Deluxe";
                default -> "-";
            };

            String[] data = {
                    nvl(room.getRoomNumber()),
                    String.valueOf(room.getRoomFloor()),
                    roomSize,
                    plan.getContractType().getName(),
                    moneyFmt.format(contract.getRentAmountSnapshot()),
                    moneyFmt.format(contract.getDeposit()),
                    contract.getEndDate() != null ? contract.getEndDate().format(dateFmt) : "-"
            };
            for (String d : data) {
                PdfPCell cell = new PdfPCell(new Phrase(d, tableFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                cell.setBorderColor(borderGray);
                table.addCell(cell);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);

            // ====== PAGE 2 ======
            document.newPage();
            Paragraph termTitle = new Paragraph("TERMS AND CONDITIONS", titleFont);
            termTitle.setAlignment(Element.ALIGN_CENTER);
            termTitle.setSpacingAfter(5);
            document.add(termTitle);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            String[] terms = {
                    "1. The tenant agrees to pay the monthly rent on or before the due date as stated in this contract.\n   ผู้เช่าตกลงชำระค่าเช่าภายในวันที่กำหนด หากชำระล่าช้าจะถูกเรียกเก็บค่าปรับในอัตราที่ทางหอพักกำหนด.",
                    "2. The landlord agrees to maintain the property in good condition.\n   ผู้ให้เช่าจะดูแลรักษาทรัพย์สินให้อยู่ในสภาพพร้อมใช้งาน.",
                    "3. The security deposit will be refunded at the end of the contract provided there is no outstanding debt or damage beyond normal wear and tear.\n   เงินมัดจำจะคืนเมื่อครบกำหนดสัญญา หากไม่มีหนี้ค้างชำระหรือความเสียหายเกินสภาพการใช้งานปกติ.",
                    "4. If the tenant terminates the contract before the end date without prior notice, the deposit will not be refunded.\n   หากยกเลิกสัญญาก่อนครบกำหนด จะไม่ได้รับเงินมัดจำคืน.",
                    "5. Electricity and water bills will be calculated based on the rate set by the residence and must be paid monthly together with the rent.\n   ค่าไฟฟ้าและค่าน้ำจะคำนวณตามหน่วยที่หอพักกำหนด และต้องชำระพร้อมค่าเช่าในแต่ละเดือน.",
                    "6. The tenant shall not make any structural modifications to the room without prior consent.\n   ผู้เช่าห้ามปรับปรุงหรือเปลี่ยนแปลงโครงสร้างห้องโดยไม่ได้รับอนุญาต.",
                    "7. Before moving in and after moving out, both parties must inspect the room together.\n   ก่อนเข้าพักและหลังออกจากห้อง ผู้ให้เช่าและผู้เช่าต้องตรวจสอบสภาพห้องร่วมกัน.",
                    "8. Pets are not allowed unless specifically permitted by the landlord.\n   ห้ามนำสัตว์เลี้ยงเข้ามาในห้องพัก เว้นแต่ได้รับอนุญาตจากผู้ให้เช่า.",
                    "9. The tenant agrees to comply with all residence rules and regulations.\n   ผู้เช่าต้องปฏิบัติตามกฎระเบียบของหอพักอย่างเคร่งครัด.",
                    "10. This contract is renewable upon mutual agreement between both parties.\n   สัญญาฉบับนี้สามารถต่ออายุได้โดยความยินยอมร่วมกันของทั้งสองฝ่าย."
            };

            for (String t : terms) {
                Paragraph p = new Paragraph(t, valueFont);
                p.setSpacingAfter(8f);
                p.setLeading(16f);
                document.add(p);
            }

            // ====== PAGE 3: SIGNATURE ======
            document.newPage();

            Paragraph ackTitle = new Paragraph("ACKNOWLEDGEMENT & SIGNATURES", titleFont);
            ackTitle.setAlignment(Element.ALIGN_CENTER);
            ackTitle.setSpacingAfter(6);
            document.add(ackTitle);
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Paragraph ackBody = new Paragraph(
                    "Both parties confirm that they have reviewed, understood, and agreed to all clauses in this contract.\n" +
                            "คู่สัญญาทั้งสองฝ่ายได้อ่าน ทำความเข้าใจ และยอมรับข้อกำหนดทั้งหมดในสัญญานี้.",
                    valueFont);
            ackBody.setAlignment(Element.ALIGN_CENTER);
            ackBody.setSpacingAfter(40);
            ackBody.setLeading(16f);
            document.add(ackBody);

            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{50f, 50f});

            PdfPCell left = signatureBlock("Landlord Signature", "ลายเซ็นผู้ให้เช่า", valueFont);
            PdfPCell right = signatureBlock("Tenant Signature", "ลายเซ็นผู้เช่า", valueFont);
            signTable.addCell(left);
            signTable.addCell(right);

            document.add(signTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // -------------------- Helpers --------------------
    private static PdfPCell signatureBlock(String en, String th, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(20f);

        Paragraph line = new Paragraph("(__________________________)", font);
        line.setAlignment(Element.ALIGN_CENTER);

        Paragraph name = new Paragraph(en + " / " + th, font);
        name.setAlignment(Element.ALIGN_CENTER);

        Paragraph date = new Paragraph("Date: ______________", font);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingBefore(4);

        cell.addElement(line);
        cell.addElement(name);
        cell.addElement(date);
        return cell;
    }

    private static String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    // ✅ Footer (แสดงเลขหน้าอย่างเดียว)
    private static class PageFooterEvent extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 100, 100));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String text = String.valueOf(writer.getPageNumber());
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(text, footerFont),
                    document.right(), document.bottom() - 10, 0);
        }
    }
}