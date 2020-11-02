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

import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Memcached {@link CacheMeterBinderProvider} bean.
 *
 * @author Sasa Bolic
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(MemcachedCacheManager.class)
@ConditionalOnClass(MeterBinder.class)
public class MemcachedCacheMeterBinderProviderConfiguration {

    @Bean
    public MemcachedCacheMeterBinderProvider memcachedCacheMeterBinderProvider() {
        return new MemcachedCacheMeterBinderProvider();
    }
}
