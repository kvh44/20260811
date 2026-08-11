package com.example._0260811.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Page not found");
        body.put("status", HttpStatus.NOT_FOUND.value());
        String path = request != null ? request.getRequestURI() : ex.getRequestURL();
        body.put("path", path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
