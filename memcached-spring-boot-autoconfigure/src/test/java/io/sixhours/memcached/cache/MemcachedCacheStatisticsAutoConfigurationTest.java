package io.sixhours.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.CacheStatisticsProvider;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class MemcachedCacheStatisticsAutoConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    private CacheManager cacheManager;

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void whenCachingNotEnabledThenCacheStatisticsNotLoaded() {
        loadContext(EmptyConfiguration.class);

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No bean named 'memcachedCacheStatisticsProvider' available");

        this.context.getBean("memcachedCacheStatisticsProvider", CacheStatisticsProvider.class);
    }

    @Test
    public void whenCacheTypeIsNoneThenCacheStatisticsNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No bean named 'memcachedCacheStatisticsProvider' available");

        this.context.getBean("memcachedCacheStatisticsProvider", CacheStatisticsProvider.class);
    }

    @Test
    public void whenNoCustomCacheManagerThenCacheStatisticsLoaded() {
        loadContext(MemcachedAutoConfigurationTest.CacheConfiguration.class);

        CacheStatisticsProvider provider = this.context.getBean("memcachedCacheStatisticsProvider", CacheStatisticsProvider.class);

        assertThat(provider).isNotNull();
    }

    @Test
    public void whenMemcachedCacheManagerBeanThenCacheStatisticsLoaded() {
        loadContext(CacheWithMemcachedCacheManagerConfiguration.class);

        CacheStatisticsProvider provider = this.context.getBean(
                "memcachedCacheStatisticsProvider", CacheStatisticsProvider.class);

        assertThat(provider).isNotNull();

        this.cacheManager = this.context.getBean(CacheManager.class);
        Cache books = this.cacheManager.getCache("books");

        CacheStatistics cacheStatistics = provider.getCacheStatistics(this.cacheManager, books);

        assertCacheStatistics(cacheStatistics, null, null);
        getCacheKeyValues(books, "a", "b", "b", "c", "d", "c", "a", "a", "a", "d");

        cacheStatistics = provider.getCacheStatistics(this.cacheManager, books);
        assertCacheStatistics(cacheStatistics, 0.6d, 0.4d);
    }

    private void getCacheKeyValues(Cache cache, String... keys) {
        for (String key : keys) {
            cache.get(key);
        }
    }

    private void assertCacheStatistics(CacheStatistics cacheStatistics, Double hitRatio, Double missRatio) {
        assertThat(cacheStatistics).isNotNull();
        assertRatio(hitRatio, cacheStatistics.getHitRatio());
        assertRatio(missRatio, cacheStatistics.getMissRatio());
    }

    private static void assertRatio(Double actual, Double expected) {
        if (actual == null || expected == null) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(actual).isEqualTo(expected, offset(0.01D));
        }
    }

    private void loadContext(Class<?> configuration, String... environment) {
        EnvironmentTestUtils.addEnvironment(context, environment);

        context.register(configuration);
        context.register(MemcachedCacheAutoConfiguration.class);
        context.register(CacheAutoConfiguration.class);
        context.register(MemcachedCacheStatisticsAutoConfiguration.class);
        context.refresh();
    }

    @Configuration
    static class EmptyConfiguration {
    }

    @Configuration
    @EnableCaching
    static class CacheConfiguration {
    }

    @Configuration
    static class CacheWithCustomCacheManagerConfiguration extends CacheConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Configuration
    static class CacheWithMemcachedCacheManagerConfiguration extends CacheConfiguration {

        @Bean
        public MemcachedCacheManager cacheManager() {
            MemcachedClient memcachedClient = mock(MemcachedClient.class);

            given(memcachedClient.get(any()))
                    .willReturn("namespace").willReturn(null)
                    .willReturn("namespace").willReturn(null)
                    .willReturn("namespace").willReturn("b")
                    .willReturn("namespace").willReturn(null)
                    .willReturn("namespace").willReturn(null)
                    .willReturn("namespace").willReturn("c")
                    .willReturn("namespace").willReturn("a")
                    .willReturn("namespace").willReturn("a")
                    .willReturn("namespace").willReturn("a")
                    .willReturn("namespace").willReturn("d");

            return new MemcachedCacheManager(memcachedClient);
        }
    }

}