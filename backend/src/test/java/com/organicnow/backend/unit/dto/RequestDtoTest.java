package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.RequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtoTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testDefaultConstructorAndSetters() {
        RequestDto dto = new RequestDto();

        dto.setId(1L);
        dto.setIssueTitle("Broken light");
        dto.setIssueDescription("Light not working in bathroom");
        dto.setCreateDate(LocalDateTime.of(2025, 1, 10, 12, 0));
        dto.setScheduledDate(LocalDateTime.of(2025, 1, 12, 9, 0));
        dto.setFinishDate(LocalDateTime.of(2025, 1, 15, 18, 0));

        assertEquals(1L, dto.getId());
        assertEquals("Broken light", dto.getIssueTitle());
        assertEquals("Light not working in bathroom", dto.getIssueDescription());
        assertEquals(LocalDateTime.of(2025, 1, 10, 12, 0), dto.getCreateDate());
        assertEquals(LocalDateTime.of(2025, 1, 12, 9, 0), dto.getScheduledDate());
        assertEquals(LocalDateTime.of(2025, 1, 15, 18, 0), dto.getFinishDate());
    }

    @Test
    void testFullConstructor() {
        LocalDateTime c = LocalDateTime.now();
        LocalDateTime s = LocalDateTime.now().plusDays(2);
        LocalDateTime f = LocalDateTime.now().plusDays(5);

        RequestDto dto = new RequestDto(
                10L,
                "Aircon issue",
                "Not cold",
                c, s, f
        );

        assertEquals(10L, dto.getId());
        assertEquals("Aircon issue", dto.getIssueTitle());
        assertEquals("Not cold", dto.getIssueDescription());
        assertEquals(c, dto.getCreateDate());
        assertEquals(s, dto.getScheduledDate());
        assertEquals(f, dto.getFinishDate());
    }

    @Test
    void testRepositoryConstructor() {
        LocalDateTime s = LocalDateTime.now();
        LocalDateTime f = LocalDateTime.now().plusDays(3);

        RequestDto dto = new RequestDto(
                50L,
                "Water leak",
                s,
                f
        );

        assertEquals(50L, dto.getId());
        assertEquals("Water leak", dto.getIssueTitle());
        assertEquals(s, dto.getScheduledDate());
        assertEquals(f, dto.getFinishDate());

        // Fields not included in constructor should remain null
        assertNull(dto.getIssueDescription());
        assertNull(dto.getCreateDate());
    }

    @Test
    void testJsonSerialization() throws Exception {
        RequestDto dto = new RequestDto(
                1L,
                "Test issue",
                "Something happened",
                LocalDateTime.of(2025, 1, 11, 8, 0),
                LocalDateTime.of(2025, 1, 13, 10, 0),
                LocalDateTime.of(2025, 1, 20, 15, 0)
        );

        String json = mapper.writeValueAsString(dto);
        assertNotNull(json);

        RequestDto deserialized = mapper.readValue(json, RequestDto.class);
        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getIssueTitle(), deserialized.getIssueTitle());
        assertEquals(dto.getIssueDescription(), deserialized.getIssueDescription());
        assertEquals(dto.getCreateDate(), deserialized.getCreateDate());
        assertEquals(dto.getScheduledDate(), deserialized.getScheduledDate());
        assertEquals(dto.getFinishDate(), deserialized.getFinishDate());
    }

    @Test
    void testFieldEqualityInsteadOfEquals() {
        LocalDateTime now = LocalDateTime.now();

        RequestDto dto1 = new RequestDto(1L, "Fan broken", "Not spinning",
                now, now.plusHours(1), now.plusHours(3));

        RequestDto dto2 = new RequestDto(1L, "Fan broken", "Not spinning",
                now, now.plusHours(1), now.plusHours(3));

        assertEquals(dto1.getId(), dto2.getId());
        assertEquals(dto1.getIssueTitle(), dto2.getIssueTitle());
        assertEquals(dto1.getIssueDescription(), dto2.getIssueDescription());
        assertEquals(dto1.getCreateDate(), dto2.getCreateDate());
        assertEquals(dto1.getScheduledDate(), dto2.getScheduledDate());
        assertEquals(dto1.getFinishDate(), dto2.getFinishDate());
    }

}
