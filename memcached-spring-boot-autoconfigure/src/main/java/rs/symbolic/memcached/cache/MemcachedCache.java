package rs.symbolic.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache implementation on top of Memcached.
 *
 * @author Igor Bolic
 */
public class MemcachedCache extends AbstractValueAdaptingCache {

    private static final String KEY_DELIMITER = ":";

    private final MemcachedClient memcachedClient;
    private final MemcacheCacheMetadata memcacheCacheMetadata;

    private final Lock lock = new ReentrantLock();

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
        this.memcachedClient = memcachedClient;
        this.memcacheCacheMetadata = new MemcacheCacheMetadata(name, expiration, prefix, namespace);
    }

    @Override
    protected Object lookup(Object key) {
        return memcachedClient.get(new MemcachedKey(key).value());
    }

    @Override
    public String getName() {
        return this.memcacheCacheMetadata.name();
    }

    @Override
    public Object getNativeCache() {
        return this.memcachedClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) fromStoreValue(value);
        }

        lock.lock();
        try {
            value = lookup(key);
            if (value != null) {
                return (T) fromStoreValue(value);
            } else {
                return loadValue(key, valueLoader);
            }
        } finally {
            lock.unlock();
        }
    }

    private <T> T loadValue(Object key, Callable<T> valueLoader) {
        T value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        put(key, value);
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        this.memcachedClient.set(new MemcachedKey(key).value(), this.memcacheCacheMetadata.expiration(), value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object existingValue = lookup(key);
        if (existingValue == null) {
            put(key, value);
            return toValueWrapper(value);
        }

        return toValueWrapper(existingValue);
    }

    @Override
    public void evict(Object key) {
        this.memcachedClient.delete(new MemcachedKey(key).value());
    }

    @Override
    public void clear() {
        this.memcachedClient.incr(this.memcacheCacheMetadata.namespace(), 1);
    }

    /**
     * Wrapper class for the Memcached key value.
     * <p>
     * All whitespace characters will be stripped from the key value, for Memcached
     * key to be valid.
     */
    class MemcachedKey {
        private final StringBuilder value;

        public MemcachedKey(Object key) {
            this.value = new StringBuilder(memcacheCacheMetadata.cachePrefix())
                    .append(namespaceValue())
                    .append(KEY_DELIMITER)
                    .append(String.valueOf(key).replaceAll("\\s", ""));
        }

        String value() {
            return this.value.toString();
        }

        /**
         * Gets namespace value from the cache. The value is used for invalidation of the cache data
         * by incrementing current namespace value by 1.
         *
         * @return Namespace integer value returned as {@code String}
         */
        private String namespaceValue() {
            String value = (String) MemcachedCache.this.memcachedClient.get(MemcachedCache.this.memcacheCacheMetadata.namespace());
            if (value == null) {
                value = String.valueOf(new Random().nextInt(1000));
                MemcachedCache.this.memcachedClient.set(MemcachedCache.this.memcacheCacheMetadata.namespace(),
                        MemcachedCache.this.memcacheCacheMetadata.expiration(), value);
            }

            return value;
        }
    }

    class MemcacheCacheMetadata {
        private final String name;
        private final int expiration;
        private final String cachePrefix;
        private final String namespace;

        public MemcacheCacheMetadata(String name, int expiration, String cachePrefix, String namespace) {
            this.name = name;
            this.expiration = expiration;

            StringBuilder sb = new StringBuilder(cachePrefix)
                    .append(KEY_DELIMITER)
                    .append(name)
                    .append(KEY_DELIMITER);

            this.cachePrefix = sb.toString();
            this.namespace = sb.append(namespace).toString();
        }

        public String name() {
            return name;
        }

        public int expiration() {
            return expiration;
        }

        public String cachePrefix() {
            return cachePrefix;
        }

        public String namespace() {
            return namespace;
        }
    }
}
