package com.example._0260811.service;

import com.example._0260811.model.MysqlClient;
import com.example._0260811.repository.MysqlClientRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MysqlServiceImplTest {
    @Mock
    private MysqlClientRepository mysqlClientRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private MysqlServiceImpl mysqlService;

    @Resource
    private final List<MysqlClient> mysqlClients = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> MysqlClient.builder()
                    .id((long) i)
                    .username("mysql-client-" + i)
                    .email("mysql" + i + "@example.com")
                    .telephone("100000000" + i)
                    .build())
            .toList();

    @Test
    public void testGetMysqlClientById() {
        MysqlClient mysqlClient = MysqlClient.builder()
                .id(1L)
                .username("Test mysql Client")
                .email("abc@gmail.com")
                .telephone("1234567890")
                .build();

        when(mysqlClientRepository.findById(1L)).thenReturn(Optional.of(mysqlClient));

        MysqlClient result = mysqlService.getMysqlClientById(1L);

        assertEquals(mysqlClient.getId(), result.getId());
        assertEquals(mysqlClient.getUsername(), result.getUsername());
        assertEquals(mysqlClient.getEmail(), result.getEmail());
        assertEquals(mysqlClient.getTelephone(), result.getTelephone());
    }

    @Test
    public void testGetMysqlClientByIdNotFound() {
        when(mysqlClientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mysqlService.getMysqlClientById(1L));
        assertEquals("Mysql client not found with id: 1", ex.getMessage());
    }

    @Test
    public void testGetAllMysqlClients() {
        when(mysqlClientRepository.findAll()).thenReturn(mysqlClients);

        List<MysqlClient> result = mysqlService.getAllMysqlClients();

        assertEquals(10, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10L, result.get(9).getId());
        assertEquals("mysql-client-5", result.get(4).getUsername());
    }

    @Test
    public void testDeleteMysqlClientById() {
        doNothing().when(mysqlClientRepository).deleteById(1L);
        mysqlService.deleteMysqlClientById(1L);
        verify(mysqlClientRepository).deleteById(1L);
        verifyNoMoreInteractions(mysqlClientRepository, kafkaProducer);
    }

    @Test
    public void testDeleteAllMysqlClients() {
        doNothing().when(mysqlClientRepository).deleteAll();
        mysqlService.deleteAllMysqlClients();
        verify(mysqlClientRepository).deleteAll();
        verifyNoMoreInteractions(mysqlClientRepository, kafkaProducer);
    }

    @Test
    public void testSaveMysqlClient() {
        MysqlClient mysqlClient = MysqlClient.builder()
                .id(1L)
                .username("Test mysql Client")
                .email("abc@gmail.com")
                .telephone("1234567890")
                .build();
        when(mysqlClientRepository.save(mysqlClient)).thenReturn(mysqlClient);
        mysqlService.saveMysqlClient(mysqlClient);
        verify(kafkaProducer).sendMessage("Saved MysqlClient with id: " + mysqlClient.getId());
        verifyNoMoreInteractions(mysqlClientRepository, kafkaProducer);
    }
}