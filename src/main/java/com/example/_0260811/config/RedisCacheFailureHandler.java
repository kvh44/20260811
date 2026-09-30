package com.example._0260811.config;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

public class RedisCacheFailureHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheFailureHandler.class);
    private final AtomicBoolean unavailable = new AtomicBoolean();

    boolean isUnavailable() {
        return unavailable.get();
    }

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        handleRedisFailure(exception, cache, "read");
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        handleRedisFailure(exception, cache, "write");
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        handleRedisFailure(exception, cache, "eviction");
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        handleRedisFailure(exception, cache, "clear");
    }

    private void handleRedisFailure(RuntimeException exception, Cache cache, String operation) {
        if (!(exception instanceof RedisConnectionFailureException)
                && !(exception instanceof RedisSystemException)) {
            throw exception;
        }
        if (unavailable.compareAndSet(false, true)) {
            log.warn("Redis cache failed during {} for '{}'; bypassing Redis until restart and continuing with MySQL: {}",
                    operation, cache.getName(), exception.getMessage());
        }
    }
}
