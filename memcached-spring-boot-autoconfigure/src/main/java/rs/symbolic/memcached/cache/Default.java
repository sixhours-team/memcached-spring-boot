package io.sixhours.memcached.cache;

import net.spy.memcached.ClientMode;

/**
 * Default cache configuration values.
 *
 * @author Igor Bolic
 */
public final class Default {

    public static final String HOST = "localhost";
    public static final int PORT = 11211;
    public static final ClientMode CLIENT_MODE = ClientMode.Static;
    public static final int EXPIRATION = 60;
    public static final String PREFIX = "memcached:spring-boot";
    public static final String NAMESPACE = "namespace";

    private Default() {
        new AssertionError("Suppress default constructor");
    }
}
