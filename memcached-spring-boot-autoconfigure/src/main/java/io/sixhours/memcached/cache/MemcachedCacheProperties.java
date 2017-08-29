package io.sixhours.memcached.cache;

import lombok.Getter;
import lombok.Setter;
import net.spy.memcached.ClientMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
     * Memcached server list. The default is 'localhost:11211'.
     */
    private List<Server> servers = Default.SERVERS;

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

    /**
     * Populate server list from comma-separated list of hostname:port strings.
     *
     * @param value
     */
    public void setServers(String value) {
        this.servers = new ArrayList<>();
        if (!StringUtils.isEmpty(value)) {
            for (String s : value.split(",")) {
                this.servers.add(new Server(s.trim()));
            }
        }
    }

    @Getter
    public static class Server {
        private String host;
        private int port;

        public Server(String server) {
            String[] values = server.split(":");
            host = values[0];
            port = Integer.valueOf(values[1]);
        }
    }

}
