package rs.symbolic.cache.memcached;

import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Memcached cache manager tests
 *
 * @author Igor Bolic
 */
public class MemcachedCacheManagerTest {

    public static final String EXISTING_CACHE = "existing-cache";
    public static final String NON_EXISTING_CACHE = "non-existing-cache";

    private MemcachedCacheManager cacheManager;
    private MemcachedClient memcachedClient;

    @Before
    public void setup() {
        memcachedClient = mock(MemcachedClient.class);
        cacheManager = new MemcachedCacheManager(memcachedClient);

        cacheManager.getCache(EXISTING_CACHE);
    }

    @Test
    public void thatGetCacheReturnsNewCacheWhenRequestedCacheIsNotAvailable() throws Exception {
        Cache cache = cacheManager.getCache(NON_EXISTING_CACHE);


        assertThat(cache, is(notNullValue()));
        assertThat("Cache size should be incremented", cacheManager.getCacheNames().size(), is(2));
    }

    @Test
    public void thatGetCacheReturnsExistingCacheWhenRequested() throws Exception {
        Cache cache = cacheManager.getCache(EXISTING_CACHE);

        assertThat(cacheManager.getCache(EXISTING_CACHE), sameInstance(cache));
        assertThat("Cache size should remain the same", cacheManager.getCacheNames().size(), is(1));
    }

    @Test
    public void thatGetCacheNamesReturnsExistingCacheNames() throws Exception {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        String[] cacheNamesArray = cacheNames.toArray(new String[cacheNames.size()]);

        assertThat(cacheNamesArray.length, is(1));
        assertThat(cacheNamesArray[0], is(EXISTING_CACHE));
    }
}
