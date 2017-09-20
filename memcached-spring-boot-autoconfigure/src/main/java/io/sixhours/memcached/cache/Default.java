/*
 * Copyright 2017 Sixhours.
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

import io.sixhours.memcached.cache.MemcachedCacheProperties.Server;
import net.spy.memcached.ClientMode;

import java.util.Collections;
import java.util.List;

/**
 * Default cache configuration values.
 *
 * @author Igor Bolic
 */
final class Default {

    public static final List<Server> SERVERS = Collections.singletonList(new Server("localhost:11211"));
    public static final ClientMode CLIENT_MODE = ClientMode.Static;
    public static final int EXPIRATION = 60;
    public static final String PREFIX = "memcached:spring-boot";
    public static final String NAMESPACE = "namespace";

    private Default() {
        throw new AssertionError("Suppress default constructor");
    }
}
