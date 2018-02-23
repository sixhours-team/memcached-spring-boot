package io.sixhours.memcached.cache;

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class MemcachedCacheManagerFactory {

    private final MemcachedCacheProperties properties;

    public MemcachedCacheManagerFactory(MemcachedCacheProperties properties) {
        this.properties = properties;
    }

    public MemcachedCacheManager create() throws IOException {
        final DisposableMemcachedCacheManager cacheManager = new DisposableMemcachedCacheManager(memcachedClient());

        cacheManager.setExpiration(properties.getExpiration());
        cacheManager.setPrefix(properties.getPrefix());
        cacheManager.setNamespace(properties.getNamespace());

        return cacheManager;
    }

    private MemcachedClient memcachedClient() throws IOException {
        final List<InetSocketAddress> servers = properties.getServers();
        final ClientMode mode = properties.getMode();
        final MemcachedCacheProperties.Protocol protocol = properties.getProtocol();

        final ConnectionFactoryBuilder connectionFactoryBuilder = new ConnectionFactoryBuilder()
                .setClientMode(mode)
                .setProtocol(protocol.value());

        return new MemcachedClient(connectionFactoryBuilder.build(), servers);
    }

    protected class DisposableMemcachedCacheManager extends MemcachedCacheManager implements DisposableBean {

        public DisposableMemcachedCacheManager(MemcachedClient memcachedClient) {
            super(memcachedClient);
        }

        @Override
        public void destroy() {
            this.memcachedClient.shutdown();
        }
    }
}
