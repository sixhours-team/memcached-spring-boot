/*
 * Copyright 2016-2022 Sixhours
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedClient;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Refreshable configuration tests.
 *
 * @author Igor Bolic
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshableConfigurationTest.TestConfiguration.class)
public class RefreshableConfigurationTest {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ContextRefresher refresher;

    @Test
    @DirtiesContext
    public void whenContextLoadedThenMemcachedCacheManagerInitialized() {
        assertThat(cacheManager)
                .isNotNull()
                .isInstanceOf(MemcachedCacheManager.class);

        Object memcachedClient = ReflectionTestUtils.getField(cacheManager, "memcachedClient");

        assertThat(memcachedClient)
                .isNotNull()
                .isInstanceOf(XMemcachedClient.class);

        assertMemcachedClient((IMemcachedClient) memcachedClient);
    }

    @Test
    @DirtiesContext
    public void whenConfigurationChangedThenMemcachedClientReinitialized() {
        Object beforeRefresh = ReflectionTestUtils.getField(cacheManager, "memcachedClient");
        assertMemcachedClient((IMemcachedClient) beforeRefresh);

        TestPropertyValues.of(
            "memcached.cache.prefix:test-prefix",
            "memcached.cache.protocol:binary"
        ).applyTo(environment);

        refresher.refresh();

        Object expiration = ReflectionTestUtils.getField(cacheManager, "expiration");
        Object prefix = ReflectionTestUtils.getField(cacheManager, "prefix");
        Object afterRefresh = ReflectionTestUtils.getField(cacheManager, "memcachedClient");

        assertThat(expiration)
                .isNotNull()
                .isEqualTo(Default.EXPIRATION);
        assertThat(prefix)
                .isNotNull()
                .isEqualTo("test-prefix");
        assertMemcachedClient((IMemcachedClient) afterRefresh,
                MemcachedCacheProperties.Protocol.BINARY, Default.OPERATION_TIMEOUT);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCaching
    protected static class TestConfiguration {
    }
}
