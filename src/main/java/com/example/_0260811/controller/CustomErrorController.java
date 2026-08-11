package com.example._0260811.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/error")
public class CustomErrorController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = statusObj instanceof Integer ? (Integer) statusObj : null;

        if (statusCode != null && statusCode == HttpStatus.NOT_FOUND.value()) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Page not found");
            body.put("status", statusCode);
            Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            if (uri != null) body.put("path", uri.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unexpected error");
        body.put("status", statusCode == null ? 500 : statusCode);
        return ResponseEntity.status(statusCode == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(statusCode)).body(body);
    }
}
