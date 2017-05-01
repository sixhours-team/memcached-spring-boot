package rs.symbolic.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.cache.Cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Memcached cache tests.
 *
 * @author Igor Bolic
 */
public class MemcachedCacheTest {

    public static final String CACHED_OBJECT_KEY = "cached_value_key";

    public static final String CACHE_NAME = "cache";
    public static final String CACHE_PREFIX = Default.PREFIX;
    public static final int CACHE_EXPIRATION = Default.EXPIRATION;

    public static final String NAMESPACE_KEY = Default.NAMESPACE;
    public static final String NAMESPACE_KEY_VALUE = "123";


    private MemcachedClient memcachedClient;
    private MemcachedCache memcachedCache;

    private final Object cachedValue = new Object();
    private final Object newCachedValue = new Object();
    private final Object valueLoaderValue = new Object();
    private String memcachedKey;
    private String namespaceKey;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        memcachedClient = mock(MemcachedClient.class);
        memcachedCache = new MemcachedCache(CACHE_NAME, memcachedClient, CACHE_EXPIRATION, CACHE_PREFIX, NAMESPACE_KEY);

        memcachedKey = String.format("%s:%s:%s:%s", CACHE_PREFIX, CACHE_NAME, NAMESPACE_KEY_VALUE, CACHED_OBJECT_KEY);
        namespaceKey = String.format("%s:%s:%s", CACHE_PREFIX, CACHE_NAME, NAMESPACE_KEY);
    }

    @Test
    public void thatLookupCallsMemcachedClientGetKey() throws Exception {
        when(memcachedClient.get(anyObject())).thenReturn(NAMESPACE_KEY_VALUE).thenReturn(cachedValue);

        Object actual = memcachedCache.lookup(CACHED_OBJECT_KEY);

        assertThat(actual, is(cachedValue));

        verify(memcachedClient).get(argThat(is(memcachedKey)));
        verify(memcachedClient).get(namespaceKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatGetNameReturnsCacheName() throws Exception {
        String actual = memcachedCache.getName();

        assertThat(actual, is(CACHE_NAME));
    }

    @Test
    public void thatGetNativeCacheReturnsMemcachedClient() throws Exception {
        Object actual = memcachedCache.getNativeCache();

        assertThat(actual, is(memcachedClient));
    }

    @Test
    public void thatGetWithValueLoaderReturnsExistingValue() throws Exception {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE).thenReturn(cachedValue);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual, is(cachedValue));

        verify(memcachedClient).get(memcachedKey);
        verify(memcachedClient).get(namespaceKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatGetWithValueLoaderReturnsValueLoaderValueWhenCachedValueMissing() throws Exception {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null);

        Object actual = memcachedCache.get(CACHED_OBJECT_KEY, () -> valueLoaderValue);

        assertThat(actual, is(valueLoaderValue));

        verify(memcachedClient, times(2)).get(memcachedKey);
        verify(memcachedClient, times(3)).get(namespaceKey);
        verify(memcachedClient).set(memcachedKey, CACHE_EXPIRATION, valueLoaderValue);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatGetWithValueLoaderWithExceptionIsWrappedInValueRetrievalException() throws Exception {
        when(memcachedClient.get(namespaceKey)).thenReturn(NAMESPACE_KEY_VALUE);
        when(memcachedClient.get(memcachedKey)).thenReturn(null);

        thrown.expect(Cache.ValueRetrievalException.class);
        thrown.expect(hasProperty("key", is(CACHED_OBJECT_KEY)));

        memcachedCache.get(CACHED_OBJECT_KEY, () -> {
            throw new Exception("exception to be wrapped");
        });
    }

    @Test
    public void thatPutForNewNamespaceValueCallsMemcachedClientSet() throws Exception {
        when(memcachedClient.get(namespaceKey)).thenReturn(null);

        memcachedCache.put(CACHED_OBJECT_KEY, cachedValue);

        verify(memcachedClient).get(namespaceKey);
        verify(memcachedClient, times(2)).set(anyString(), anyInt(), anyString());
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatPutIfAbsentReturnsExistingValue() throws Exception {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE).thenReturn(cachedValue);

        Cache.ValueWrapper actual = memcachedCache.putIfAbsent(CACHED_OBJECT_KEY, newCachedValue);

        assertThat(actual.get(), is(cachedValue));

        verify(memcachedClient).get(memcachedKey);
        verify(memcachedClient).get(namespaceKey);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatPutIfAbsentReturnsNewValue() throws Exception {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE, null, NAMESPACE_KEY_VALUE);

        Cache.ValueWrapper actual = memcachedCache.putIfAbsent(CACHED_OBJECT_KEY, newCachedValue);

        assertThat(actual.get(), is(newCachedValue));

        verify(memcachedClient, times(2)).get(namespaceKey);
        verify(memcachedClient).get(memcachedKey);
        verify(memcachedClient).set(anyString(), anyInt(), anyObject());
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void thatEvictCallsMemcachedClientDelete() throws Exception {
        when(memcachedClient.get(anyString())).thenReturn(NAMESPACE_KEY_VALUE);

        memcachedCache.evict(CACHED_OBJECT_KEY);

        verify(memcachedClient).delete(memcachedKey);
    }

    @Test
    public void thatClearCallsMemcachedClientIncrOnNamespace() throws Exception {
        memcachedCache.clear();

        verify(memcachedClient).incr(namespaceKey, 1);
    }
}
