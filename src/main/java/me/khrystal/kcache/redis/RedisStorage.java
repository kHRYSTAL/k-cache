package me.khrystal.kcache.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by kHRYSTAL on 19/1/2.
 */
@Component
@Order(2)
public class RedisStorage {
    @Autowired
    private RedisTemplate<String, Object> template;

    // redisServer 是否正常连接
    private static boolean redisUsable = false;
    // 当连接失败时, 后续尝试连接次数
    private static int tryCount = 30;

    /**
     * set cache
     */
    public void set(String key, Object value) {
        template.opsForValue().set(key, value);
    }

    /**
     * get cache
     */
    public <T extends Object> T get(String key) {
        return (T) template.opsForValue().get(key);
    }

    /**
     * del cache by key
     */
    public void del(String key) {
        template.delete(key);
    }

    /**
     * clear all
     */
    public void clear() {
        template.discard();
    }

    public boolean getRedisUsable() {
        if (!redisUsable) {
            if (tryCount > 0) {
                try {
                    template.opsForValue().get("TEST_CONNECTION_KEY");
                    redisUsable = true;
                } catch (Exception e) {
                    System.out.println("[K-Cache] Redis Server 未能成功连接,未启用二级缓存,Session也将无法共享");
                    redisUsable = false;
                }
                tryCount--;
            }
        }
        return redisUsable;
    }
}
