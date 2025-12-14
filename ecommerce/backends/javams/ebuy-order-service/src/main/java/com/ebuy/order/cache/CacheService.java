package com.ebuy.order.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * High-performance cache service for order item operations.
 * Provides Redis-based caching with fallback to local cache for high availability.
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    // Cache names
    public static final String ORDER_ITEMS_CACHE = "orderItems";
    public static final String ORDER_ITEMS_BY_ORDER_CACHE = "orderItemsByOrder";
    public static final String ORDER_VALIDATION_CACHE = "orderValidation";
    public static final String PRODUCT_VALIDATION_CACHE = "productValidation";
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";

    @Value("${ebuy.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${ebuy.cache.redis.enabled:true}")
    private boolean redisEnabled;

    @Value("${ebuy.cache.default-ttl:300}")
    private Duration defaultTtl;

    @Value("${ebuy.cache.validation-ttl:600}")
    private Duration validationTtl;

    @Value("${ebuy.cache.permissions-ttl:180}")
    private Duration permissionsTtl;

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public CacheService(CacheManager cacheManager,
                        RedisTemplate<String, Object> redisTemplate,
                        StringRedisTemplate stringRedisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Stores a value in cache with default TTL.
     */
    public void put(String cacheName, Object key, Object value) {
        put(cacheName, key, value, defaultTtl);
    }

    /**
     * Stores a value in cache with specified TTL.
     */
    public void put(String cacheName, Object key, Object value, Duration ttl) {
        if (!cacheEnabled) {
            return;
        }

        logger.debug("Caching value with key: {} in cache: {}", key, cacheName);

        try {
            if (redisEnabled) {
                String redisKey = buildRedisKey(cacheName, key);
                redisTemplate.opsForValue().set(redisKey, value, ttl);
            }

            // Also store in local cache as fallback
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

        } catch (Exception e) {
            logger.warn("Error caching value with key: {} in cache: {}", key, cacheName, e);
            // Continue execution even if caching fails
        }
    }

    /**
     * Retrieves a value from cache.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key, Class<T> type) {
        if (!cacheEnabled) {
            return null;
        }

        logger.debug("Retrieving cached value with key: {} from cache: {}", key, cacheName);

        try {
            // Try Redis first
            if (redisEnabled) {
                String redisKey = buildRedisKey(cacheName, key);
                Object value = redisTemplate.opsForValue().get(redisKey);
                if (value != null) {
                    logger.debug("Cache hit in Redis for key: {}", key);
                    return type.cast(value);
                }
            }

            // Fallback to local cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    logger.debug("Cache hit in local cache for key: {}", key);
                    return type.cast(wrapper.get());
                }
            }

            logger.debug("Cache miss for key: {} in cache: {}", key, cacheName);
            return null;

        } catch (Exception e) {
            logger.warn("Error retrieving cached value with key: {} from cache: {}", key, cacheName, e);
            return null;
        }
    }

    /**
     * Removes a specific key from cache.
     */
    public void evict(String cacheName, Object key) {
        if (!cacheEnabled) {
            return;
        }

        logger.debug("Evicting cache entry with key: {} from cache: {}", key, cacheName);

        try {
            // Remove from Redis
            if (redisEnabled) {
                String redisKey = buildRedisKey(cacheName, key);
                redisTemplate.delete(redisKey);
            }

            // Remove from local cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }

        } catch (Exception e) {
            logger.warn("Error evicting cache entry with key: {} from cache: {}", key, cacheName, e);
        }
    }

    /**
     * Clears all entries from a specific cache.
     */
    public void evictAll(String cacheName) {
        if (!cacheEnabled) {
            return;
        }

        logger.info("Evicting all entries from cache: {}", cacheName);

        try {
            // Clear Redis entries with pattern
            if (redisEnabled) {
                String pattern = buildRedisKey(cacheName, "*");
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    logger.info("Cleared {} Redis cache entries for cache: {}", keys.size(), cacheName);
                }
            }

            // Clear local cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Cleared local cache: {}", cacheName);
            }

        } catch (Exception e) {
            logger.warn("Error clearing cache: {}", cacheName, e);
        }
    }

    /**
     * Checks if a key exists in cache.
     */
    public boolean exists(String cacheName, Object key) {
        if (!cacheEnabled) {
            return false;
        }

        try {
            // Check Redis first
            if (redisEnabled) {
                String redisKey = buildRedisKey(cacheName, key);
                return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
            }

            // Check local cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }

            return false;

        } catch (Exception e) {
            logger.warn("Error checking cache existence for key: {} in cache: {}", key, cacheName, e);
            return false;
        }
    }

    /**
     * Sets expiration time for a cached key.
     */
    public void expire(String cacheName, Object key, Duration ttl) {
        if (!cacheEnabled || !redisEnabled) {
            return;
        }

        logger.debug("Setting expiration for key: {} in cache: {} to {} seconds",
                key, cacheName, ttl.getSeconds());

        try {
            String redisKey = buildRedisKey(cacheName, key);
            redisTemplate.expire(redisKey, ttl);

        } catch (Exception e) {
            logger.warn("Error setting expiration for key: {} in cache: {}", key, cacheName, e);
        }
    }

    /**
     * Gets the remaining TTL for a cached key.
     */
    public Duration getTtl(String cacheName, Object key) {
        if (!cacheEnabled || !redisEnabled) {
            return Duration.ZERO;
        }

        try {
            String redisKey = buildRedisKey(cacheName, key);
            Long ttlSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            if (ttlSeconds != null && ttlSeconds > 0) {
                return Duration.ofSeconds(ttlSeconds);
            }

            return Duration.ZERO;

        } catch (Exception e) {
            logger.warn("Error getting TTL for key: {} in cache: {}", key, cacheName, e);
            return Duration.ZERO;
        }
    }

    /**
     * Increments a numeric value in cache.
     */
    public Long increment(String cacheName, Object key) {
        return increment(cacheName, key, 1L);
    }

    /**
     * Increments a numeric value in cache by specified amount.
     */
    public Long increment(String cacheName, Object key, long delta) {
        if (!cacheEnabled || !redisEnabled) {
            return 0L;
        }

        logger.debug("Incrementing cache value for key: {} in cache: {} by {}", key, cacheName, delta);

        try {
            String redisKey = buildRedisKey(cacheName, key);
            return redisTemplate.opsForValue().increment(redisKey, delta);

        } catch (Exception e) {
            logger.warn("Error incrementing cache value for key: {} in cache: {}", key, cacheName, e);
            return 0L;
        }
    }

    /**
     * Stores multiple key-value pairs in cache atomically.
     */
    public void multiPut(String cacheName, java.util.Map<Object, Object> keyValuePairs, Duration ttl) {
        if (!cacheEnabled || keyValuePairs.isEmpty()) {
            return;
        }

        logger.debug("Storing {} entries in cache: {}", keyValuePairs.size(), cacheName);

        try {
            if (redisEnabled) {
                // Convert keys to Redis format
                java.util.Map<String, Object> redisEntries = new java.util.HashMap<>();
                for (java.util.Map.Entry<Object, Object> entry : keyValuePairs.entrySet()) {
                    String redisKey = buildRedisKey(cacheName, entry.getKey());
                    redisEntries.put(redisKey, entry.getValue());
                }

                // Store all entries
                redisTemplate.opsForValue().multiSet(redisEntries);

                // Set TTL for each entry
                for (String redisKey : redisEntries.keySet()) {
                    redisTemplate.expire(redisKey, ttl);
                }
            }

            // Also store in local cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                for (java.util.Map.Entry<Object, Object> entry : keyValuePairs.entrySet()) {
                    cache.put(entry.getKey(), entry.getValue());
                }
            }

        } catch (Exception e) {
            logger.warn("Error storing multiple entries in cache: {}", cacheName, e);
        }
    }

    /**
     * Retrieves multiple values from cache.
     */
    public java.util.Map<Object, Object> multiGet(String cacheName, java.util.Set<Object> keys) {
        java.util.Map<Object, Object> result = new java.util.HashMap<>();

        if (!cacheEnabled || keys.isEmpty()) {
            return result;
        }

        logger.debug("Retrieving {} entries from cache: {}", keys.size(), cacheName);

        try {
            if (redisEnabled) {
                // Convert keys to Redis format
                java.util.List<String> redisKeys = keys.stream()
                        .map(key -> buildRedisKey(cacheName, key))
                        .collect(java.util.stream.Collectors.toList());

                java.util.List<Object> values = redisTemplate.opsForValue().multiGet(redisKeys);

                // Map back to original keys
                java.util.Iterator<Object> keyIterator = keys.iterator();
                for (int i = 0; i < values.size() && keyIterator.hasNext(); i++) {
                    Object key = keyIterator.next();
                    Object value = values.get(i);
                    if (value != null) {
                        result.put(key, value);
                    }
                }
            }

            // Fill missing entries from local cache
            for (Object key : keys) {
                if (!result.containsKey(key)) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        Cache.ValueWrapper wrapper = cache.get(key);
                        if (wrapper != null) {
                            result.put(key, wrapper.get());
                        }
                    }
                }
            }

            logger.debug("Retrieved {} out of {} requested entries from cache: {}",
                    result.size(), keys.size(), cacheName);

        } catch (Exception e) {
            logger.warn("Error retrieving multiple entries from cache: {}", cacheName, e);
        }

        return result;
    }

    /**
     * Gets cache statistics for monitoring.
     */
    public CacheStats getCacheStats(String cacheName) {
        CacheStats stats = new CacheStats();
        stats.setCacheName(cacheName);

        try {
            if (redisEnabled) {
                String pattern = buildRedisKey(cacheName, "*");
                Set<String> keys = stringRedisTemplate.keys(pattern);
                stats.setRedisEntryCount(keys != null ? keys.size() : 0);
            }

            // Local cache stats would depend on the cache implementation
            // For now, just indicate if cache exists
            Cache cache = cacheManager.getCache(cacheName);
            stats.setLocalCacheAvailable(cache != null);

        } catch (Exception e) {
            logger.warn("Error getting cache stats for cache: {}", cacheName, e);
        }

        return stats;
    }

    /**
     * Performs cache warming for frequently accessed data.
     */
    public void warmUpCache(String cacheName, java.util.Map<Object, Object> warmUpData) {
        if (!cacheEnabled || warmUpData.isEmpty()) {
            return;
        }

        logger.info("Warming up cache: {} with {} entries", cacheName, warmUpData.size());

        try {
            Duration ttl = getCacheTtl(cacheName);
            multiPut(cacheName, warmUpData, ttl);

            logger.info("Successfully warmed up cache: {} with {} entries",
                    cacheName, warmUpData.size());

        } catch (Exception e) {
            logger.error("Error warming up cache: {}", cacheName, e);
        }
    }

    /**
     * Checks cache health and connectivity.
     */
    public boolean isHealthy() {
        try {
            if (redisEnabled) {
                // Test Redis connectivity
                String testKey = "health_check_" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(testKey, "ok", Duration.ofSeconds(5));
                String value = (String) redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);

                if (!"ok".equals(value)) {
                    return false;
                }
            }

            // Test local cache
            Cache testCache = cacheManager.getCache("test");
            if (testCache != null) {
                String testKey = "health_check";
                testCache.put(testKey, "ok");
                Cache.ValueWrapper wrapper = testCache.get(testKey);
                testCache.evict(testKey);

                if (wrapper == null || !"ok".equals(wrapper.get())) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            return false;
        }
    }

    // Private helper methods

    private String buildRedisKey(String cacheName, Object key) {
        return String.format("ebuy:cache:%s:%s", cacheName, key);
    }

    private Duration getCacheTtl(String cacheName) {
        switch (cacheName) {
            case ORDER_VALIDATION_CACHE:
            case PRODUCT_VALIDATION_CACHE:
                return validationTtl;
            case USER_PERMISSIONS_CACHE:
                return permissionsTtl;
            default:
                return defaultTtl;
        }
    }

    /**
     * Cache statistics container.
     */
    public static class CacheStats {
        private String cacheName;
        private int redisEntryCount;
        private boolean localCacheAvailable;
        private boolean healthy;

        // Getters and setters
        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }

        public int getRedisEntryCount() {
            return redisEntryCount;
        }

        public void setRedisEntryCount(int redisEntryCount) {
            this.redisEntryCount = redisEntryCount;
        }

        public boolean isLocalCacheAvailable() {
            return localCacheAvailable;
        }

        public void setLocalCacheAvailable(boolean localCacheAvailable) {
            this.localCacheAvailable = localCacheAvailable;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        @Override
        public String toString() {
            return "CacheStats{" +
                    "cacheName='" + cacheName + '\'' +
                    ", redisEntryCount=" + redisEntryCount +
                    ", localCacheAvailable=" + localCacheAvailable +
                    ", healthy=" + healthy +
                    '}';
        }
    }
}