package com.example._0260811.service;

import com.example._0260811.model.Dockerclient;

import java.util.List;

public interface MysqlService {
    Dockerclient getDockerclientById(long id);
    List<Dockerclient> getAllDockerclients();
}
