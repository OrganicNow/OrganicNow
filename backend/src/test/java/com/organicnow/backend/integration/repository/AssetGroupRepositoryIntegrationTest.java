package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // ถ้ามี profile test ก็ใช้, ถ้าไม่มีจะเอาออกก็ได้
@Transactional // ✅ ให้แต่ละเทส rollback อัตโนมัติ
class AssetGroupRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AssetGroupRepository assetGroupRepository;

    @Test
    @DisplayName("existsByAssetGroupName() ควรคืน true เมื่อมีชื่อกลุ่มนี้อยู่ใน DB")
    void existsByAssetGroupName_shouldReturnTrue_whenNameExists() {
        // given
        AssetGroup group = new AssetGroup();
        group.setAssetGroupName("Kitchen Set");
        // field อื่น ๆ เป็น nullable ไม่จำเป็นต้องเซ็ต
        assetGroupRepository.save(group);

        // when
        boolean exists = assetGroupRepository.existsByAssetGroupName("Kitchen Set");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByAssetGroupName() ควรคืน false เมื่อไม่มีชื่อกลุ่มนี้ใน DB")
    void existsByAssetGroupName_shouldReturnFalse_whenNameDoesNotExist() {
        // given
        // ใส่ data อื่นเพื่อให้แน่ใจว่า method เช็คตามชื่อจริง ๆ
        AssetGroup group = new AssetGroup();
        group.setAssetGroupName("Living Room Set");
        assetGroupRepository.save(group);

        // when
        boolean exists = assetGroupRepository.existsByAssetGroupName("Bedroom Set");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByAssetGroupName() ควรแยกแยะชื่อที่ต่างกันแบบ case-sensitive (ขึ้นกับ DB collation)")
    void existsByAssetGroupName_shouldBeCaseSensitive_withDefaultPostgresCollation() {
        // given
        AssetGroup group = new AssetGroup();
        group.setAssetGroupName("Test Group");
        assetGroupRepository.save(group);

        // when
        boolean exactMatch = assetGroupRepository.existsByAssetGroupName("Test Group");
        boolean differentCase = assetGroupRepository.existsByAssetGroupName("test group");

        // then
        assertThat(exactMatch).isTrue();
        // ปกติ Postgres default collation จะ case-sensitive => ต่างตัวพิมพ์ควรเป็น false
        assertThat(differentCase).isFalse();
    }
}
