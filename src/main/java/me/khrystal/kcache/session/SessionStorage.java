package me.khrystal.kcache.session;

import me.khrystal.kcache.console.Console;
import me.khrystal.kcache.ehcache.EhCacheStorage;
import me.khrystal.kcache.redis.RedisStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kHRYSTAL on 19/1/2.
 */
@Component
@Order(3)
public class SessionStorage {
    @Autowired
    private RedisStorage redisStorage;
    @Autowired
    private EhCacheStorage ehCacheStorage;

    @Value("${spring.application.name}")
    private String applicationName;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void put(String key, Object value, String... region) {
        String[] keys = getSessionKeys(key, region);
        for (String s : keys) {
            saveSession(s, value);
            Console.println(String.format("[K-Cache] Session存值, key:%s", s));
        }
    }

    public Object get(String key, String... region) {
        String[] keys = getSessionKeys(key, region);
        for (String s : keys) {
            Object value = getSession(s);
            if (null != value) {
                Console.println(String.format("[K-Cache] Session取值, key:%s", s));
                return value;
            }
        }
        return null;
    }

    public void remove(String key, String... region) {
        String[] keys = getSessionKeys(key, region);
        for (String s : keys) {
            deleteSession(s);
            Console.println(String.format("[K-Cache] Session删除值, key:%s", s));
        }
    }

    private String[] getSessionKeys(String key, String... region) {
        String[] keys;
        if (region == null || region.length == 0) {
            keys = new String[1];
            if (!StringUtils.isEmpty(applicationName)) {
                key = "session:" + applicationName + ":" + key;
            } else {
                String userDir = System.getProperty("user.dir");
                userDir = userDir.substring(userDir.lastIndexOf(File.separator));
                key = "session:" + userDir + ":" + key;
            }
            keys[0] = key;
        } else {
            keys = new String[region.length];
            for (int i = 0; i < region.length; i++) {
                String t = "session:" + region[i] + ":" + key;
                keys[i] = t;
            }
        }
        return keys;
    }

    /**
     * save session two hours delete
     */
    private void saveSession(String key, Object value) {
        ehCacheStorage.set(key, value);
        if (redisStorage.getRedisUsable()) {
            redisStorage.set(key, value);
        }
        executorService.schedule(() -> {
            ehCacheStorage.del(key);
            if (redisStorage.getRedisUsable()) {
                redisStorage.del(key);
            }
        }, 2, TimeUnit.HOURS);
    }

    private Object getSession(String key) {
        Object value = ehCacheStorage.get(key);
        if (null == value && redisStorage.getRedisUsable()) {
            value = redisStorage.get(key);
        }
        return value;
    }

    private void deleteSession(String key) {
        ehCacheStorage.del(key);
        if (redisStorage.getRedisUsable()) {
            redisStorage.del(key);
        }
    }
}
