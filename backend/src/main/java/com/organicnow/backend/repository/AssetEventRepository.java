package com.organicnow.backend.repository;

import com.organicnow.backend.model.AssetEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetEventRepository extends JpaRepository<AssetEvent, Long> {

    // ✅ ใช้ดึง event ทั้งหมดตาม room id
    List<AssetEvent> findByRoom_Id(Long roomId);

    // ✅ เพิ่มบรรทัดนี้ เพื่อให้ RoomService ใช้ลบ event ได้
    void deleteByRoom_Id(Long roomId);
}