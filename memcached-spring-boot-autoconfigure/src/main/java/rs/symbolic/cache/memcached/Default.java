package rs.symbolic.cache.memcached;

import net.spy.memcached.ClientMode;

/**
 * Constant values for the default configuration.
 *
 * @author Igor Bolic
 */
public class Default {

    public static final String HOST = "localhost";
    public static final int PORT = 11211;
    public static final ClientMode CLIENT_MODE = ClientMode.Static;
    public static final int EXPIRATION = 60;
    public static final String PREFIX = "memcached:spring-boot";
    public static final String NAMESPACE_KEY = "namespace-key";

    private Default() {
    }
}
