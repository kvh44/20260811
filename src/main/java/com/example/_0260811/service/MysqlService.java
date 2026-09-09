package com.example._0260811.service;

import com.example._0260811.model.MysqlClient;

import java.util.List;

public interface MysqlService {
    MysqlClient getMysqlClientById(long id);
    List<MysqlClient> getAllMysqlClients();
}
