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

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory for the {@link MemcachedCacheManager} instances.
 *
 * @author Igor Bolic
 */
public abstract class MemcachedCacheManagerFactory {

    protected final MemcachedCacheProperties properties;

    public MemcachedCacheManagerFactory(MemcachedCacheProperties properties) {
        this.properties = properties;
    }

    public MemcachedCacheManager create() throws IOException {
        final DisposableMemcachedCacheManager cacheManager = new DisposableMemcachedCacheManager(memcachedClient());

        cacheManager.setExpiration((int) properties.getExpiration().getSeconds());
        cacheManager.setExpirationPerCache(properties.getExpirationPerCache().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (int) e.getValue().getSeconds())));
        cacheManager.setConfigurationPerCache(properties.getConfigurationPerCache());
        cacheManager.setPrefix(properties.getPrefix());
        cacheManager.setNamespace(Default.NAMESPACE);
        cacheManager.setDisabledCacheNames(properties.getDisableCacheNames());

        return cacheManager;
    }

    abstract IMemcachedClient memcachedClient() throws IOException;
}
