package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.Admin;
import com.organicnow.backend.repository.AdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // ถ้ามี profile test ก็ใช้, ถ้าไม่มีจะเอาออกก็ได้
class AdminRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AdminRepository adminRepository;

    @Test
    @DisplayName("findByAdminUsername() ควรคืนค่า Admin เมื่อ username มีอยู่ใน DB")
    void findByAdminUsername_shouldReturnAdmin_whenUsernameExists() {
        // given
        Admin admin = new Admin();
        admin.setAdminUsername("admin1");
        admin.setAdminPassword("secret"); // เปลี่ยนตามชื่อ field จริงใน entity
        // TODO: ถ้า Admin มี field not-null อื่น ๆ ต้อง set ให้ครบ
        admin = adminRepository.save(admin);

        // when
        Optional<Admin> result = adminRepository.findByAdminUsername("admin1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(admin.getId());
        assertThat(result.get().getAdminUsername()).isEqualTo("admin1");
    }

    @Test
    @DisplayName("findByAdminUsername() ควรคืน Optional.empty เมื่อ username ไม่มีใน DB")
    void findByAdminUsername_shouldReturnEmpty_whenUsernameNotExists() {
        // when
        Optional<Admin> result = adminRepository.findByAdminUsername("no_such_user");

        // then
        assertThat(result).isEmpty();
    }
}
