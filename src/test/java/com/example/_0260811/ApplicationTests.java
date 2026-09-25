package com.example._0260811;

import java.time.Duration;

import io.lettuce.core.ClientOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class ApplicationTests {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private LettuceConnectionFactory redisConnectionFactory;

	@Test
	void contextLoads() {
	}

	@Test
	void redisListCacheUsesTheRawKey() {
		RedisCache listCache = assertInstanceOf(RedisCache.class, cacheManager.getCache("mysqlClientList"));
		RedisCache clientCache = assertInstanceOf(RedisCache.class, cacheManager.getCache("mysqlClient"));

		assertFalse(listCache.getCacheConfiguration().usePrefix());
		assertTrue(clientCache.getCacheConfiguration().usePrefix());
	}

	@Test
	void redisConnectionUsesTimeoutsWithoutAutomaticReconnect() {
		var clientConfiguration = redisConnectionFactory.getClientConfiguration();
		ClientOptions clientOptions = clientConfiguration.getClientOptions().orElseThrow();

		assertEquals(Duration.ofSeconds(2), clientConfiguration.getCommandTimeout());
		assertEquals(Duration.ofSeconds(2), clientOptions.getSocketOptions().getConnectTimeout());
		assertFalse(clientOptions.isAutoReconnect());
		assertEquals(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS,
				clientOptions.getDisconnectedBehavior());
	}

}
