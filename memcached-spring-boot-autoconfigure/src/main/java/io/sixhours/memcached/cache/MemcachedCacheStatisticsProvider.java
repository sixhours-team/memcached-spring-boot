package io.sixhours.memcached.cache;

import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.CacheStatisticsProvider;
import org.springframework.boot.actuate.cache.DefaultCacheStatistics;
import org.springframework.cache.CacheManager;

public class MemcachedCacheStatisticsProvider implements CacheStatisticsProvider<MemcachedCache> {

    @Override
    public CacheStatistics getCacheStatistics(CacheManager cacheManager, MemcachedCache memcachedCache) {
        DefaultCacheStatistics statistics = new DefaultCacheStatistics();

        statistics.setGetCacheCounts(memcachedCache.hits(), memcachedCache.misses());

        return statistics;
    }
}
