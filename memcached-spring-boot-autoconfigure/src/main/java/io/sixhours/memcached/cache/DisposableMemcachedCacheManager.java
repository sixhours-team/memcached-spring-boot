package io.sixhours.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.DisposableBean;

class DisposableMemcachedCacheManager extends MemcachedCacheManager implements DisposableBean {

    public DisposableMemcachedCacheManager(MemcachedClient memcachedClient) {
        super(memcachedClient);
    }

    @Override
    public void destroy() {
        this.memcachedClient.shutdown();
    }
}
