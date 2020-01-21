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

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedCacheManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Memcached auto-configuration tests.
 *
 * @author Igor Bolic
 * @author Sasa Bolic
 */
public class MemcachedAutoConfigurationTest {

    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    @After
    public void tearDown() {
        applicationContext.close();
    }

    @Test
    public void whenCachingNotEnabledThenMemcachedNotLoaded() {
        loadContext(EmptyConfiguration.class);

        assertThatThrownBy(() ->
                this.applicationContext.getBean(MemcachedCacheManager.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");
    }

    @Test
    public void whenCacheTypeIsNoneThenMemcachedNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        assertThatThrownBy(() ->
                this.applicationContext.getBean(MemcachedCacheManager.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");
    }

    @Test
    public void whenCacheTypeIsNoneThenNoOpCacheLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(NoOpCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsSimpleThenMemcachedNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=simple");

        assertThatThrownBy(() ->
                this.applicationContext.getBean(MemcachedCacheManager.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");
    }

    @Test
    public void whenCacheTypeIsSimpleThenSimpleCacheLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=simple");

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsInvalidThenContextNotLoaded() {
        assertThatThrownBy(() ->
                loadContext(CacheConfiguration.class, "spring.cache.type=invalid-type")
        )
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Failed to bind properties under 'spring.cache.type' to org.springframework.boot.autoconfigure.cache.CacheType");
    }

    @Test
    public void whenUsingCustomCacheManagerThenMemcachedNotLoaded() {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        assertThatThrownBy(() ->
                this.applicationContext.getBean(MemcachedCacheManager.class)
        )
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");
    }

    @Test
    public void whenUsingCustomCacheManagerThenMemcachedCustomCacheManagerLoaded() {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
    }

    @Test
    public void whenNoCustomCacheManagerThenMemcachedCacheManagerLoaded() {
        loadContext(CacheConfiguration.class);

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(MemcachedCacheManager.class);
    }

    @Test
    public void whenRefreshAutoConfigurationThenRefreshConfigurationLoaded() {
        loadContext(CacheWithRefreshAutoConfiguration.class);

        assertThat(this.applicationContext
                .getBeanDefinition("scopedTarget.cacheManager").getScope()).isEqualTo("refresh");
    }

    @Test
    public void whenMemcachedCacheManagerBeanAlreadyInContextThenMemcachedWithNonCustomConfigurationLoaded() {
        loadContext(CacheWithMemcachedCacheManagerConfiguration.class, "memcached.cache.expiration=3600",
                "memcached.cache.prefix=custom:prefix",
                "memcached.cache.namespace=custom_namespace");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        assertThat(memcachedCacheManager)
                .as("Auto-configured disposable instance should not be loaded in context")
                .isNotInstanceOf(DisposableMemcachedCacheManager.class);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, null, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenAppEngineProviderThenAppEngineMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.provider=appengine");

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(DisposableMemcachedCacheManager.class);
        assertThat(((DisposableMemcachedCacheManager) cacheManager).memcachedClient).isInstanceOf(AppEngineMemcachedClient.class);
    }

    @Test
    public void whenStaticProviderAndEmptyServerListThenMemcachedNotLoaded() {
        assertThatThrownBy(() ->
                loadContext(CacheConfiguration.class, "memcached.cache.servers=",
                        "memcached.cache.provider=static")
        )
                .isInstanceOf(UnsatisfiedDependencyException.class)
                .hasRootCause(new IllegalArgumentException("Server list is empty"));
    }

    @Test
    public void whenOperationTimeoutInvalidThenMemcachedNotLoaded() {
        assertThatThrownBy(() -> loadContext(CacheConfiguration.class,
                "memcached.cache.operation-timeout=0"))
                .isInstanceOf(UnsatisfiedDependencyException.class)
                .hasRootCause(new IllegalArgumentException("Operation timeout must be greater then zero"));
    }

    private void loadContext(Class<?> configuration, String... environment) {
        TestPropertyValues.of(environment).applyTo(applicationContext);

        applicationContext.register(configuration);
        applicationContext.register(MemcachedCacheAutoConfiguration.class);
        applicationContext.register(CacheAutoConfiguration.class);
        applicationContext.refresh();
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
            IMemcachedClient memcachedClient = mock(IMemcachedClient.class);

            return new MemcachedCacheManager(memcachedClient);
        }
    }

    @Configuration
    @Import(RefreshAutoConfiguration.class)
    static class CacheWithRefreshAutoConfiguration extends CacheConfiguration {
    }

}
