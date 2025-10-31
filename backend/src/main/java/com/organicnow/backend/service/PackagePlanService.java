package com.organicnow.backend.service;

import com.organicnow.backend.dto.PackagePlanDto;
import com.organicnow.backend.dto.PackagePlanRequestDto;
import com.organicnow.backend.model.PackagePlan;
import com.organicnow.backend.repository.ContractTypeRepository;
import com.organicnow.backend.repository.PackagePlanRepository;
import com.organicnow.backend.repository.RoomRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PackagePlanService {

    private final PackagePlanRepository packagePlanRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final RoomRepository roomRepository;

    public PackagePlanService(PackagePlanRepository packagePlanRepository,
                              ContractTypeRepository contractTypeRepository,
                              RoomRepository roomRepository) {
        this.packagePlanRepository = packagePlanRepository;
        this.contractTypeRepository = contractTypeRepository;
        this.roomRepository = roomRepository;
    }

    // ✅ CREATE package plan
    @Transactional
    public PackagePlanDto createPackage(PackagePlanRequestDto dto) {
        if (dto.getContractTypeId() == null) {
            throw new ResponseStatusException(CONFLICT, "contractTypeId is required");
        }
        if (dto.getRoomSize() == null) {
            throw new ResponseStatusException(CONFLICT, "roomSize is required");
        }

        var ct = contractTypeRepository.findById(dto.getContractTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "ContractType not found with id " + dto.getContractTypeId()
                ));

        // ยืนยันว่ามีห้องขนาดนี้จริง
        if (!roomRepository.existsByRoomSize(dto.getRoomSize())) {
            throw new ResponseStatusException(
                    NOT_FOUND, "No room found with room_size " + dto.getRoomSize());
        }

        Long ctId = ct.getId();
        Integer size = dto.getRoomSize();

        // ปิดตัวเก่าที่ active ของคู่ (contractTypeId, roomSize)
        packagePlanRepository.deactivateActiveForPair(ctId, size);

        // สร้างตัวใหม่เป็น active = 1 เสมอ ตามกติกา
        var plan = PackagePlan.builder()
                .price(dto.getPrice())
                .isActive(1)
                .contractType(ct)
                .roomSize(size)
                .build();

        var saved = packagePlanRepository.save(plan);

        var ctSaved = saved.getContractType();
        return new PackagePlanDto(
                saved.getId(),
                saved.getPrice(),
                saved.getIsActive(),
                ctSaved != null ? ctSaved.getName() : null,
                ctSaved != null ? ctSaved.getDuration() : null,
                ctSaved != null ? ctSaved.getId() : null,
                ctSaved != null ? ctSaved.getName() : null,
                saved.getRoomSize()
        );
    }

    // ✅ GET packages list
    public List<PackagePlanDto> getAllPackages() {
        return packagePlanRepository.findAll()
                .stream()
                .map(p -> {
                    var ct = p.getContractType();
                    return new PackagePlanDto(
                            p.getId(),
                            p.getPrice(),
                            p.getIsActive(),
                            (ct != null ? ct.getName() : null),
                            (ct != null ? ct.getDuration() : null),
                            (ct != null ? ct.getId() : null),
                            (ct != null ? ct.getName() : null),
                            p.getRoomSize()
                    );
                })
                .collect(Collectors.toList());
    }

    // ✅ TOGGLE active status (0 <-> 1)
    @Transactional
    public PackagePlanDto togglePackageStatus(Long packageId) {
        PackagePlan pkg = packagePlanRepository.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Package not found"));

        Integer current = pkg.getIsActive() != null ? pkg.getIsActive() : 0;
        pkg.setIsActive(current == 1 ? 0 : 1);
        PackagePlan saved = packagePlanRepository.save(pkg);

        var ct = saved.getContractType();
        return new PackagePlanDto(
                saved.getId(),
                saved.getPrice(),
                saved.getIsActive(),
                ct != null ? ct.getName() : null,
                ct != null ? ct.getDuration() : null,
                ct != null ? ct.getId() : null,
                ct != null ? ct.getName() : null,
                saved.getRoomSize()
        );
    }
}