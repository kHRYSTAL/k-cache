package me.khrystal.kcache.context;

import me.khrystal.kcache.ehcache.EhCacheStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by kHRYSTAL on 19/1/2.
 */
@Component
@Order(3)
public class ContextStorage {
    @Autowired
    private EhCacheStorage ehCacheStorage;

    public void put(String key, Object value, Long... duration) {
        if (null == duration || duration.length == 0) {
            ehCacheStorage.set(key, value);
        } else {
            ehCacheStorage.set(key, value, duration[0]);
        }
    }

    /**
     * get cache
     */
    public Object get(String key) {
        return ehCacheStorage.get(key);
    }

    /**
     * remove cache
     */
    public void remove(String key) {
        ehCacheStorage.del(key);
    }
}
