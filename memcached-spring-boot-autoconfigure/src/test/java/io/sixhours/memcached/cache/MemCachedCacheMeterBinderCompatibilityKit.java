package io.sixhours.memcached.cache;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinderCompatibilityKit;
import net.spy.memcached.MemcachedClient;
import org.mockito.Mockito;

import java.util.HashMap;

public class MemcachedCacheMeterBinderCompatibilityKit extends CacheMeterBinderCompatibilityKit {
  private MemcachedCache memcachedCache;

  @Override
  public CacheMeterBinder binder() {
    MemcachedClient memcachedClient = Mockito.mock(MemcachedClient.class);
    HashMap<String,Object> cache = new HashMap<>();
    Mockito.when(memcachedClient.set(Mockito.anyString(), Mockito.anyInt(), Mockito.any()))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          cache.put(key, invocation.getArgument(2));
          return null;
        });
    Mockito.when(memcachedClient.get(Mockito.anyString()))
        .thenAnswer(invocation -> cache.get(invocation.getArgument(0)));

    MemcachedCacheManager memcachedCacheManager = new MemcachedCacheManager(memcachedClient);
    memcachedCache = (MemcachedCache) memcachedCacheManager.getCache("mycache");
    return new MemcachedCacheMeterBinder(memcachedCache, "mycache", Tags.empty());
  }

  @Override
  public void put(String key, String value) {
    memcachedCache.put(key, value);
  }

  @Override
  public String get(String key) {
    return memcachedCache.get(key, String.class);
  }
}
