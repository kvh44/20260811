package com.example._0260811.controller;

import com.example._0260811.model.MongoClient;
import com.example._0260811.service.MongoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mongo")
@RequiredArgsConstructor
public class MongoController {
    private final MongoService mongoService;

    @GetMapping("/all")
    public List<MongoClient> findAll() {
        return mongoService.getAllMongoClients();
    }

}
