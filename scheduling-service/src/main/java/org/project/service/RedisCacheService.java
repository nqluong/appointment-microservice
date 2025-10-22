package org.project.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheService {

    RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting cache key: {}", key, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            log.error("Error getting cache key: {}", key, e);
            return null;
        }
    }

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error caching key: {}", key, e);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Error caching key with TTL: {}", key, e);
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting cache key: {}", key, e);
            return false;
        }
    }

    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Error deleting cache keys", e);
            return 0L;
        }
    }

    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Error getting keys with pattern: {}", pattern, e);
            return Set.of();
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }

    public List<Object> mget(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.error("Error getting multiple cache keys", e);
            return new ArrayList<>();
        }
    }

    public Long leftPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("Error left pushing to queue: {}", key, e);
            return 0L;
        }
    }

    public Object rightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Error right popping from queue: {}", key, e);
            return null;
        }
    }

    public Object rightPop(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.opsForList().rightPop(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Error right popping with timeout from queue: {}", key, e);
            return null;
        }
    }

    public Long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("Error getting list size: {}", key, e);
            return 0L;
        }
    }

    public boolean checkExistsKey(String key) {
        boolean check = false;
        try {
            check = redisTemplate.hasKey(key);
        } catch (Exception ex) {
            log.error("Error checking key existence: {}", key, ex);
        }
        return check;
    }
}

