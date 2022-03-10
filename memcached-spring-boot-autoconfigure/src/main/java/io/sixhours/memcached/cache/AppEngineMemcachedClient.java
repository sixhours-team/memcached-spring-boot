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

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@code AppEngine} memcached client implementation.
 *
 * @author Igor Bolic
 */
public class AppEngineMemcachedClient implements IMemcachedClient {
    private static final Log log = LogFactory.getLog(AppEngineMemcachedClient.class);

    private final MemcacheService service;

    public AppEngineMemcachedClient(MemcacheService service) {
        log.info("AppEngineMemcachedClient client initialized.");
        this.service = service;
    }

    @Override
    public MemcacheService nativeClient() {
        return this.service;
    }

    @Override
    public Object get(String key) {
        return this.service.get(key);
    }

    @Override
    public void set(String key, int exp, Object value) {
        this.service.put(key, value, Expiration.byDeltaSeconds(exp));
    }

    @Override
    public void touch(String key, int exp) {
        final MemcacheService.IdentifiableValue identifiable = this.service.getIdentifiable(key);
        this.service.putIfUntouched(key, identifiable, identifiable.getValue(), Expiration.byDeltaSeconds(exp));
    }

    @Override
    public void delete(String key) {
        this.service.delete(key);
    }

    @Override
    public void flush() {
        this.service.clearAll();
    }

    @Override
    public long incr(String key, int by) {
        return this.service.increment(key, by);
    }

    @Override
    public void shutdown() {
        // do nothing
    }
}
