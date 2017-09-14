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

import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the Memcached cache.
 * Creates {@link CacheManager} when caching is enabled via {@link EnableCaching}.
 *
 * @author Igor Bolic
 */
@Configuration
@ConditionalOnClass({MemcachedClient.class, CacheManager.class})
@ConditionalOnBean(CacheAspectSupport.class)
@ConditionalOnMissingBean({CacheManager.class, CacheResolver.class})
@EnableConfigurationProperties(MemcachedCacheProperties.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class MemcachedCacheAutoConfiguration {

    private final MemcachedCacheProperties cacheProperties;

    @Autowired
    public MemcachedCacheAutoConfiguration(MemcachedCacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    private MemcachedClient memcachedClient() throws IOException {
        final List<MemcachedCacheProperties.Server> servers = cacheProperties.getServers();
        final ClientMode mode = cacheProperties.getMode();

        final List<InetSocketAddress> socketAddresses = servers.stream()
                .map(s -> new InetSocketAddress(s.getHost(), s.getPort()))
                .collect(Collectors.toList());

        return new MemcachedClient(new DefaultConnectionFactory(mode), socketAddresses);
    }

    @Bean
    public MemcachedCacheManager cacheManager() throws IOException {
        final DisposableMemcachedCacheManager cacheManager = new DisposableMemcachedCacheManager(memcachedClient());

        cacheManager.setExpiration(cacheProperties.getExpiration());
        cacheManager.setPrefix(cacheProperties.getPrefix());
        cacheManager.setNamespace(cacheProperties.getNamespace());

        return cacheManager;
    }

    protected class DisposableMemcachedCacheManager extends MemcachedCacheManager implements DisposableBean {

        public DisposableMemcachedCacheManager(MemcachedClient memcachedClient) {
            super(memcachedClient);
        }

        @Override
        public void destroy() throws Exception {
            this.memcachedClient.shutdown();
        }
    }
}
