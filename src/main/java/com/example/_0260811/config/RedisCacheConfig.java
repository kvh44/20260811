package com.example._0260811.config;

import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RedisCacheConfig implements CachingConfigurer {

    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final RedisCacheFailureHandler redisErrorHandler = new RedisCacheFailureHandler();
    private final CacheResolver noOpCacheResolver = new SimpleCacheResolver(new NoOpCacheManager());

    public RedisCacheConfig(ObjectProvider<CacheManager> cacheManagerProvider) {
        this.cacheManagerProvider = cacheManagerProvider;
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return redisErrorHandler;
    }

    // Keep this out of the bean registry so Boot still auto-configures the Redis CacheManager.
    @Override
    public CacheResolver cacheResolver() {
        return context -> {
            if (redisErrorHandler.isUnavailable()) {
                return noOpCacheResolver.resolveCaches(context);
            }
            return new SimpleCacheResolver(cacheManagerProvider.getObject()).resolveCaches(context);
        };
    }

    @Bean
    RedisCacheManagerBuilderCustomizer mysqlClientListCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "mysqlClientList", builder.cacheDefaults().disableKeyPrefix());
    }

    @Bean
    LettuceClientOptionsBuilderCustomizer disableRedisReconnect() {
        return builder -> builder.autoReconnect(false)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
    }
}
