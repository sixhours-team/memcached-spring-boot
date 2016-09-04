package rs.symbolic.cache.memcached;

import net.spy.memcached.ClientMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Igor Bolic
 */
@ConfigurationProperties(prefix = "memcached.cache", ignoreInvalidFields = true)
public class MemcachedCacheProperties {

    /**
     * Memcached server host. The default is "localhost".
     */
    private String host = Default.HOST;

    /**
     * Memcached server port. The default is 11211.
     */
    private Integer port = Default.PORT;

    /**
     * Memcached client mode. The default mode is "static". Use "dynamic" mode for AWS node auto discovery.
     */
    private ClientMode mode = Default.CLIENT_MODE;

    /**
     * Cache expiration in seconds. The default is 60 seconds.
     */
    private Integer expiration = Default.EXPIRATION;

    /**
     * Cached object key prefix. The default is "memcached:spring-boot".
     */
    private String prefix = Default.PREFIX;

    /**
     * Namespace key value used for invalidation of cached values. The default value is "namespace-key".
     */
    private String namespace = Default.NAMESPACE_KEY;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ClientMode getMode() {
        return mode;
    }

    public void setMode(ClientMode mode) {
        this.mode = mode;
    }

    public Integer getExpiration() {
        return expiration;
    }

    public void setExpiration(Integer expiration) {
        this.expiration = expiration;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
