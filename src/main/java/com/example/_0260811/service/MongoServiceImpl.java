package com.example._0260811.service;

import com.example._0260811.model.MongoClient;
import com.example._0260811.repository.MongoClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoServiceImpl implements MongoService {

    private final MongoClientRepository mongoClientRepository;
    @Override
    public MongoClient save(MongoClient mongoClient) {
        return mongoClientRepository.save(mongoClient);
    }

    @Override
    public List<MongoClient> getAllMongoClients() {
        return mongoClientRepository.findAll();
    }
}
