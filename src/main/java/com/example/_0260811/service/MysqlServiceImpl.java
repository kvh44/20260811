package com.example._0260811.service;

import com.example._0260811.model.Dockerclient;
import com.example._0260811.repository.DockerclientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MysqlServiceImpl implements MysqlService {

    final private DockerclientRepository dockerclientRepository;

    @Override
    public Dockerclient getDockerclientById(long id) {
        return dockerclientRepository.findById(id).orElseThrow(() -> new RuntimeException("Docker client not found with id: " + id));
    }

    @Override
    public List<Dockerclient> getAllDockerclients() {
        return dockerclientRepository.findAll();
    }
}
