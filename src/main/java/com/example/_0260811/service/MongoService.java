package com.example._0260811.service;

import com.example._0260811.model.MongoClient;

import java.util.List;

public interface MongoService {
    public MongoClient save(MongoClient mongoClient);
    List<MongoClient> getAllMongoClients();
}
