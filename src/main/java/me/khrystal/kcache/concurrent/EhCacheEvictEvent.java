package me.khrystal.kcache.concurrent;

import me.khrystal.kcache.console.Console;
import me.khrystal.kcache.redis.RedisStorage;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by kHRYSTAL on 19/1/2.
 */
public class EhCacheEvictEvent implements CacheEventListener {

    @Autowired
    private RedisStorage redisStorage;

    @Override
    public void onEvent(CacheEvent cacheEvent) {
        if (cacheEvent.getType().equals(EventType.EXPIRED)) {
            Object objKey = cacheEvent.getKey();
            if (null != objKey) {
                String key = objKey.toString();
                redisStorage.del(key);
                Console.println("[K-Cache] 数据已过期, key:" + objKey);
            }
        }
    }
}
