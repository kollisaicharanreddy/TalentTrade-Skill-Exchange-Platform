package com.talenttrade.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Initializing Custom Logging Redis Cache Manager...");
        
        // Default configuration: 10 minutes TTL, String keys, JSON values
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 1. Dashboard statistics
        cacheConfigurations.put("dashboardStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 2. User profile
        cacheConfigurations.put("userProfiles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 3. Platform analytics / Dashboard summary (popular skills, top mentors, etc.)
        cacheConfigurations.put("platformAnalytics", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("dashboardSummary", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 4. Match results
        cacheConfigurations.put("matchResults", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 5. Notification counts
        cacheConfigurations.put("notificationCounts", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        return new RedisCacheManager(cacheWriter, defaultConfig, cacheConfigurations) {
            @Override
            protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
                return new RedisCache(name, cacheWriter, cacheConfig) {
                    @Override
                    protected Object lookup(Object key) {
                        Object value = super.lookup(key);
                        if (value != null) {
                            log.info("[CACHE HIT] Cache: '{}', Key: '{}'", getName(), key);
                        } else {
                            log.info("[CACHE MISS] Cache: '{}', Key: '{}'", getName(), key);
                        }
                        return value;
                    }

                    @Override
                    public void put(Object key, Object value) {
                        log.info("[CACHE PUT] Cache: '{}', Key: '{}'", getName(), key);
                        super.put(key, value);
                    }

                    @Override
                    public void evict(Object key) {
                        log.info("[CACHE EVICT] Cache: '{}', Key: '{}'", getName(), key);
                        super.evict(key);
                    }

                    @Override
                    public void clear() {
                        log.info("[CACHE CLEAR] Cache: '{}'", getName());
                        super.clear();
                    }
                };
            }
        };
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("[REDIS ERROR] Cache GET failed for cache {} with key {}. Falling back to database. Error: {}", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("[REDIS ERROR] Cache PUT failed for cache {} with key {}. Error: {}", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("[REDIS ERROR] Cache EVICT failed for cache {} with key {}. Error: {}", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("[REDIS ERROR] Cache CLEAR failed for cache {}. Error: {}", 
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
