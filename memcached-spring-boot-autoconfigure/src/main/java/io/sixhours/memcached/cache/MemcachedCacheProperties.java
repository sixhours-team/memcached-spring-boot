package io.sixhours.memcached.cache;

import lombok.Getter;
import lombok.Setter;
import net.spy.memcached.ClientMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Memcached cache.
 *
 * @author Igor Bolic
 */
@ConfigurationProperties(prefix = "memcached.cache", ignoreInvalidFields = true)
@Getter
@Setter
public class MemcachedCacheProperties {

    /**
     * Memcached server host. The default is 'localhost'.
     */
    private String host = Default.HOST;

    /**
     * Memcached server port. The default is 11211.
     */
    private Integer port = Default.PORT;

    /**
     * Memcached client mode. The default mode is 'static'. Use 'dynamic' mode for AWS node auto discovery.
     */
    private ClientMode mode = Default.CLIENT_MODE;

    /**
     * Cache expiration in seconds. The default is 60 seconds.
     */
    private Integer expiration = Default.EXPIRATION;

    /**
     * Cached object key prefix. The default is 'memcached:spring-boot'.
     */
    private String prefix = Default.PREFIX;

    /**
     * Namespace key value used for invalidation of cached values. The default value is 'namespace'.
     */
    private String namespace = Default.NAMESPACE;

}
