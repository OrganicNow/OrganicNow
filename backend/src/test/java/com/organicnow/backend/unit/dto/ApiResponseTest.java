package com.organicnow.backend.unit.dto;

import org.junit.jupiter.api.Test;
import com.organicnow.backend.dto.ApiResponse;


import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponseConstructor() {
        // Arrange
        String expectedStatus = "success";
        String expectedResult = "Data Loaded";

        // Act
        ApiResponse<String> apiResponse = new ApiResponse<>(expectedStatus, expectedResult);

        // Assert
        assertEquals(expectedStatus, apiResponse.getStatus());
        assertEquals(expectedResult, apiResponse.getResult());
    }

    @Test
    void testApiResponseSettersAndGetters() {
        // Arrange
        ApiResponse<String> apiResponse = new ApiResponse<>("success", "Initial Result");

        // Act
        apiResponse.setStatus("error");
        apiResponse.setResult("An error occurred");

        // Assert
        assertEquals("error", apiResponse.getStatus());
        assertEquals("An error occurred", apiResponse.getResult());
    }

    @Test
    void testApiResponseWithNullResult() {
        // Arrange
        String expectedStatus = "success";

        // Act
        ApiResponse<String> apiResponse = new ApiResponse<>(expectedStatus, null);

        // Assert
        assertEquals(expectedStatus, apiResponse.getStatus());
        assertNull(apiResponse.getResult());
    }
}
