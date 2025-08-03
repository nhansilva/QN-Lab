package com.mservice.cinema.promotion.cinema_promotion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "user_lock:";
    private static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(10);
    
    @Autowired
    public DistributedLockService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Acquire a distributed lock for a specific resource
     * @param resourceKey The resource to lock (e.g., user name)
     * @return LockResult containing lock ID and success status
     */
    public LockResult acquireLock(String resourceKey) {
        return acquireLock(resourceKey, DEFAULT_LOCK_TIMEOUT, DEFAULT_WAIT_TIMEOUT);
    }
    
    /**
     * Acquire a distributed lock with custom timeouts
     * @param resourceKey The resource to lock
     * @param lockTimeout How long the lock should be held
     * @param waitTimeout How long to wait for the lock
     * @return LockResult containing lock ID and success status
     */
    public LockResult acquireLock(String resourceKey, Duration lockTimeout, Duration waitTimeout) {
        String lockKey = LOCK_PREFIX + resourceKey;
        String lockValue = UUID.randomUUID().toString();
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + waitTimeout.toMillis();
        
        while (System.currentTimeMillis() < endTime) {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, lockTimeout.toMillis(), TimeUnit.MILLISECONDS);
            
            if (Boolean.TRUE.equals(acquired)) {
                return new LockResult(true, lockValue, lockKey);
            }
            
            // Wait a bit before retrying
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new LockResult(false, null, lockKey);
            }
        }
        
        return new LockResult(false, null, lockKey);
    }
    
    /**
     * Release a distributed lock
     * @param lockKey The lock key
     * @param lockValue The lock value (must match to release)
     * @return true if lock was released, false otherwise
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        
        Long result = redisTemplate.execute(
            redisScript,
            Collections.singletonList(lockKey),
            lockValue
        );
        
        return result != null && result == 1L;
    }
    
    /**
     * Result of lock acquisition attempt
     */
    public static class LockResult {
        private final boolean acquired;
        private final String lockValue;
        private final String lockKey;
        
        public LockResult(boolean acquired, String lockValue, String lockKey) {
            this.acquired = acquired;
            this.lockValue = lockValue;
            this.lockKey = lockKey;
        }
        
        public boolean isAcquired() {
            return acquired;
        }
        
        public String getLockValue() {
            return lockValue;
        }
        
        public String getLockKey() {
            return lockKey;
        }
    }
} 