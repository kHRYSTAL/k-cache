package me.khrystal.kcache.ehcache;

import me.khrystal.kcache.concurrent.EhCacheEvictEvent;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kHRYSTAL on 19/1/2.
 */
@Component
@Order(2)
public class EhCacheStorage {
    private static final String EHCACHESTORAGE_NAME = EhCacheStorage.class.getSimpleName();

    private static Cache cache;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private EhCacheEvictEvent event;

    /**
     * save cache
     */
    public void set(String key, Object value) {
        Cache cache = getCache();
        cache.put(key, value);
    }

    /**
     * save cache when duration to delete
     */
    public void set(String key, Object value, Long duration) {
        Cache cache = getCache();
        cache.put(key, value);
        if (duration == null) {
            duration = 60 * 60 * 24 * 7 * 1000L;
        }
        executorService.schedule(() -> {
        }, duration, TimeUnit.MILLISECONDS);
    }

    /**
     * get cache
     */
    public <T extends Object> T get(String key) {
        Cache cache = getCache();
        return (T) cache.get(key);
    }

    /**
     * delete cache by key
     */
    public void del(String key) {
        Cache cache = getCache();
        cache.remove(key);
    }

    private Cache getCache() {
        if (cache == null) {
            cache = cacheManager.getCache(EHCACHESTORAGE_NAME, String.class, Object.class);
            cache.getRuntimeConfiguration().registerCacheEventListener(event, EventOrdering.UNORDERED
                    , EventFiring.ASYNCHRONOUS
                    , EnumSet.of(EventType.EXPIRED));
        }
        return cache;
    }
}
