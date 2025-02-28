/*
 * Copyright 2016-2025 Sixhours
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;

/**
 * Memcached cache manager tests.
 *
 * @author Igor Bolic
 */
public class MemcachedCacheManagerTest {

    private static final String EXISTING_CACHE = "existing-cache";
    private static final String NON_EXISTING_CACHE = "non-existing-cache";

    private MemcachedCacheManager cacheManager;

    @Before
    public void setUp() {
        IMemcachedClient memcachedClient = mock(IMemcachedClient.class);
        cacheManager = new MemcachedCacheManager(memcachedClient);

        cacheManager.getCache(EXISTING_CACHE);
    }

    @Test
    public void whenGetNonExistingCacheThenIncreaseCacheSize() {
        Cache cache = cacheManager.getCache(NON_EXISTING_CACHE);

        assertThat(cache).isNotNull();
        assertThat(cacheManager.getCacheNames())
                .as("Cache size should be incremented")
                .hasSize(2);
    }

    @Test
    public void whenGetCacheThenReturnExistingCache() {
        Cache cache = cacheManager.getCache(EXISTING_CACHE);

        assertThat(cacheManager.getCache(EXISTING_CACHE)).isSameAs(cache);
        assertThat(cacheManager.getCacheNames())
                .as("Cache size should remain the same")
                .hasSize(1);
    }

    @Test
    public void whenGetCacheNamesThenReturnExistingCacheNames() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        String[] cacheNamesArray = cacheNames.toArray(new String[0]);

        assertThat(cacheNamesArray).hasSize(1);
        assertThat(cacheNamesArray[0]).isEqualTo(EXISTING_CACHE);
    }

    @Test
    public void whenGetDisabledCacheThenIncreaseCacheSize() {
        final Set<String> disabledCacheNames = new HashSet<>();
        disabledCacheNames.add(EXISTING_CACHE);
        cacheManager.setDisabledCacheNames(disabledCacheNames);
        Cache cache = cacheManager.getCache(EXISTING_CACHE);

        assertThat(cache).isInstanceOf(NoOpCache.class);
    }

    @Test
    public void whenLoadCachesThenReturnMetricsCacheNames() {
        cacheManager.setMetricsCacheNames(Collections.singletonList(EXISTING_CACHE));

        Collection<? extends Cache> result = cacheManager.loadCaches();

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .isInstanceOf(ArrayList.class);

        Object actual = result.toArray()[0];
        assertThat(actual).isInstanceOf(MemcachedCache.class);
        assertThat((MemcachedCache) actual).extracting("name").isEqualToComparingFieldByField(EXISTING_CACHE);
    }
}
