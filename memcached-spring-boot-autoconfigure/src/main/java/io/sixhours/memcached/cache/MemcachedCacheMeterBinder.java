package io.sixhours.memcached.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;

import java.util.Map;
import java.util.function.ToLongFunction;

public class MemcachedCacheMeterBinder extends CacheMeterBinder {
  private final MemcachedCache memcachedCache;

  public MemcachedCacheMeterBinder(MemcachedCache cache, String cacheName, Iterable<Tag> tags) {
    super(cache, cacheName, tags);
    this.memcachedCache = cache;
  }

  @Override
  protected Long size() {
    return null;
  }

  @Override
  protected long hitCount() {
    return memcachedCache.hits();
  }

  @Override
  protected Long missCount() {
    return memcachedCache.misses();
  }

  @Override
  protected Long evictionCount() {
    return memcachedCache.evictions();
  }

  @Override
  protected long putCount() {
    return memcachedCache.puts();
  }

  @Override
  protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
    registry.gauge("available_servers_count", memcachedCache.getNativeCache().getAvailableServers().size());
  }
}
