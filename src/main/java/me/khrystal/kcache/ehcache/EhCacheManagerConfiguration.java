package me.khrystal.kcache.ehcache;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Created by kHRYSTAL on 19/1/3.
 */
@Configuration
@Order(1)
public class EhCacheManagerConfiguration {

    @Value("${spring.cache.redis.time-to-live:}")
    private String timeToLive;

    private final long MAX_ENTRIES = 1024 * 10 * 2L;

    @Bean
    public CacheManager ehCacheCacheManager() {
        if (StringUtils.isEmpty(timeToLive)) {
            timeToLive = "7d";
        }
        Duration duration;
        timeToLive = timeToLive.toLowerCase();
        if (timeToLive.contains("ms")) {
            timeToLive = timeToLive.replaceAll("ms", "");
            duration = Duration.ofMillis(Long.valueOf(timeToLive.trim()));
        } else if (timeToLive.contains("d")) {
            timeToLive = timeToLive.replaceAll("d", "");
            duration = Duration.ofDays(Long.valueOf(timeToLive.trim()));
        } else if (timeToLive.contains("h")) {
            timeToLive = timeToLive.replaceAll("h", "");
            duration = Duration.ofHours(Long.valueOf(timeToLive.trim()));
        } else if (timeToLive.contains("m")) {
            timeToLive = timeToLive.replaceAll("m", "");
            duration = Duration.ofMinutes(Long.valueOf(timeToLive.trim()));
        } else if (timeToLive.contains("s")) {
            timeToLive = timeToLive.replaceAll("s", "");
            duration = Duration.ofSeconds(Long.valueOf(timeToLive.trim()));
        } else {
            duration = Duration.ofMinutes(Long.valueOf(timeToLive.trim()));
        }
        CacheManager cacheManager;
        if (Long.valueOf(timeToLive.trim()) == 0L) {
            cacheManager = CacheManagerBuilder
                    .newCacheManagerBuilder()
                    .withCache(EhCacheStorage.EHCACHESTORAGE_NAME,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                                    ResourcePoolsBuilder.heap(MAX_ENTRIES)).build()).build(true);
        } else {
            cacheManager = CacheManagerBuilder
                    .newCacheManagerBuilder()
                    .withCache(EhCacheStorage.EHCACHESTORAGE_NAME,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(MAX_ENTRIES))
                                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(duration)).build()).build(true);

        }
        return cacheManager;
    }
}
