package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.repository.MaintainRepository;
import com.organicnow.backend.service.MaintainRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaintainRoomService
 */
@ExtendWith(MockitoExtension.class)
class MaintainRoomServiceTest {

    @Mock
    private MaintainRepository maintainRepository;

    @InjectMocks
    private MaintainRoomService maintainRoomService;

    @Test
    void getRequestsByRoomId_shouldReturnListFromRepository() {
        // Arrange
        Long roomId = 1L;

        // ใช้ mock RequestDto เพื่อไม่ต้องรู้ constructor ที่แท้จริง
        RequestDto dto1 = mock(RequestDto.class);
        RequestDto dto2 = mock(RequestDto.class);

        List<RequestDto> repoResult = List.of(dto1, dto2);

        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenReturn(repoResult);

        // Act
        List<RequestDto> result = maintainRoomService.getRequestsByRoomId(roomId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(repoResult, result); // list เดียวกันเป๊ะ
        verify(maintainRepository, times(1)).findRequestsByRoomId(roomId);
        verifyNoMoreInteractions(maintainRepository);
    }

    @Test
    void getRequestsByRoomId_whenNoData_shouldReturnEmptyList() {
        // Arrange
        Long roomId = 999L;

        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenReturn(List.of()); // empty list

        // Act
        List<RequestDto> result = maintainRoomService.getRequestsByRoomId(roomId);

        // Assert
        assertEquals(0, result.size());
        verify(maintainRepository, times(1)).findRequestsByRoomId(roomId);
        verifyNoMoreInteractions(maintainRepository);
    }

    @Test
    void getRequestsByRoomId_whenRepositoryThrows_shouldPropagateException() {
        // Arrange
        Long roomId = 1L;

        RuntimeException ex = new RuntimeException("DB error");
        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenThrow(ex);

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> maintainRoomService.getRequestsByRoomId(roomId)
        );
        assertEquals("DB error", thrown.getMessage());

        verify(maintainRepository, times(1)).findRequestsByRoomId(roomId);
        verifyNoMoreInteractions(maintainRepository);
    }
}
