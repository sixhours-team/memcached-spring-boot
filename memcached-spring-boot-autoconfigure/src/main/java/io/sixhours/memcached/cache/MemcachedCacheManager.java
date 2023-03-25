/*
 * Copyright 2016-2023 Sixhours
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

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * {@link CacheManager} implementation for Memcached.
 * <p>
 * By default appends prefix {@code memcached:spring-boot} and uses namespace key value of {@code namespace-key}
 * to avoid clashes with other data that might be kept in the cache. Custom prefix can be specified
 * in Spring configuration file e.g.
 * <br><br>
 * <code>
 * memcached.cache.prefix=custom-prefix<br>
 * memcached.cache.namespace=custom-namespace-key
 * </code>
 *
 * @author Igor Bolic
 * @author Sasa Bolic
 */
public class MemcachedCacheManager extends AbstractTransactionSupportingCacheManager {

    private static final Logger log = Logger.getLogger(MemcachedCacheManager.class.getName());

    final IMemcachedClient memcachedClient;

    private int expiration = Default.EXPIRATION;
    private String prefix = Default.PREFIX;
    private String namespace = Default.NAMESPACE;
    private Map<String, Integer> expirationPerCache;
    private List<String> metricsCacheNames = Collections.emptyList();
    private Set<String> disabledCacheNames = new HashSet<>();
    private Clock clock = Clock.systemUTC();

    /**
     * Construct a {@link MemcachedCacheManager}
     *
     * @param memcachedClient {@link IMemcachedClient}
     */
    public MemcachedCacheManager(IMemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        List<MemcachedCache> caches = new ArrayList<>();

        for (String metricsCacheName : metricsCacheNames) {
            caches.add(createCache(metricsCacheName));
        }

        return caches;
    }

    @Override
    public Cache getCache(String name) {
        if (disabledCacheNames.contains(name)) {
            log.warning(() -> String.format("Ignoring cache '%s' because it is on the disabled cache names.", name));
            return new NoOpCache(name);
        }

        return super.getCache(name);
    }

    @Override
    protected MemcachedCache getMissingCache(String name) {
        return createCache(name);
    }

    private MemcachedCache createCache(String name) {
        int cacheExpiration = determineExpiration(name);
        return new MemcachedCache(name, memcachedClient, cacheExpiration, prefix, namespace, clock);
    }

    private int determineExpiration(String name) {
        return Optional.ofNullable(expirationPerCache).map(e -> e.get(name))
                .orElse(this.expiration);
    }

    /**
     * Sets global expiration for all cache names.
     * Custom expiration per cache is used in case it is defined by {@code expirationPerCache} {@link Map} property.
     *
     * @param expiration the expiration
     */
    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets expiration time for cache keys.
     *
     * @param expirationPerCache {@link Map} of expiration times per cache key
     */
    public void setExpirationPerCache(Map<String, Integer> expirationPerCache) {
        this.expirationPerCache = (expirationPerCache != null ? new ConcurrentHashMap<>(expirationPerCache) : null);
    }

    /**
     * Sets cache names for which metrics will be collected.
     *
     * @param metricsCacheNames the metrics cache names
     */
    public void setMetricsCacheNames(List<String> metricsCacheNames) {
        if (metricsCacheNames != null) {
            this.metricsCacheNames = metricsCacheNames;
        }
    }

    public IMemcachedClient client() {
        return this.memcachedClient;
    }

    public void setDisabledCacheNames(Set<String> disabledCacheNames) {
        this.disabledCacheNames = disabledCacheNames;
    }

    public Set<String> getDisabledCacheNames() {
        return disabledCacheNames;
    }

    /**
     * Overrides the default value of UTC timezone clock. Should be used only for tests in order not to rely
     * on mocking frameworks, since mocking might not work with all JDK versions the tests are run on.
     *
     * @param clock The fixed clock instance
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
