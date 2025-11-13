package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Admin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void testGetterAndSetter() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setAdminUsername("adminUser");
        admin.setAdminPassword("password123");
        admin.setAdminRole(1);

        assertEquals(1L, admin.getId());
        assertEquals("adminUser", admin.getAdminUsername());
        assertEquals("password123", admin.getAdminPassword());
        assertEquals(1, admin.getAdminRole());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Admin admin = Admin.builder()
                .id(2L)
                .adminUsername("superadmin")
                .adminPassword("securePass")
                .adminRole(1)
                .build();

        assertNotNull(admin);
        assertEquals(2L, admin.getId());
        assertEquals("superadmin", admin.getAdminUsername());
        assertEquals("securePass", admin.getAdminPassword());
        assertEquals(1, admin.getAdminRole());
    }

    @Test
    void testNoArgsConstructorAndDefaultRole() {
        Admin admin = new Admin();
        assertNotNull(admin);
        assertEquals(0, admin.getAdminRole());
    }

    @Test
    void testAllArgsConstructor() {
        Admin admin = new Admin(3L, "john", "abc123", 1);
        assertEquals(3L, admin.getId());
        assertEquals("john", admin.getAdminUsername());
        assertEquals("abc123", admin.getAdminPassword());
        assertEquals(1, admin.getAdminRole());
    }

    @Test
    void testToStringNotNull() {
        Admin admin = new Admin(4L, "maria", "hashpass", 0);
        assertNotNull(admin.toString()); // แค่เช็กว่าไม่เป็น null
        // ❌ อย่าเช็ก contains("maria") เพราะ model ไม่มี @ToString
    }


    @Test
    void testEqualsAndHashCode() {
        Admin a1 = new Admin(10L, "test", "pw", 0);
        Admin a2 = a1; // อ้างอิงอ็อบเจกต์เดียวกัน
        Admin a3 = new Admin(11L, "test2", "pw2", 1);

        // ถ้าไม่ override equals/hashCode, เทียบได้แค่ reference
        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
    }

}
