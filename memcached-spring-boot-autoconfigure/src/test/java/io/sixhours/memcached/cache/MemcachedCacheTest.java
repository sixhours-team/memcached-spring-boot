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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NullValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Memcached cache tests.
 *
 * @author Igor Bolic
 */
public class MemcachedCacheTest {

    private static final String CACHED_OBJECT_KEY = "cached_value_key";

    private static final String CACHE_NAME = "cache";
    private static final String CACHE_PREFIX = Default.PREFIX;
    private static final int CACHE_EXPIRATION = Default.EXPIRATION;

    private static final String NAMESPACE_KEY = Default.NAMESPACE;
    private static final String NAMESPACE_KEY_VALUE = String.valueOf(System.currentTimeMillis());

    private MemcachedClient memcachedClient;
    private MemcachedCache memcachedCache;

    private final Object cachedValue = new Object();
    private final Object nullCachedValue = NullValue.INSTANCE;
    private final Object newCachedValue = new Object();

    private final Object valueLoaderValue = new Object();
    private final Object valueLoaderNullValue = NullValue.INSTANCE;

    private String memcachedKey;
    private String namespaceKey;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        memcachedClient = mock(MemcachedClient.class);
        memcachedCache = new MemcachedCache(CACHE_NAME, memcachedClient, CACHE_EXPIRATION, CACHE_PREFIX, NAMESPACE_KEY);

        memcachedKey = String.format("%s:%s:%s:%s", CACHE_PREFIX, CACHE_NAME, NAMESPACE_KEY_VALUE, CACHED_OBJECT_KEY);
        namespaceKey = String.format("%s:%s:%s", CACHE_PREFIX, CACHE_NAME, NAMESPACE_KEY);
    }

    @Test
    public void whenLookupThenCallMemcachedClientGetKey() {
        when(memcachedClient.get(any())).thenReturn(NAMESPACE_KEY_VALUE).thenReturn(cachedValue);

        Object actual = memcachedCache.lookup(CACHED_OBJECT_KEY);

        assertThat(actual).isEqualTo(cachedValue);

        verify(memcachedClient).get(memcachedKey);
        verify(memcachedClient).get(namespaceKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetNameThenReturnCacheName() {
        String actual = memcachedCache.getName();

        assertThat(actual).isEqualTo(CACHE_NAME);
    }

    @Test
    public void whenGetNativeThenReturnMemcachedClient() {
        Object actual = memcachedCache.getNativeCache();

        assertThat(actual).isSameAs(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderThenReturnCachedValue() {
        when(memcachedClient.get(anyString()))
                .thenReturn(NAMESPACE_KEY_VALUE)
                .thenReturn(cachedValue);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual).isEqualTo(cachedValue);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient).get(memcachedKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderAndNullCachedValueThenReturnNull() {
        when(memcachedClient.get(anyString()))
                .thenReturn(NAMESPACE_KEY_VALUE)
                .thenReturn(nullCachedValue);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual).isEqualTo(null);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient).get(memcachedKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderAndCachedValueFromSecondLookupThenReturnCachedValue() {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null).thenReturn(cachedValue);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual).isEqualTo(cachedValue);

        verify(memcachedClient, times(2)).get(namespaceKey);
        verify(memcachedClient, times(2)).get(memcachedKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderAndNullCachedValueFromSecondLookupThenReturnNull() {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null).thenReturn(nullCachedValue);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual).isEqualTo(null);

        verify(memcachedClient, times(2)).get(namespaceKey);
        verify(memcachedClient, times(2)).get(memcachedKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderAndCachedValueMissingThenReturnValueLoaderNull() {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderNullValue);

        assertThat(actual).isEqualTo(null);

        verify(memcachedClient, times(3)).get(namespaceKey);
        verify(memcachedClient, times(2)).get(memcachedKey);
        verify(memcachedClient).set(memcachedKey, CACHE_EXPIRATION, valueLoaderNullValue);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderAndCachedValueMissingThenReturnValueLoaderValue() {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual).isEqualTo(valueLoaderValue);

        verify(memcachedClient, times(3)).get(namespaceKey);
        verify(memcachedClient, times(2)).get(memcachedKey);
        verify(memcachedClient).set(memcachedKey, CACHE_EXPIRATION, valueLoaderValue);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenGetWithValueLoaderThrowsExceptionThenValueRetrievalException() {
        when(memcachedClient.get(namespaceKey))
                .thenReturn(NAMESPACE_KEY_VALUE)
                .thenReturn(null);

        thrown.expect(Cache.ValueRetrievalException.class);
        thrown.expect(hasProperty("key", is(CACHED_OBJECT_KEY)));

        memcachedCache.get(CACHED_OBJECT_KEY, () -> {
            throw new Exception("exception to be wrapped");
        });
    }

    @Test
    public void whenPutNullThenStoreNullValueInstance() {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);

        memcachedCache.put(CACHED_OBJECT_KEY, null);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient).set(memcachedKey, CACHE_EXPIRATION, NullValue.INSTANCE);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenPutAndNamespaceMissingThenSetNamespace() {
        when(memcachedClient.get(namespaceKey)).thenReturn(null);

        memcachedCache.put(CACHED_OBJECT_KEY, cachedValue);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient).set(eq(namespaceKey), eq(CACHE_EXPIRATION), anyString());
        verify(memcachedClient).set(endsWith(CACHED_OBJECT_KEY), eq(CACHE_EXPIRATION), eq(cachedValue));
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenPutIfAbsentThenReturnExistingValue() {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE).thenReturn(cachedValue);

        Cache.ValueWrapper actual = memcachedCache.putIfAbsent(CACHED_OBJECT_KEY, newCachedValue);

        assertThat(actual.get()).isEqualTo(cachedValue);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient).get(memcachedKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenPutIfAbsentAndNoCachedValueThenReturnNewValue() {
        when(memcachedClient.get(anyString()))
                .thenReturn(NAMESPACE_KEY_VALUE)
                .thenReturn(null)
                .thenReturn(NAMESPACE_KEY_VALUE);

        Cache.ValueWrapper actual = memcachedCache.putIfAbsent(CACHED_OBJECT_KEY, newCachedValue);

        assertThat(actual.get()).isEqualTo(newCachedValue);

        verify(memcachedClient, times(2)).get(namespaceKey);
        verify(memcachedClient).get(memcachedKey);
        verify(memcachedClient).set(eq(memcachedKey), anyInt(), eq(newCachedValue));
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void whenEvictThenMemcachedClientDelete() {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE);

        memcachedCache.evict(CACHED_OBJECT_KEY);

        verify(memcachedClient).delete(memcachedKey);
    }

    @Test
    public void whenClearThenMemcachedClientIncrNamespace() {
        memcachedCache.clear();

        verify(memcachedClient).incr(namespaceKey, 1);
    }
}
