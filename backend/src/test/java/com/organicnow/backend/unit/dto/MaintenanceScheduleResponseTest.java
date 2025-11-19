package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.AssetGroupDropdownDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.dto.MaintenanceScheduleResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceScheduleResponseTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @Test
    void testGetterSetterAndConstructor() {
        MaintenanceScheduleDto s1 = MaintenanceScheduleDto.builder()
                .id(1L).scheduleScope(1)
                .assetGroupId(100L).assetGroupName("Group A")
                .cycleMonth(6)
                .lastDoneDate(LocalDateTime.now())
                .nextDueDate(LocalDateTime.now().plusMonths(6))
                .notifyBeforeDate(7)
                .scheduleTitle("Check A")
                .scheduleDescription("Description A")
                .build();

        AssetGroupDropdownDto a1 = AssetGroupDropdownDto.builder()
                .id(10L).name("AG1")
                .threshold(5)
                .monthlyAddonFee(BigDecimal.valueOf(100))
                .oneTimeDamageFee(BigDecimal.valueOf(200))
                .freeReplacement(true)
                .updatedAt(LocalDateTime.now())
                .build();

        MaintenanceScheduleResponse response =
                new MaintenanceScheduleResponse(
                        List.of(s1),
                        List.of(a1)
                );

        assertEquals(1, response.getResult().size());
        assertEquals(1, response.getAssetGroupDropdown().size());
        assertEquals("AG1", response.getAssetGroupDropdown().get(0).getName());
        assertEquals("Check A", response.getResult().get(0).getScheduleTitle());
    }

    @Test
    void testBuilder() {
        MaintenanceScheduleResponse response = MaintenanceScheduleResponse.builder()
                .result(List.of())
                .assetGroupDropdown(List.of())
                .build();

        assertNotNull(response.getResult());
        assertNotNull(response.getAssetGroupDropdown());
        assertEquals(0, response.getResult().size());
        assertEquals(0, response.getAssetGroupDropdown().size());
    }

    @Test
    void testEqualsByField() {
        MaintenanceScheduleDto s1 = MaintenanceScheduleDto.builder()
                .id(1L).scheduleScope(2).assetGroupId(3L)
                .scheduleTitle("TitleT").build();

        MaintenanceScheduleDto s2 = MaintenanceScheduleDto.builder()
                .id(1L).scheduleScope(2).assetGroupId(3L)
                .scheduleTitle("TitleT").build();

        AssetGroupDropdownDto a1 = AssetGroupDropdownDto.builder()
                .id(10L).name("GG")
                .threshold(5)
                .monthlyAddonFee(BigDecimal.valueOf(10))
                .oneTimeDamageFee(BigDecimal.valueOf(20))
                .freeReplacement(false)
                .updatedAt(LocalDateTime.now())
                .build();

        AssetGroupDropdownDto a2 = AssetGroupDropdownDto.builder()
                .id(10L).name("GG")
                .threshold(5)
                .monthlyAddonFee(BigDecimal.valueOf(10))
                .oneTimeDamageFee(BigDecimal.valueOf(20))
                .freeReplacement(false)
                .updatedAt(a1.getUpdatedAt())
                .build();

        MaintenanceScheduleResponse r1 = MaintenanceScheduleResponse.builder()
                .result(List.of(s1))
                .assetGroupDropdown(List.of(a1))
                .build();

        MaintenanceScheduleResponse r2 = MaintenanceScheduleResponse.builder()
                .result(List.of(s2))
                .assetGroupDropdown(List.of(a2))
                .build();

        // เทียบค่า ไม่ใช่ Object Reference
        assertEquals(r1.getResult().size(), r2.getResult().size());
        assertEquals(r1.getAssetGroupDropdown().size(), r2.getAssetGroupDropdown().size());

        assertEquals(r1.getResult().get(0).getId(), r2.getResult().get(0).getId());
        assertEquals(r1.getResult().get(0).getScheduleTitle(), r2.getResult().get(0).getScheduleTitle());

        assertEquals(r1.getAssetGroupDropdown().get(0).getName(),
                r2.getAssetGroupDropdown().get(0).getName());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        MaintenanceScheduleDto schedule = MaintenanceScheduleDto.builder()
                .id(5L).scheduleScope(1).assetGroupName("Engine")
                .cycleMonth(6)
                .lastDoneDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .nextDueDate(LocalDateTime.of(2025, 7, 1, 10, 0))
                .scheduleTitle("Engine Check")
                .build();

        AssetGroupDropdownDto dropdown = AssetGroupDropdownDto.builder()
                .id(99L).name("Cooling")
                .threshold(10)
                .monthlyAddonFee(BigDecimal.valueOf(300))
                .oneTimeDamageFee(BigDecimal.valueOf(1000))
                .freeReplacement(true)
                .updatedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        MaintenanceScheduleResponse original = MaintenanceScheduleResponse.builder()
                .result(List.of(schedule))
                .assetGroupDropdown(List.of(dropdown))
                .build();

        String json = mapper().writeValueAsString(original);
        MaintenanceScheduleResponse clone =
                mapper().readValue(json, MaintenanceScheduleResponse.class);

        assertEquals(original.getResult().size(), clone.getResult().size());
        assertEquals(original.getAssetGroupDropdown().size(), clone.getAssetGroupDropdown().size());

        assertEquals(original.getResult().get(0).getId(), clone.getResult().get(0).getId());
        assertEquals(original.getResult().get(0).getScheduleTitle(),
                clone.getResult().get(0).getScheduleTitle());

        assertEquals(original.getAssetGroupDropdown().get(0).getName(),
                clone.getAssetGroupDropdown().get(0).getName());
    }

    @Test
    void testNullSafety() {
        MaintenanceScheduleResponse response = new MaintenanceScheduleResponse();
        assertNull(response.getResult());
        assertNull(response.getAssetGroupDropdown());
    }
}
