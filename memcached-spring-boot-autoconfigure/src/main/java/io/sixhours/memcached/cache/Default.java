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
public final class Default {

    public static final List<Server> SERVERS = Collections.singletonList(new Server("localhost:11211"));
    public static final ClientMode CLIENT_MODE = ClientMode.Static;
    public static final int EXPIRATION = 60;
    public static final String PREFIX = "memcached:spring-boot";
    public static final String NAMESPACE = "namespace";

    private Default() {
        throw new AssertionError("Suppress default constructor");
    }
}
