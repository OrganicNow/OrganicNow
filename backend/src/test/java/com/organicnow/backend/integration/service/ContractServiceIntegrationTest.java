package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.ContractService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ContractServiceIntegrationTest.TestConfig.class
)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional

class ContractServiceIntegrationTest {


    @Configuration
    @EnableAutoConfiguration
    @EntityScan("com.organicnow.backend.model")
    @EnableJpaRepositories("com.organicnow.backend.repository")
    static class TestConfig {

        @Bean
        public ContractService contractService(
                ContractRepository cr,
                RoomRepository rr
        ) {
            return new ContractService(cr, rr);
        }
    }

    // -------------------------------
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    // -------------------------------
    @Autowired private ContractService contractService;
    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;

    private Room room;
    private Tenant tenant;
    private PackagePlan plan;
    private Contract savedContract;

    @BeforeEach
    void setup() {

        room = roomRepository.save(
                Room.builder()
                        .roomNumber("A101")
                        .roomFloor(1)
                        .roomSize(25)
                        .build()
        );

        tenant = tenantRepository.save(
                Tenant.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phoneNumber("0900000000")
                        .email("john@example.com")
                        .nationalId("1234567890123")
                        .build()
        );

        ContractType type = contractTypeRepository.save(
                ContractType.builder()
                        .name("Monthly")
                        .duration(1)
                        .build()
        );

        plan = packagePlanRepository.save(
                PackagePlan.builder()
                        .contractType(type)
                        .price(new BigDecimal("5000"))
                        .isActive(1)
                        .roomSize(25)
                        .build()
        );

        savedContract = contractRepository.save(
                Contract.builder()
                        .room(room)
                        .tenant(tenant)
                        .packagePlan(plan)
                        .signDate(LocalDateTime.now().minusDays(10))
                        .startDate(LocalDateTime.now().minusDays(10))
                        .endDate(LocalDateTime.now().plusDays(20))
                        .status(1)
                        .deposit(BigDecimal.ZERO)
                        .rentAmountSnapshot(new BigDecimal("5000"))
                        .build()
        );
    }

    @Test
    void testGetTenantList() {
        List<TenantDto> list = contractService.getTenantList();
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testGetOccupiedRoomIds() {
        List<Long> ids = contractService.getOccupiedRoomIds();
        Assertions.assertThat(ids).contains(room.getId());
    }

    @Test
    void testFindContractByFloorAndRoom() {
        TenantDto dto = contractService.findContractByFloorAndRoom(1, "A101");

        Assertions.assertThat(dto).isNotNull();
        Assertions.assertThat(dto.getFirstName()).isEqualTo("John");
        Assertions.assertThat(dto.getContractId()).isEqualTo(savedContract.getId());
    }
}
