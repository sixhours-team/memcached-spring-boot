package rs.symbolic.cache.memcached;

import net.spy.memcached.MemcachedClient;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * Cache implementation on top of Memcached.
 *
 * @author Igor Bolic
 */
public class MemcachedCache extends AbstractValueAdaptingCache {

    /**
     * Create an {@code MemcachedCache} with the given settings.
     *
     * @param name            Cache name
     * @param memcachedClient {@link MemcachedClient}
     * @param expiration      Cache expiration in seconds
     * @param prefix          Cache key prefix
     * @param namespace       Cache invalidation namespace key
     */
    public MemcachedCache(String name, MemcachedClient memcachedClient, int expiration, String prefix, String namespace) {
        super(true);
    }

    @Override
    protected Object lookup(Object key) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
