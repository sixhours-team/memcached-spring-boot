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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import net.spy.memcached.MemcachedClient;

/**
 * Collect metrics on Memcached caches.
 *
 * @author Sasa Bolic
 */
public class MemcachedCacheMetrics extends CacheMeterBinder {
    private final MemcachedCache cache;

    public MemcachedCacheMetrics(MemcachedCache cache, String cacheName, Iterable<Tag> tags) {
        super(cache, cacheName, tags);
        this.cache = cache;
    }

    @Override
    protected Long size() {
        return null;
    }

    @Override
    protected long hitCount() {
        return cache.hits();
    }

    @Override
    protected Long missCount() {
        return cache.misses();
    }

    @Override
    protected Long evictionCount() {
        return cache.evictions();
    }

    @Override
    protected long putCount() {
        return cache.puts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        if (cache.getNativeCache() instanceof MemcachedClient) {
            final MemcachedClient memcachedClient = cache.getNativeCache();

            registry.gauge("available_servers_count", memcachedClient.getAvailableServers().size());
        }
    }
}
