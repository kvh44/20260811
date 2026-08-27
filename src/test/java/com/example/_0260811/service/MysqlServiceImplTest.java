package com.example._0260811.service;

import com.example._0260811.model.Dockerclient;
import com.example._0260811.repository.DockerclientRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MysqlServiceImplTest {
    @Mock
    private DockerclientRepository dockerclientRepository;

    @InjectMocks
    private MysqlServiceImpl mysqlService;

    @Resource
    private final List<Dockerclient> dockerclients = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> Dockerclient.builder()
                    .id((long) i)
                    .username("docker-client-" + i)
                    .email("docker" + i + "@example.com")
                    .telephone("100000000" + i)
                    .build())
            .toList();

    @Test
    public void testGetDockerclientById() {
        Dockerclient dockerclient = Dockerclient.builder()
                .id(1L)
                .username("Test Docker Client")
                .email("abc@gmail.com")
                .telephone("1234567890")
                .build();

        when(dockerclientRepository.findById(1L)).thenReturn(Optional.of(dockerclient));

        Dockerclient result = mysqlService.getDockerclientById(1L);

        assertEquals(dockerclient.getId(), result.getId());
        assertEquals(dockerclient.getUsername(), result.getUsername());
        assertEquals(dockerclient.getEmail(), result.getEmail());
        assertEquals(dockerclient.getTelephone(), result.getTelephone());
    }

    @Test
    public void testGetDockerclientByIdNotFound() {
        when(dockerclientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mysqlService.getDockerclientById(1L));
        assertEquals("Docker client not found with id: 1", ex.getMessage());
    }

    @Test
    public void testGetAllDockerclients() {
        when(dockerclientRepository.findAll()).thenReturn(dockerclients);

        List<Dockerclient> result = mysqlService.getAllDockerclients();

        assertEquals(10, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10L, result.get(9).getId());
        assertEquals("docker-client-5", result.get(4).getUsername());
    }
}