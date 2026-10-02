package com.example._0260811.service;

import com.example._0260811.model.MysqlClient;
import com.example._0260811.repository.MysqlClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MysqlServiceImpl implements MysqlService {

    final private MysqlClientRepository mysqlClientRepository;
    final private KafkaProducer kafkaProducer;

    @Override
    @Cacheable(value = "mysqlClient", key = "#id")
    public MysqlClient getMysqlClientById(long id) {
        return mysqlClientRepository.findById(id).orElseThrow(() -> new RuntimeException("Mysql client not found with id: " + id));
    }

    @Override
    @Cacheable(value = "mysqlClientList", key = "'all'")
    public List<MysqlClient> getAllMysqlClients() {
        return mysqlClientRepository.findAll();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "mysqlClient", key = "#id"),
    })
    public void deleteMysqlClientById(long id) {
        mysqlClientRepository.deleteById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "mysqlClient", allEntries = true),
            @CacheEvict(value = "mysqlClientList", key = "'all'")
    })
    public void deleteAllMysqlClients() {
        mysqlClientRepository.deleteAll();
    }

    @Override
    public void saveMysqlClient(MysqlClient mysqlClient) {
        mysqlClient = mysqlClientRepository.save(mysqlClient);
        kafkaProducer.sendMessage("Saved MysqlClient with id: " + mysqlClient.getId());
    }
}
