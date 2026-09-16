package com.example._0260811.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleDataNotFoundExceptionReturnsNotFoundResponse() {
        RuntimeException exception = new RuntimeException("Mysql client not found with id: 1");
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleDataNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Data not found", response.getBody().get("error"));
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        verifyNoInteractions(request);
    }
}
