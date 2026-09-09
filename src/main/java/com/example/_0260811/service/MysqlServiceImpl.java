package com.example._0260811.service;

import com.example._0260811.model.MysqlClient;
import com.example._0260811.repository.MysqlClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MysqlServiceImpl implements MysqlService {

    final private MysqlClientRepository mysqlClientRepository;

    @Override
    public MysqlClient getMysqlClientById(long id) {
        return mysqlClientRepository.findById(id).orElseThrow(() -> new RuntimeException("Mysql client not found with id: " + id));
    }

    @Override
    public List<MysqlClient> getAllMysqlClients() {
        return mysqlClientRepository.findAll();
    }
}
