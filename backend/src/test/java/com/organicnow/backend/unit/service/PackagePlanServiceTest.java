package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.PackagePlanDto;
import com.organicnow.backend.dto.PackagePlanRequestDto;
import com.organicnow.backend.model.ContractType;
import com.organicnow.backend.model.PackagePlan;
import com.organicnow.backend.repository.ContractTypeRepository;
import com.organicnow.backend.repository.PackagePlanRepository;
import com.organicnow.backend.repository.RoomRepository;
import com.organicnow.backend.service.PackagePlanService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class PackagePlanServiceTest {

    private AutoCloseable closeable;

    @Mock private PackagePlanRepository packagePlanRepository;
    @Mock private ContractTypeRepository contractTypeRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private PackagePlanService service;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ================================
    // CREATE PACKAGE
    // ================================
    @Test
    void testCreatePackage_MissingContractTypeId() {

        PackagePlanRequestDto dto = new PackagePlanRequestDto();
        dto.setRoomSize(25);
        dto.setPrice(BigDecimal.valueOf(5000));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPackage(dto));

        assertEquals(CONFLICT, ex.getStatusCode());
    }

    @Test
    void testCreatePackage_MissingRoomSize() {

        PackagePlanRequestDto dto = new PackagePlanRequestDto();
        dto.setContractTypeId(10L);
        dto.setPrice(BigDecimal.valueOf(5000));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPackage(dto));

        assertEquals(CONFLICT, ex.getStatusCode());
    }

    @Test
    void testCreatePackage_ContractTypeNotFound() {

        PackagePlanRequestDto dto = new PackagePlanRequestDto();
        dto.setContractTypeId(10L);
        dto.setRoomSize(25);
        dto.setPrice(BigDecimal.valueOf(8000));

        when(contractTypeRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPackage(dto));

        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testCreatePackage_RoomSizeNotExist() {

        PackagePlanRequestDto dto = new PackagePlanRequestDto();
        dto.setContractTypeId(10L);
        dto.setRoomSize(25);
        dto.setPrice(BigDecimal.valueOf(6000));

        ContractType ct = new ContractType();
        ct.setId(10L);
        ct.setName("Monthly");
        ct.setDuration(1);

        when(contractTypeRepository.findById(10L)).thenReturn(Optional.of(ct));
        when(roomRepository.existsByRoomSize(25)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPackage(dto));

        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testCreatePackage_Success() {

        PackagePlanRequestDto dto = new PackagePlanRequestDto();
        dto.setContractTypeId(10L);
        dto.setRoomSize(25);
        dto.setPrice(BigDecimal.valueOf(7000));

        ContractType ct = new ContractType();
        ct.setId(10L);
        ct.setName("Monthly");
        ct.setDuration(1);

        when(contractTypeRepository.findById(10L)).thenReturn(Optional.of(ct));
        when(roomRepository.existsByRoomSize(25)).thenReturn(true);

        PackagePlan saved = PackagePlan.builder()
                .id(99L)
                .price(BigDecimal.valueOf(7000))
                .isActive(1)
                .contractType(ct)
                .roomSize(25)
                .build();

        when(packagePlanRepository.save(any())).thenReturn(saved);

        PackagePlanDto result = service.createPackage(dto);

        assertEquals(99L, result.getId());
        assertEquals(BigDecimal.valueOf(7000), result.getPrice());
        assertEquals(1, result.getIsActive());
        assertEquals("Monthly", result.getContractTypeName());
    }

    // ================================
    // GET ALL PACKAGES
    // ================================
    @Test
    void testGetAllPackages() {

        ContractType ct = new ContractType();
        ct.setId(1L);
        ct.setName("Monthly");
        ct.setDuration(1);

        PackagePlan p = PackagePlan.builder()
                .id(1L)
                .price(BigDecimal.valueOf(5000))
                .isActive(1)
                .contractType(ct)
                .roomSize(25)
                .build();

        when(packagePlanRepository.findAll()).thenReturn(List.of(p));

        List<PackagePlanDto> result = service.getAllPackages();

        assertEquals(1, result.size());
        assertEquals(5000, result.get(0).getPrice().intValue());
    }

    // ================================
    // TOGGLE STATUS
    // ================================
    @Test
    void testTogglePackageStatus_NotFound() {

        when(packagePlanRepository.findById(123L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.togglePackageStatus(123L));

        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testTogglePackageStatus_From1To0() {

        PackagePlan pkg = PackagePlan.builder()
                .id(10L)
                .price(BigDecimal.valueOf(5000))
                .isActive(1)
                .roomSize(25)
                .contractType(new ContractType())
                .build();

        when(packagePlanRepository.findById(10L)).thenReturn(Optional.of(pkg));
        when(packagePlanRepository.save(any())).thenReturn(pkg);

        PackagePlanDto result = service.togglePackageStatus(10L);

        assertEquals(0, result.getIsActive());
    }

    @Test
    void testTogglePackageStatus_From0To1() {

        PackagePlan pkg = PackagePlan.builder()
                .id(10L)
                .price(BigDecimal.valueOf(5000))
                .isActive(0)
                .roomSize(25)
                .contractType(new ContractType())
                .build();

        when(packagePlanRepository.findById(10L)).thenReturn(Optional.of(pkg));
        when(packagePlanRepository.save(any())).thenReturn(pkg);

        PackagePlanDto result = service.togglePackageStatus(10L);

        assertEquals(1, result.getIsActive());
    }
}
