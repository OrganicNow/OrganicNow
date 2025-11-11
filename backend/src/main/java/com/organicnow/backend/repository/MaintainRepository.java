package com.organicnow.backend.repository;

import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.model.Maintain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

public interface MaintainRepository extends JpaRepository<Maintain, Long> {

    // ✅ ดึงรายการ Maintain (Request) ทั้งหมดในห้องนั้น
    @Query("""
        SELECT new com.organicnow.backend.dto.RequestDto(
            m.id, m.issueTitle, m.scheduledDate, m.finishDate
        )
        FROM Maintain m
        WHERE m.room.id = :roomId
        ORDER BY m.scheduledDate DESC
    """)
    List<RequestDto> findRequestsByRoomId(@Param("roomId") Long roomId);

    // ✅ ใช้สำหรับ Dashboard: เช็กว่าห้องยังมีงานซ่อมที่ยังไม่เสร็จ
    @Query("""
        select case when count(m) > 0 then true else false end
        from Maintain m
        where m.room.id = :roomId
          and m.finishDate is null
    """)
    boolean existsActiveMaintainByRoomId(Long roomId);

    // ✅ Dashboard: นับจำนวน maintain ต่อเดือน (12 เดือนล่าสุด)
    @Query(value = """
        SELECT to_char(m.create_date, 'YYYY-MM') AS month,
               COUNT(m.maintain_id) AS total
        FROM maintain m
        WHERE m.create_date >= date_trunc('month', CURRENT_DATE) - INTERVAL '11 months'
        GROUP BY to_char(m.create_date, 'YYYY-MM')
        ORDER BY month
    """, nativeQuery = true)
    List<Object[]> countRequestsLast12Months();

    @Modifying
    @Query("DELETE FROM Maintain m WHERE m.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);

}