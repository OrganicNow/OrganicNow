package com.organicnow.backend.repository;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.dto.TenantDto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // ใช้ใน /tenant/list
    @Query("""
        select new com.organicnow.backend.dto.TenantDto(
            c.id,
            t.firstName,
            t.lastName,
            r.roomFloor,
            r.roomNumber,
            r.id,
            p.id,
            p.contractType.id,
            p.contractType.name,
            c.startDate,
            c.endDate,
            t.phoneNumber,
            t.email,
            t.nationalId,
            case when c.endDate < CURRENT_TIMESTAMP then 0 else c.status end,
            c.rentAmountSnapshot
        )
        from Contract c
        join c.tenant t
        join c.room r
        join c.packagePlan p
        order by c.signDate desc
    """)
    List<TenantDto> findTenantRows();

    // ใช้ใน /tenant/search
    @Query("""
        select new com.organicnow.backend.dto.TenantDto(
            c.id,
            t.firstName,
            t.lastName,
            r.roomFloor,
            r.roomNumber,
            r.id,
            p.id,
            p.contractType.id,
            p.contractType.name,
            c.startDate,
            c.endDate,
            t.phoneNumber,
            t.email,
            t.nationalId,
            case when c.endDate < CURRENT_TIMESTAMP then 0 else c.status end,
            c.rentAmountSnapshot
        )
        from Contract c
        join c.tenant t
        join c.room r
        join c.packagePlan p
        where t.id in :tenantIds
        order by c.signDate desc
    """)
    List<TenantDto> findTenantRowsByTenantIds(@Param("tenantIds") List<Long> tenantIds);

    boolean existsByTenant_IdAndStatusAndEndDateAfter(Long tenantId, Integer status, LocalDateTime now);

    @Query("""
        select case when count(c) > 0 then true else false end
        from Contract c
        where c.room.id = :roomId
          and c.status = 1
          and c.endDate >= CURRENT_TIMESTAMP
    """)
    boolean existsActiveContractByRoomId(Long roomId);

    @Query("""
        select c.room.id
        from Contract c
        where c.status = 1
          and c.endDate >= CURRENT_TIMESTAMP
    """)
    List<Long> findCurrentlyOccupiedRoomIds();

    @Modifying
    @Transactional
    @Query("""
        update Contract c 
        set c.status = 0
        where c.endDate < CURRENT_TIMESTAMP
          and c.status = 1
    """)
    int updateExpiredContracts();

    Optional<Contract> findByRoomAndPackagePlan_IdAndStatus(Room room, Long packageId, Integer status);
}