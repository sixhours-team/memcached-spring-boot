/*
 * Copyright 2016-2026 Sixhours
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

import net.rubyeye.xmemcached.MemcachedClientBuilder;

/**
 * A customizer interface for beans that want to adjust the {@link MemcachedClientBuilder}, allowing fine-tuning of the
 * auto-configuration before creating a {@link net.rubyeye.xmemcached.MemcachedClient}.
 */
@FunctionalInterface
public interface XMemcachedClientCustomizer {

    /**
     * Customize the given {@link MemcachedClientBuilder}.
     *
     * @param builder the builder
     */
    void customize(MemcachedClientBuilder builder);
}
