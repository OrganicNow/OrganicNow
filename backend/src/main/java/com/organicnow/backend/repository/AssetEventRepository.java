package com.organicnow.backend.repository;

import com.organicnow.backend.model.AssetEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetEventRepository extends JpaRepository<AssetEvent, Long> {
    List<AssetEvent> findByRoom_Id(Long roomId);
}