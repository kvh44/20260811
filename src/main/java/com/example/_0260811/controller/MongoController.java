package com.example._0260811.controller;

import com.example._0260811.model.MongoClient;
import com.example._0260811.service.MongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mongo")
@RequiredArgsConstructor
@Tag(name = "MongoDB clients", description = "Client records stored in MongoDB")
public class MongoController {
    private final MongoService mongoService;

    @GetMapping("/all")
    @Operation(summary = "List MongoDB clients", description = "Returns all client records stored in MongoDB.")
    public List<MongoClient> findAll() {
        return mongoService.getAllMongoClients();
    }

}
