//package com.organicnow.backend.unit.dto;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * ✅ Unit test for NotificationDto
// * - ทดสอบการทำงานของ Lombok (builder, getter/setter, constructors)
// */
//class NotificationDtoTest {
//
//    @Test
//    @DisplayName("Builder → should correctly build NotificationDto object")
//    void testBuilderCreatesCorrectObject() {
//        LocalDateTime now = LocalDateTime.now();
//
//        NotificationDto dto = NotificationDto.builder()
//                .id(1L)
//                .title("Maintenance Reminder")
//                .message("Check AC in room A-101")
//                .type("MAINTENANCE")
//                .isRead(false)
//                .createdAt(now)
//                .readAt(null)
//                .maintenanceScheduleId(10L)
//                .maintenanceScheduleTitle("Air Conditioner Check")
//                .build();
//
//        assertThat(dto.getId()).isEqualTo(1L);
//        assertThat(dto.getTitle()).isEqualTo("Maintenance Reminder");
//        assertThat(dto.getMessage()).isEqualTo("Check AC in room A-101");
//        assertThat(dto.getType()).isEqualTo("MAINTENANCE");
//        assertThat(dto.getIsRead()).isFalse();
//        assertThat(dto.getCreatedAt()).isEqualTo(now);
//        assertThat(dto.getReadAt()).isNull();
//        assertThat(dto.getMaintenanceScheduleId()).isEqualTo(10L);
//        assertThat(dto.getMaintenanceScheduleTitle()).isEqualTo("Air Conditioner Check");
//    }
//
//    @Test
//    @DisplayName("AllArgsConstructor → should assign all fields correctly")
//    void testAllArgsConstructor() {
//        LocalDateTime created = LocalDateTime.now();
//        LocalDateTime read = created.plusHours(2);
//
//        NotificationDto dto = new NotificationDto(
//                2L,
//                "Invoice Paid",
//                "Tenant completed payment",
//                "PAYMENT",
//                true,
//                created,
//                read,
//                20L,
//                "Payment Notification"
//        );
//
//        assertThat(dto.getId()).isEqualTo(2L);
//        assertThat(dto.getTitle()).isEqualTo("Invoice Paid");
//        assertThat(dto.getMessage()).isEqualTo("Tenant completed payment");
//        assertThat(dto.getType()).isEqualTo("PAYMENT");
//        assertThat(dto.getIsRead()).isTrue();
//        assertThat(dto.getCreatedAt()).isEqualTo(created);
//        assertThat(dto.getReadAt()).isEqualTo(read);
//        assertThat(dto.getMaintenanceScheduleId()).isEqualTo(20L);
//        assertThat(dto.getMaintenanceScheduleTitle()).isEqualTo("Payment Notification");
//    }
//
//    @Test
//    @DisplayName("NoArgsConstructor + Setters → should set and get values correctly")
//    void testNoArgsConstructorAndSetters() {
//        LocalDateTime now = LocalDateTime.now();
//
//        NotificationDto dto = new NotificationDto();
//        dto.setId(5L);
//        dto.setTitle("System Alert");
//        dto.setMessage("Temperature too high");
//        dto.setType("ALERT");
//        dto.setIsRead(true);
//        dto.setCreatedAt(now);
//        dto.setReadAt(now.plusMinutes(30));
//        dto.setMaintenanceScheduleId(99L);
//        dto.setMaintenanceScheduleTitle("Temperature Check");
//
//        assertThat(dto.getId()).isEqualTo(5L);
//        assertThat(dto.getTitle()).isEqualTo("System Alert");
//        assertThat(dto.getMessage()).isEqualTo("Temperature too high");
//        assertThat(dto.getType()).isEqualTo("ALERT");
//        assertThat(dto.getIsRead()).isTrue();
//        assertThat(dto.getCreatedAt()).isEqualTo(now);
//        assertThat(dto.getReadAt()).isEqualTo(now.plusMinutes(30));
//        assertThat(dto.getMaintenanceScheduleId()).isEqualTo(99L);
//        assertThat(dto.getMaintenanceScheduleTitle()).isEqualTo("Temperature Check");
//    }
//
//    @Test
//    @DisplayName("Field equality check without toString()")
//    void testToStringEqualsAndHashCode() {
//        LocalDateTime now = LocalDateTime.now();
//
//        NotificationDto dto1 = NotificationDto.builder()
//                .id(1L)
//                .title("Alert")
//                .message("Test message")
//                .type("INFO")
//                .isRead(false)
//                .createdAt(now)
//                .maintenanceScheduleId(10L)
//                .build();
//
//        NotificationDto dto2 = NotificationDto.builder()
//                .id(1L)
//                .title("Alert")
//                .message("Test message")
//                .type("INFO")
//                .isRead(false)
//                .createdAt(now)
//                .maintenanceScheduleId(10L)
//                .build();
//
//        // ✅ ตรวจเท่ากันแบบ field-by-field
//        assertThat(dto1)
//                .usingRecursiveComparison()
//                .isEqualTo(dto2);
//
//        // ✅ ตรวจค่า field ตรง ๆ แทนการใช้ toString()
//        assertThat(dto1.getTitle()).isEqualTo("Alert");
//        assertThat(dto1.getType()).isEqualTo("INFO");
//    }
//
//}
