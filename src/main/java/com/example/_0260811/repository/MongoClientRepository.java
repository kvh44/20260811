package com.example._0260811.repository;

import com.example._0260811.model.MongoClient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoClientRepository extends MongoRepository<MongoClient, String> {
}
