package me.khrystal.kcache.annotation;

import me.khrystal.kcache.concurrent.DataCoordinate;
import me.khrystal.kcache.concurrent.EhCacheEvictEvent;
import me.khrystal.kcache.context.ContextStorage;
import me.khrystal.kcache.ehcache.EhCacheManagerConfiguration;
import me.khrystal.kcache.ehcache.EhCacheStorage;
import me.khrystal.kcache.redis.RedisStorage;
import me.khrystal.kcache.redis.RedisTemplateConfiguration;
import me.khrystal.kcache.session.SessionStorage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by kHRYSTAL on 19/1/21.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({
        EhCacheManagerConfiguration.class,
        EhCacheStorage.class,
        RedisTemplateConfiguration.class,
        RedisStorage.class,
        DataCoordinate.class,
        EhCacheEvictEvent.class,
        SessionStorage.class,
        ContextStorage.class,
        CacheBreakAspect.class,
        CacheThisAspect.class
})
public @interface EnableKCache {
}
