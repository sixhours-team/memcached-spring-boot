package io.sixhours.memcached.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;

public class MemcachedMetrics extends CacheMeterBinder {
  private final MemcachedCache memcachedCache;

  public MemcachedMetrics(MemcachedCache cache, String cacheName, Iterable<Tag> tags) {
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
    return null;
  }

  @Override
  protected long putCount() {
    return 0;
  }

  @Override
  protected void bindImplementationSpecificMetrics(MeterRegistry registry) {

  }
}
