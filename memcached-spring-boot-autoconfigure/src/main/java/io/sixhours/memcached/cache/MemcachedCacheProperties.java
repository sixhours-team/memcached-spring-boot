/**
 * Copyright 2016-2020 Sixhours
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sixhours.memcached.cache;

import io.micrometer.core.instrument.util.StringUtils;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.boot.convert.DurationUnit;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Memcached cache.
 *
 * @author Igor Bolic
 */
@ConfigurationProperties(prefix = "memcached.cache")
public class MemcachedCacheProperties {

    /**
     * Comma-separated list of hostname:port for memcached servers. The default hostname:port is 'localhost:11211'.
     */
    private List<InetSocketAddress> servers = Default.SERVERS;

    /**
     * Memcached client mode. The default mode is 'static'. Use 'dynamic' mode for AWS node auto discovery, or 'static'
     * if using multiple memcached servers.
     */
    private ClientMode mode = Default.CLIENT_MODE;

    /**
     * Cache expiration in seconds. The default is 0s, meaning the cache will never expire.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration expiration = Duration.ofSeconds(Default.EXPIRATION);

    private Map<String, Duration> expirationPerCache = new HashMap<>();

    /**
     * Cached object key prefix. The default is 'memcached:spring-boot'.
     */
    private String prefix = Default.PREFIX;

    /**
     * Memcached client protocol. Supports two main protocols: the classic text (ascii), and the newer binary protocol.
     * The default is 'text' protocol.
     */
    private Protocol protocol = Default.PROTOCOL;

    /**
     * Memcached client operation timeout in milliseconds. The default is 2500 milliseconds.
     */
    private Duration operationTimeout = Duration.ofMillis(Default.OPERATION_TIMEOUT);

    public List<InetSocketAddress> getServers() {
        return servers;
    }

    /**
     * Populate server list from comma-separated list of hostname:port strings.
     *
     * @param value Comma-separated list
     */
    public void setServers(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Server list is empty");
        }
        this.servers = AddrUtil.getAddresses(Arrays.asList(value.split(",")));
    }

    public ClientMode getMode() {
        return mode;
    }

    public void setMode(ClientMode mode) {
        this.mode = mode;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        validateExpiration(expiration);
        this.expiration = expiration;
    }

    public void setExpirationPerCache(Map<String, String> expirationPerCache) {
        if (expirationPerCache != null) {
            expirationPerCache.forEach((cacheName, expiration) -> {
                Duration exp = DurationStyle.detect(expiration).parse(expiration, ChronoUnit.SECONDS);
                validateExpiration(exp);
                this.expirationPerCache.put(cacheName, exp);
            });
        }
    }

    public Map<String, Duration> getExpirationPerCache() {
        return expirationPerCache;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Duration getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(Duration operationTimeout) {
        if (operationTimeout == Duration.ZERO) {
            throw new IllegalArgumentException("Operation timeout must be greater then zero");
        }
        this.operationTimeout = operationTimeout;
    }

    private void validateExpiration(Duration expiration) {
        if (expiration == null || expiration.toDays() > 30) {
            throw new IllegalStateException("Invalid expiration. It should not be null or greater than 30 days.");
        }
    }

    public enum Protocol {
        TEXT, BINARY;

        public ConnectionFactoryBuilder.Protocol value() {
            return ConnectionFactoryBuilder.Protocol.valueOf(this.name());
        }
    }
}
