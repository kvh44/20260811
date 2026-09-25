package com.example._0260811.service;

import com.example._0260811.config.RedisCacheConfig;
import com.example._0260811.model.MysqlClient;
import com.example._0260811.repository.MysqlClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringJUnitConfig({RedisCacheConfig.class, MysqlServiceRedisFallbackTest.TestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MysqlServiceRedisFallbackTest {

    @Autowired
    private MysqlService mysqlService;

    @Autowired
    private MysqlClientRepository repository;

    @Autowired
    private Cache cache;

    @BeforeEach
    void makeRedisUnavailable() {
        reset(repository, cache);
        when(cache.getName()).thenReturn("redis");
        RedisConnectionFailureException failure = new RedisConnectionFailureException("Redis unavailable");
        doThrow(failure).when(cache).get(any());
        doThrow(failure).when(cache).put(any(), any());
        doThrow(failure).when(cache).evict(any());
        doThrow(failure).when(cache).clear();
    }

    @Test
    void readsClientByIdFromMysqlWhenRedisGetAndPutFail() {
        MysqlClient client = MysqlClient.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(client));

        assertSame(client, mysqlService.getMysqlClientById(1L));
        assertSame(client, mysqlService.getMysqlClientById(1L));

        verify(repository, times(2)).findById(1L);
        verify(cache, times(1)).get(1L);
    }

    @Test
    void readsAllClientsFromMysqlWhenRedisGetAndPutFail() {
        List<MysqlClient> clients = List.of(MysqlClient.builder().id(1L).build());
        when(repository.findAll()).thenReturn(clients);

        assertEquals(clients, mysqlService.getAllMysqlClients());
        assertEquals(clients, mysqlService.getAllMysqlClients());

        verify(repository, times(2)).findAll();
        verify(cache, times(1)).get("all");
    }

    @Test
    void bypassesRedisAfterCachePutFailure() {
        List<MysqlClient> clients = List.of(MysqlClient.builder().id(1L).build());
        doReturn(null).when(cache).get(any());
        when(repository.findAll()).thenReturn(clients);

        assertEquals(clients, mysqlService.getAllMysqlClients());
        assertEquals(clients, mysqlService.getAllMysqlClients());

        verify(repository, times(2)).findAll();
        verify(cache, times(1)).get("all");
    }

    @Test
    void deletesOneClientFromMysqlWhenRedisEvictionFails() {
        mysqlService.deleteMysqlClientById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deletesAllClientsFromMysqlWhenRedisClearFails() {
        when(repository.findAll()).thenReturn(List.of());

        mysqlService.deleteAllMysqlClients();
        mysqlService.getAllMysqlClients();

        verify(repository).deleteAll();
        verify(repository).findAll();
        verify(cache, never()).get(any());
    }

    @Test
    void propagatesMysqlErrors() {
        IllegalStateException mysqlFailure = new IllegalStateException("MySQL unavailable");
        when(repository.findAll()).thenThrow(mysqlFailure);

        assertSame(mysqlFailure, assertThrows(IllegalStateException.class, mysqlService::getAllMysqlClients));
    }

    @Test
    void propagatesOtherCacheErrors() {
        IllegalStateException cacheFailure = new IllegalStateException("Unexpected cache error");
        doThrow(cacheFailure).when(cache).get(any());

        assertSame(cacheFailure, assertThrows(IllegalStateException.class, mysqlService::getAllMysqlClients));
        verifyNoInteractions(repository);
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
        Cache cache() {
            return mock(Cache.class);
        }

        @Bean
        CacheManager cacheManager(Cache cache) {
            CacheManager manager = mock(CacheManager.class);
            when(manager.getCache(anyString())).thenReturn(cache);
            return manager;
        }
    }
}
