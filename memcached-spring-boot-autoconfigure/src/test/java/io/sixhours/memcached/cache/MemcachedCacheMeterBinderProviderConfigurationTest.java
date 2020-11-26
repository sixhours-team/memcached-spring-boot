/**
 * Copyright 2016-2020 Sixhours
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sixhours.memcached.cache;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.rubyeye.xmemcached.MemcachedClient;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * Cache statistics auto-configuration tests.
 *
 * @author Igor Bolic
 */
public class MemcachedCacheMeterBinderProviderConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    private Tags expectedTag = Tags.of("app", "test");

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void whenCachingNotEnabledThenCacheStatisticsNotLoaded() {
        loadContext(EmptyConfiguration.class);

        assertThatThrownBy(() ->
                this.context.getBean("memcachedCacheMeterBinderProvider", CacheMeterBinderProvider.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No bean named 'memcachedCacheMeterBinderProvider' available");
    }

    @Test
    public void whenCacheTypeIsNoneThenCacheStatisticsNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        assertThatThrownBy(() ->
                this.context.getBean("memcachedCacheMeterBinderProvider", CacheMeterBinderProvider.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No bean named 'memcachedCacheMeterBinderProvider' available");
    }

    @Test
    public void whenNoCustomCacheManagerThenCacheStatisticsLoaded() {
        loadContext(MemcachedAutoConfigurationTest.CacheConfiguration.class);

        CacheMeterBinderProvider provider = this.context.getBean("memcachedCacheMeterBinderProvider", CacheMeterBinderProvider.class);

        assertThat(provider).isNotNull();
    }

    @Test
    public void whenMemcachedCacheManagerBeanThenCacheStatisticsLoaded() {
        loadContext(CacheWithMemcachedCacheManagerConfiguration.class);

        CacheMeterBinderProvider provider = this.context.getBean(
                "memcachedCacheMeterBinderProvider", CacheMeterBinderProvider.class);

        assertThat(provider).isNotNull();

        CacheManager cacheManager = this.context.getBean(CacheManager.class);
        Cache books = cacheManager.getCache("books");

        MeterBinder metrics = provider.getMeterBinder(books, expectedTag);

        MeterRegistry registry = new SimpleMeterRegistry();
        metrics.bindTo(registry);

        FunctionCounter hits = registry.get("cache.gets").tags(expectedTag).tag("result", "hit").functionCounter();
        FunctionCounter misses = registry.get("cache.gets").tags(expectedTag).tag("result", "miss").functionCounter();
        FunctionCounter puts = registry.get("cache.puts").tags(expectedTag).functionCounter();
        double availableServersCount = registry.get("available_servers_count").gauge().value();

        assertThat(hits.count()).isZero();
        assertThat(misses.count()).isZero();
        assertThat(puts.count()).isZero();
        assertThat(availableServersCount).isEqualTo(1.0);

        getCacheKeyValues(books, "a", "b", "b", "c", "d", "c", "a", "a", "a", "d");

        assertThat(hits.count()).isEqualTo(6);
        assertThat(misses.count()).isEqualTo(4);
        assertThat(puts.count()).isZero();
        assertThat(availableServersCount).isEqualTo(1.0);
    }

    private void getCacheKeyValues(Cache cache, String... keys) {
        for (String key : keys) {
            cache.get(key);
        }
    }

    private void loadContext(Class<?> configuration, String... environment) {
        TestPropertyValues.of(environment).applyTo(context);

        context.register(configuration);
        context.register(MemcachedCacheAutoConfiguration.class);
        context.register(CacheAutoConfiguration.class);
        context.register(MemcachedCacheMeterBinderProviderConfiguration.class);
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
    static class CacheWithMemcachedCacheManagerConfiguration extends CacheConfiguration {

        @Bean
        public MemcachedCacheManager cacheManager() {
            final XMemcachedClient memcachedClient = mock(XMemcachedClient.class);
            final MemcachedClient client = mock(MemcachedClient.class);


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
            given(memcachedClient.nativeClient()).willReturn(client);
            given(client.getAvailableServers())
                    .willReturn(Collections.singletonList(new InetSocketAddress("127.0.0.1", 11222)));

            return new MemcachedCacheManager(memcachedClient);
        }
    }

}
