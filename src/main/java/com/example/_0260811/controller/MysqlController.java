package com.example._0260811.controller;

import com.example._0260811.model.Dockerclient;
import com.example._0260811.service.MysqlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/mysql")
@RequiredArgsConstructor
public class MysqlController {
    private final MysqlService mysqlService;

    @GetMapping("/{id}")
    public Dockerclient getDockerclientById(@PathVariable Long id) {
        return mysqlService.getDockerclientById(id);
    }
}
