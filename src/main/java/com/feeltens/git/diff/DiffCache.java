package com.feeltens.git.diff;

import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Diff缓存
 * 使用内存缓存提高性能
 *
 * @author feeltens
 */
@Component
@Slf4j
public class DiffCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5); // 5分钟过期

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final ThreeWayDiffVO value;
        final long timestamp;

        CacheEntry(ThreeWayDiffVO value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TimeUnit.MINUTES.toMillis(5);
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public ThreeWayDiffVO get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        log.debug("缓存命中: key={}", key);
        return entry.value;
    }

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void put(String key, ThreeWayDiffVO value) {
        cache.put(key, new CacheEntry(value));
        log.debug("缓存设置: key={}", key);
    }

    /**
     * 清除缓存
     *
     * @param key 缓存键
     */
    public void invalidate(String key) {
        cache.remove(key);
        log.debug("缓存清除: key={}", key);
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        cache.clear();
        log.debug("清除所有缓存");
    }

    /**
     * 清理过期缓存
     */
    public void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("清理过期缓存");
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    public int size() {
        return cache.size();
    }

}