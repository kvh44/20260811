package com.example._0260811.controller;

import com.example._0260811.model.MysqlClient;
import com.example._0260811.service.MysqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mysql")
@RequiredArgsConstructor
@Tag(name = "MySQL clients", description = "Client records stored in MySQL")
public class MysqlController {
    private final MysqlService mysqlService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a MySQL client", description = "Returns the MySQL client with the requested ID.")
    public MysqlClient getMysqlClientById(@PathVariable Long id) {
        return mysqlService.getMysqlClientById(id);
    }

    @GetMapping("/all")
    @Operation(summary = "List MySQL clients", description = "Returns all client records stored in MySQL.")
    public List<MysqlClient> getAllMysqlClients() {
        return mysqlService.getAllMysqlClients();
    }

    @PostMapping
    @Operation(summary = "Create a new MySQL client", description = "Creates a new MySQL client record.")
    public void createMysqlClient(@RequestBody MysqlClient mysqlClient) {
        mysqlService.saveMysqlClient(mysqlClient);
    }
}
