//package com.organicnow.backend.service;
//
//import com.organicnow.backend.dto.PackagePlanDto;
//import com.organicnow.backend.dto.PackagePlanRequestDto;
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.model.PackagePlan;
//import com.organicnow.backend.repository.ContractTypeRepository;
//import com.organicnow.backend.repository.PackagePlanRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class PackagePlanServiceTest {
//
//    @Mock
//    private PackagePlanRepository packagePlanRepository;
//
//    @Mock
//    private ContractTypeRepository contractTypeRepository;
//
//    @InjectMocks
//    private PackagePlanService packagePlanService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void createPackage_shouldCloseOldPlans_andSaveNewOne() {
//        ContractType contractType = new ContractType();
//        contractType.setId(1L);
//        contractType.setName("3 เดือน");
//        contractType.setDuration(3);
//
//        PackagePlan oldPlan = PackagePlan.builder()
//                .id(100L)
//                .price(BigDecimal.valueOf(5000.0))
//                .isActive(1)
//                .contractType(contractType)
//                .build();
//
//        PackagePlanRequestDto dto = new PackagePlanRequestDto();
//        dto.setPrice(BigDecimal.valueOf(8000.0));
//        dto.setIsActive(1);
//        dto.setContractTypeId(1L);
//
//        when(contractTypeRepository.findById(1L)).thenReturn(Optional.of(contractType));
//        when(packagePlanRepository.findByContractType_NameAndIsActive("3 เดือน", 1))
//                .thenReturn(List.of(oldPlan));
//
//        packagePlanService.createPackage(dto);
//
//        verify(packagePlanRepository, times(1)).save(oldPlan);
//        verify(packagePlanRepository, times(1)).save(argThat(newPlan ->
//                newPlan.getPrice().compareTo(BigDecimal.valueOf(8000.0)) == 0 &&
//                        newPlan.getIsActive() == 1 &&
//                        newPlan.getContractType().getName().equals("3 เดือน")
//        ));
//    }
//
//    @Test
//    void createPackage_shouldThrowIfContractTypeNotFound() {
//        PackagePlanRequestDto dto = new PackagePlanRequestDto();
//        dto.setContractTypeId(99L);
//
//        when(contractTypeRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(ResponseStatusException.class,
//                () -> packagePlanService.createPackage(dto));
//    }
//
//    @Test
//    void getAllPackages_shouldReturnDtos() {
//        ContractType contractType = new ContractType();
//        contractType.setId(1L);
//        contractType.setName("6 เดือน");
//        contractType.setDuration(6);
//
//        PackagePlan plan = PackagePlan.builder()
//                .id(200L)
//                .price(BigDecimal.valueOf(10000.0))
//                .isActive(1)
//                .contractType(contractType)
//                .build();
//
//        when(packagePlanRepository.findAll()).thenReturn(Collections.singletonList(plan));
//
//        List<PackagePlanDto> result = packagePlanService.getAllPackages();
//
//        assertEquals(1, result.size());
//        assertEquals("6 เดือน", result.get(0).getName());   // ✅ ใช้ getName()
//        assertEquals(6, result.get(0).getDuration());
//        assertEquals(BigDecimal.valueOf(10000.0), result.get(0).getPrice());
//    }
//}
