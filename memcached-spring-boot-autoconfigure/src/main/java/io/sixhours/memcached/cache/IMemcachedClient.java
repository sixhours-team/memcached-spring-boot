/*
 * Copyright 2016-2021 Sixhours
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

/**
 * Memcached client interface.
 *
 * @author Igor Bolic
 */
public interface IMemcachedClient {

    Object nativeClient();

    Object get(String key);

    void set(String key, int exp, Object value);

    void touch(final String key, final int exp);

    void delete(String key);

    void flush();

    long incr(String key, int by);

    void shutdown();
}
