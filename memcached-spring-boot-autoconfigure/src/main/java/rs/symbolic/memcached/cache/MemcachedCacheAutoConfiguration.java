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
import java.util.Collections;

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
        final String host = cacheProperties.getHost();
        final int port = cacheProperties.getPort();
        final ClientMode mode = cacheProperties.getMode();

        return new MemcachedClient(new DefaultConnectionFactory(mode),
                Collections.singletonList(new InetSocketAddress(host, port)));
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
