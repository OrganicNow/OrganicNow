package com.organicnow.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.organicnow.backend.dto.CreateMaintainRequest;
import com.organicnow.backend.dto.MaintainDto;
import com.organicnow.backend.dto.UpdateMaintainRequest;
import com.organicnow.backend.model.Maintain;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import com.organicnow.backend.repository.MaintainRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import com.organicnow.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintainServiceImpl implements MaintainService {

    private final MaintainRepository maintainRepository;
    private final RoomRepository roomRepository;
    private final RoomAssetRepository roomAssetRepository;

    @Override
    public List<MaintainDto> getAll() {
        return maintainRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public java.util.Optional<MaintainDto> getById(Long id) {
        return maintainRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional
    public MaintainDto create(CreateMaintainRequest req) {
        System.out.println("🚀 Creating maintain request: " + req);
        validateCreate(req);

        Room room = resolveRoom(req.getRoomId(), req.getRoomNumber());
        RoomAsset asset = resolveAsset(req.getRoomAssetId());
        
        System.out.println("🏠 Resolved room: " + (room != null ? room.getId() : "null"));
        System.out.println("🔧 Resolved asset: " + (asset != null ? asset.getId() : "null"));

        Maintain m = Maintain.builder()
                .targetType(req.getTargetType())
                .room(room)
                .roomAsset(asset)
                .issueCategory(req.getIssueCategory())
                .issueTitle(req.getIssueTitle())
                .issueDescription(req.getIssueDescription())
                .createDate(req.getCreateDate() != null ? req.getCreateDate() : LocalDateTime.now())
                .scheduledDate(req.getScheduledDate())
                .finishDate(req.getFinishDate())
                // ✅ เพิ่มฟิลด์ใหม่
                .maintainType(req.getMaintainType())
                .technicianName(req.getTechnicianName())
                .technicianPhone(req.getTechnicianPhone())
                .build();

        System.out.println("💾 Saving maintain entity...");
        Maintain saved = maintainRepository.save(m);
        System.out.println("✅ Saved with ID: " + saved.getId());
        
        return toDto(saved);
    }

    @Override
    @Transactional
    public MaintainDto update(Long id, UpdateMaintainRequest req) {
        Maintain m = maintainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintain not found: " + id));

        if (req.getTargetType() != null) m.setTargetType(req.getTargetType());

        if (req.getRoomId() != null || (req.getRoomNumber() != null && !req.getRoomNumber().isBlank())) {
            Room room = resolveRoom(req.getRoomId(), req.getRoomNumber());
            m.setRoom(room);
        }

        if (req.getRoomAssetId() != null) {
            RoomAsset asset = resolveAsset(req.getRoomAssetId());
            m.setRoomAsset(asset);
        }

        if (req.getIssueCategory() != null)      m.setIssueCategory(req.getIssueCategory());
        if (req.getIssueTitle() != null)         m.setIssueTitle(req.getIssueTitle());
        if (req.getIssueDescription() != null)   m.setIssueDescription(req.getIssueDescription());
        
        // ✅ อัปเดต scheduledDate (รับค่า null ได้)
        m.setScheduledDate(req.getScheduledDate());
        
        // ✅ อัปเดต finishDate (รับค่า null ได้)
        m.setFinishDate(req.getFinishDate());

        // ✅ อัปเดตฟิลด์ใหม่
        if (req.getMaintainType() != null)       m.setMaintainType(req.getMaintainType());
        if (req.getTechnicianName() != null)     m.setTechnicianName(req.getTechnicianName());
        if (req.getTechnicianPhone() != null)    m.setTechnicianPhone(req.getTechnicianPhone());

        return toDto(maintainRepository.save(m));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (maintainRepository.existsById(id)) {
            maintainRepository.deleteById(id);
        }
    }

    // ===== Helpers =====
    private void validateCreate(CreateMaintainRequest req) {
        if (req.getTargetType() == null) throw new IllegalArgumentException("targetType is required");
        if ((req.getRoomId() == null) && (req.getRoomNumber() == null || req.getRoomNumber().isBlank())) {
            throw new IllegalArgumentException("roomId or roomNumber is required");
        }
        if (req.getIssueCategory() == null) throw new IllegalArgumentException("issueCategory is required");
        if (req.getIssueTitle() == null || req.getIssueTitle().isBlank()) {
            throw new IllegalArgumentException("issueTitle is required");
        }
    }

    private Room resolveRoom(Long roomId, String roomNumber) {
        if (roomId != null) {
            return roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        }
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room not found by number: " + roomNumber));
    }

    private RoomAsset resolveAsset(Long roomAssetId) {
        if (roomAssetId == null) return null;
        return roomAssetRepository.findById(roomAssetId)
                .orElseThrow(() -> new IllegalArgumentException("RoomAsset not found: " + roomAssetId));
    }

    private MaintainDto toDto(Maintain m) {
        return MaintainDto.builder()
                .id(m.getId())
                .targetType(m.getTargetType())
                .roomId(m.getRoom() != null ? m.getRoom().getId() : null)
                .roomNumber(m.getRoom() != null ? m.getRoom().getRoomNumber() : null)
                .roomFloor(m.getRoom() != null ? m.getRoom().getRoomFloor() : null)
                .roomAssetId(m.getRoomAsset() != null ? m.getRoomAsset().getId() : null)
                .issueCategory(m.getIssueCategory())
                .issueTitle(m.getIssueTitle())
                .issueDescription(m.getIssueDescription())
                .createDate(m.getCreateDate())
                .scheduledDate(m.getScheduledDate())
                .finishDate(m.getFinishDate())
                // ✅ เพิ่มฟิลด์ใหม่
                .maintainType(m.getMaintainType())
                .technicianName(m.getTechnicianName())
                .technicianPhone(m.getTechnicianPhone())
                .build();
    }

    // ===== PDF Generation Feature =====
    
    @Override
    public byte[] generateMaintenanceReportPdf(Long maintainId) {
        System.out.println(">>> [MaintainService] Generating Maintenance Report PDF for maintainId=" + maintainId);
        
        // ดึงข้อมูล maintenance
        Maintain maintain = maintainRepository.findById(maintainId)
                .orElseThrow(() -> new RuntimeException("Maintain not found: " + maintainId));
        
        Room room = maintain.getRoom();
        if (room == null) {
            throw new RuntimeException("Room not found for maintain: " + maintainId);
        }

        RoomAsset roomAsset = maintain.getRoomAsset();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // สร้าง PDF document
            Document document = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // สร้างฟอนต์ที่ใช้ในระบบ
            Font[] fonts = PdfStyleService.createInvoiceFonts();
            Font titleFont = fonts[0];
            Font headerFont = fonts[1];
            Font labelFont = fonts[2];
            Font normalFont = fonts[3];
            Font smallFont = fonts[4];
            
            // หัวเรื่อง Company Header
            PdfStyleService.addCompanyHeader(document, titleFont, headerFont);
            
            // Title
            Paragraph title = new Paragraph("MAINTENANCE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            title.setSpacingBefore(10);
            document.add(title);
            
            PdfStyleService.addSeparatorLine(document);
            
            // ข้อมูลหลัก - ใช้ 2 columns
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingAfter(15);
            
            // ฝั่งซ้าย - ข้อมูลงาน
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(0);
            
            PdfPTable workInfoTable = new PdfPTable(2);
            workInfoTable.setWidthPercentage(100);
            workInfoTable.setWidths(new float[]{40, 60});
            
            workInfoTable.addCell(PdfStyleService.createLabelCell("Job Number:", labelFont));
            workInfoTable.addCell(PdfStyleService.createValueCell("MT-" + String.format("%06d", maintain.getId()), normalFont));
            
            workInfoTable.addCell(PdfStyleService.createLabelCell("Report Date:", labelFont));
            workInfoTable.addCell(PdfStyleService.createValueCell(
                maintain.getCreateDate() != null ? maintain.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-", 
                normalFont));
            
            workInfoTable.addCell(PdfStyleService.createLabelCell("Status:", labelFont));
            String status = getMaintenanceStatus(maintain);
            workInfoTable.addCell(PdfStyleService.createValueCell(status, normalFont));
            
            workInfoTable.addCell(PdfStyleService.createLabelCell("Maintenance Type:", labelFont));
            workInfoTable.addCell(PdfStyleService.createValueCell(
                PdfStyleService.nvl(maintain.getMaintainType()), normalFont));
            
            leftCell.addElement(workInfoTable);
            mainTable.addCell(leftCell);
            
            // ฝั่งขวา - ข้อมูลสถานที่
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPadding(0);
            
            PdfPTable locationTable = new PdfPTable(2);
            locationTable.setWidthPercentage(100);
            locationTable.setWidths(new float[]{40, 60});
            
            locationTable.addCell(PdfStyleService.createLabelCell("Room:", labelFont));
            locationTable.addCell(PdfStyleService.createValueCell(
                "Floor " + room.getRoomFloor() + " Room " + room.getRoomNumber(), normalFont));
            
            locationTable.addCell(PdfStyleService.createLabelCell("Work Type:", labelFont));
            String targetType = maintain.getTargetType() == 0 ? "Item Repair" : "Room Repair";
            locationTable.addCell(PdfStyleService.createValueCell(targetType, normalFont));
            
            if (roomAsset != null && roomAsset.getAsset() != null) {
                locationTable.addCell(PdfStyleService.createLabelCell("Asset:", labelFont));
                locationTable.addCell(PdfStyleService.createValueCell(roomAsset.getAsset().getAssetName(), normalFont));
            }
            
            locationTable.addCell(PdfStyleService.createLabelCell("Issue Category:", labelFont));
            locationTable.addCell(PdfStyleService.createValueCell(getIssueCategoryText(maintain.getIssueCategory()), normalFont));
            
            rightCell.addElement(locationTable);
            mainTable.addCell(rightCell);
            
            document.add(mainTable);
            
            // รายละเอียดปัญหา
            document.add(new Paragraph("Issue Details", headerFont));
            document.add(Chunk.NEWLINE);
            
            PdfPTable problemTable = new PdfPTable(1);
            problemTable.setWidthPercentage(100);
            problemTable.setSpacingAfter(15);
            
            problemTable.addCell(PdfStyleService.createLabelCell("Issue Title:", labelFont));
            problemTable.addCell(PdfStyleService.createValueCell(PdfStyleService.nvl(maintain.getIssueTitle()), normalFont));
            
            problemTable.addCell(PdfStyleService.createLabelCell("Description:", labelFont));
            PdfPCell descCell = PdfStyleService.createValueCell(PdfStyleService.nvl(maintain.getIssueDescription()), normalFont);
            descCell.setMinimumHeight(60);
            problemTable.addCell(descCell);
            
            document.add(problemTable);
            
            // Technician & Schedule Information
            if (maintain.getTechnicianName() != null || maintain.getScheduledDate() != null || maintain.getFinishDate() != null) {
                document.add(new Paragraph("Technician & Schedule Information", headerFont));
                document.add(Chunk.NEWLINE);
                
                PdfPTable techTable = new PdfPTable(2);
                techTable.setWidthPercentage(100);
                techTable.setWidths(new float[]{30, 70});
                techTable.setSpacingAfter(15);
                
                if (maintain.getTechnicianName() != null) {
                    techTable.addCell(PdfStyleService.createLabelCell("Technician Name:", labelFont));
                    techTable.addCell(PdfStyleService.createValueCell(maintain.getTechnicianName(), normalFont));
                }
                
                if (maintain.getTechnicianPhone() != null) {
                    techTable.addCell(PdfStyleService.createLabelCell("Technician Phone:", labelFont));
                    techTable.addCell(PdfStyleService.createValueCell(maintain.getTechnicianPhone(), normalFont));
                }
                
                if (maintain.getScheduledDate() != null) {
                    techTable.addCell(PdfStyleService.createLabelCell("Scheduled Date:", labelFont));
                    techTable.addCell(PdfStyleService.createValueCell(
                        maintain.getScheduledDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
                }
                
                if (maintain.getFinishDate() != null) {
                    techTable.addCell(PdfStyleService.createLabelCell("Completion Date:", labelFont));
                    techTable.addCell(PdfStyleService.createValueCell(
                        maintain.getFinishDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
                }
                
                document.add(techTable);
            }
            
            // Footer
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            
            Paragraph footer = new Paragraph(
                "This report was generated by OrganicNow System on " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), 
                smallFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Error generating maintenance report PDF: " + e.getMessage());
            throw new RuntimeException("Failed to generate maintenance report PDF", e);
        }
    }
    
    private String getMaintenanceStatus(Maintain maintain) {
        if (maintain.getFinishDate() != null) {
            return "Completed";
        } else if (maintain.getScheduledDate() != null) {
            return "In Progress";
        } else {
            return "Pending";
        }
    }
    
    private String getIssueCategoryText(Integer category) {
        if (category == null) return "-";
        switch (category) {
            case 0: return "Structure";
            case 1: return "Electrical";
            case 2: return "Plumbing";
            case 3: return "Appliances/Furniture";
            case 4: return "Security";
            case 5: return "Others";
            default: return "Not Specified";
        }
    }
}
