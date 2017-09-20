/*
 * Copyright 2017 Sixhours.
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

import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

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
    public void setup() {
        MemcachedClient memcachedClient = mock(MemcachedClient.class);
        cacheManager = new MemcachedCacheManager(memcachedClient);

        cacheManager.getCache(EXISTING_CACHE);
    }

    @Test
    public void thatGetCacheReturnsNewCacheWhenRequestedCacheIsNotAvailable() {
        Cache cache = cacheManager.getCache(NON_EXISTING_CACHE);

        assertThat(cache, is(notNullValue()));
        assertThat("Cache size should be incremented", cacheManager.getCacheNames().size(), is(2));
    }

    @Test
    public void thatGetCacheReturnsExistingCacheWhenRequested() {
        Cache cache = cacheManager.getCache(EXISTING_CACHE);

        assertThat(cacheManager.getCache(EXISTING_CACHE), sameInstance(cache));
        assertThat("Cache size should remain the same", cacheManager.getCacheNames().size(), is(1));
    }

    @Test
    public void thatGetCacheNamesReturnsExistingCacheNames() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        String[] cacheNamesArray = cacheNames.toArray(new String[cacheNames.size()]);

        assertThat(cacheNamesArray.length, is(1));
        assertThat(cacheNamesArray[0], is(EXISTING_CACHE));
    }
}
