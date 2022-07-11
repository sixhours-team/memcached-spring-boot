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

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;

/**
 * Memcached {@link CacheMeterBinderProvider}.
 */
public class MemcachedCacheMeterBinderProvider implements CacheMeterBinderProvider<MemcachedCache> {

    @Override
    public CacheMeterBinder getMeterBinder(MemcachedCache memcachedCache, Iterable<Tag> tags) {
        return new MemcachedCacheMetrics(memcachedCache, memcachedCache.getName(), tags);
    }
}
