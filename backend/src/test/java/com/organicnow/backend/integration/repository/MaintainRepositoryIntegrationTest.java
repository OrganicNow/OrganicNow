package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.Maintain;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.repository.MaintainRepository;
import com.organicnow.backend.repository.RoomRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MaintainRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private MaintainRepository maintainRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room room;

    @BeforeEach
    void setup() {
        // Clean up all maintain tasks first
        maintainRepository.deleteAll();
        maintainRepository.flush();

        // Ensure that the room exists or create it
        Optional<Room> existingRoom = roomRepository.findByRoomNumber("101");
        if (existingRoom.isPresent()) {
            room = existingRoom.get();
        } else {
            room = new Room();
            room.setRoomNumber("101");
            room.setRoomFloor(1);
            room.setRoomSize(1);
            room = roomRepository.save(room);
        }

        // Clean up any existing maintain tasks for the room
        maintainRepository.deleteAllByRoomId(room.getId());
        maintainRepository.flush();
    }

    @Test
    @Transactional
    @DisplayName("existsActiveMaintainByRoomId(): should return true if there is an active maintenance task")
    void testExistsActiveMaintainByRoomId() {
        // Create a new maintenance task that is not completed (finishDate = null)
        Maintain m = new Maintain();
        m.setIssueTitle("Door broken");
        m.setScheduledDate(LocalDateTime.now());
        m.setFinishDate(null);
        m.setRoom(room);
        m.setCreateDate(LocalDateTime.now());
        m.setIssueCategory(3);
        m.setTargetType(1);
        maintainRepository.save(m);
        maintainRepository.flush();

        boolean exists = maintainRepository.existsActiveMaintainByRoomId(room.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("existsActiveMaintainByRoomId(): should return false if all maintenance tasks are completed")
    void testExistsActiveMaintainByRoomId_NoActive() {
        // Create a completed maintenance task (finishDate != null)
        Maintain m = new Maintain();
        m.setIssueTitle("Light fix");
        m.setScheduledDate(LocalDateTime.now());
        m.setFinishDate(LocalDateTime.now());
        m.setRoom(room);
        m.setCreateDate(LocalDateTime.now());
        m.setIssueCategory(4);
        m.setTargetType(0);
        maintainRepository.save(m);
        maintainRepository.flush();

        boolean exists = maintainRepository.existsActiveMaintainByRoomId(room.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("deleteAllByRoomId(): should delete all maintenance tasks for the given room")
    void testDeleteAllByRoomId() {
        // Create and save multiple maintenance tasks
        Maintain m1 = new Maintain();
        m1.setIssueTitle("To delete");
        m1.setScheduledDate(LocalDateTime.now());
        m1.setRoom(room);
        m1.setCreateDate(LocalDateTime.now());
        m1.setIssueCategory(2);
        m1.setTargetType(0);
        maintainRepository.save(m1);

        Maintain m2 = new Maintain();
        m2.setIssueTitle("Another task");
        m2.setScheduledDate(LocalDateTime.now().plusDays(1));
        m2.setRoom(room);
        m2.setCreateDate(LocalDateTime.now());
        m2.setIssueCategory(1);
        m2.setTargetType(0);
        maintainRepository.save(m2);

        maintainRepository.flush();

        // Check that the data exists before deletion
        List<Maintain> beforeDelete = maintainRepository.findAll();
        assertThat(beforeDelete).hasSize(2);

        // Delete all maintenance tasks for this room
        maintainRepository.deleteAllByRoomId(room.getId());
        maintainRepository.flush();

        // Verify that no maintenance tasks remain
        List<Maintain> afterDelete = maintainRepository.findAll();
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("findRequestsByRoomId(): should return tasks ordered by scheduledDate DESC")
    void testFindRequestsByRoomId() {
        // Create and save two maintenance tasks
        Maintain m1 = new Maintain();
        m1.setIssueTitle("Water leak");
        m1.setScheduledDate(LocalDateTime.of(2024, 1, 10, 14, 30, 0, 0));
        m1.setFinishDate(null);
        m1.setRoom(room);
        m1.setCreateDate(LocalDateTime.now());
        m1.setIssueCategory(2);
        m1.setTargetType(0);
        maintainRepository.save(m1);
        maintainRepository.flush();

        Maintain m2 = new Maintain();
        m2.setIssueTitle("Aircon broken");
        m2.setScheduledDate(LocalDateTime.of(2024, 2, 1, 9, 0, 0, 0));
        m2.setFinishDate(null);
        m2.setRoom(room);
        m2.setCreateDate(LocalDateTime.now());
        m2.setIssueCategory(1);
        m2.setTargetType(0);
        maintainRepository.save(m2);
        maintainRepository.flush();

        List<RequestDto> results = maintainRepository.findRequestsByRoomId(room.getId());
        assertThat(results).hasSize(2);

        // Ensure tasks are ordered by scheduledDate DESC
        assertThat(results.get(0).getIssueTitle()).isEqualTo("Aircon broken");
        assertThat(results.get(1).getIssueTitle()).isEqualTo("Water leak");
    }
}