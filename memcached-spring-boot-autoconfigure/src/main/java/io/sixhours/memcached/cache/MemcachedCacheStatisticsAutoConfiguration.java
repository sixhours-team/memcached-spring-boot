package io.sixhours.memcached.cache;

import org.springframework.boot.actuate.cache.CacheStatisticsProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@ConditionalOnBean(MemcachedCacheManager.class)
@ConditionalOnClass(CacheStatisticsProvider.class)
public class MemcachedCacheStatisticsAutoConfiguration {

    @Bean
    public MemcachedCacheStatisticsProvider memcachedCacheStatisticsProvider() {
        return new MemcachedCacheStatisticsProvider();
    }
}
