package me.khrystal.kcache.concurrent;

import me.khrystal.kcache.annotation.NullValObject;
import me.khrystal.kcache.ehcache.EhCacheStorage;
import me.khrystal.kcache.redis.RedisStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kHRYSTAL on 19/1/3.
 */
@Component
@Order(3)
public class DataCoordinate {

    @Autowired
    private RedisStorage redisStorage;

    @Autowired
    private EhCacheStorage ehCacheStorage;

    // execute thread pool
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * del 2 level cache
     */
    public void omitLevel2(String key) {
        executorService.execute(() -> {
            redisStorage.del(key);
        });
    }

    /**
     * set 2 level cache
     */
    public void fulFillLevel2(String key, Object value) {
        executorService.execute(() -> {
            redisStorage.set(key, value);
        });
    }

    /**
     * del 1 level cache
     */
    public void omitLevel1(String key) {
        executorService.execute(() -> {
            ehCacheStorage.del(key);
        });
    }

    /**
     * set 1 level cache
     */
    public void fulFillLevel1(String key, Object value) {
        executorService.execute(() -> ehCacheStorage.set(key, value));
    }

    /**
     * set 1 level null cache, anti breakdown
     */
    public void fulFillLevel1NullValue(String key) {
        executorService.execute(() -> ehCacheStorage.set(key, new NullValObject(), 21 * 1000L));
    }

    /**
     * get 1 level cache
     */
    public Object getLevel1Val(String key) {
        return ehCacheStorage.get(key);
    }

    /**
     * get 2 level cache
     */
    public Object getLevel2Val(String key) {
        return redisStorage.get(key);
    }

    /**
     * judge 2 level cache storage is usable
     */
    public boolean isLevel2Usable() {
        return redisStorage.getRedisUsable();
    }
}
