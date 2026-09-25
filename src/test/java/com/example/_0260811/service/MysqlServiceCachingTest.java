package com.example._0260811.service;

import com.example._0260811.model.MysqlClient;
import com.example._0260811.repository.MysqlClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class MysqlServiceCachingTest {

    @Autowired
    private MysqlService mysqlService;

    @Autowired
    private MysqlClientRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache("mysqlClient").clear();
        cacheManager.getCache("mysqlClientList").clear();
        reset(repository);
    }

    @Test
    void cachesAllClientsWithLiteralAllKey() {
        List<MysqlClient> clients = List.of(MysqlClient.builder().id(1L).build());
        when(repository.findAll()).thenReturn(clients);

        assertEquals(clients, mysqlService.getAllMysqlClients());
        assertEquals(clients, mysqlService.getAllMysqlClients());

        verify(repository, times(1)).findAll();
        Cache cache = cacheManager.getCache("mysqlClientList");
        assertNotNull(cache);
        assertEquals(clients, cache.get("all").get());
        assertNotNull(new JdkSerializationRedisSerializer().serialize(clients));
    }

    @Test
    void deletingOneClientEvictsTheAllClientsEntry() {
        when(repository.findAll()).thenReturn(List.of());

        mysqlService.getAllMysqlClients();
        mysqlService.deleteMysqlClientById(1L);
        mysqlService.getAllMysqlClients();

        verify(repository, times(1)).findAll();
        verify(repository).deleteById(1L);
    }

    @Test
    void deletingAllClientsEvictsBothCaches() {
        MysqlClient client = MysqlClient.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(client));
        when(repository.findAll()).thenReturn(List.of(client));

        mysqlService.getMysqlClientById(1L);
        mysqlService.getAllMysqlClients();
        mysqlService.deleteAllMysqlClients();
        mysqlService.getMysqlClientById(1L);
        mysqlService.getAllMysqlClients();

        verify(repository, times(2)).findById(1L);
        verify(repository, times(2)).findAll();
        verify(repository).deleteAll();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class TestConfig {

        @Bean
        MysqlClientRepository mysqlClientRepository() {
            return mock(MysqlClientRepository.class);
        }

        @Bean
        MysqlService mysqlService(MysqlClientRepository repository) {
            return new MysqlServiceImpl(repository);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("mysqlClient", "mysqlClientList");
        }
    }
}
