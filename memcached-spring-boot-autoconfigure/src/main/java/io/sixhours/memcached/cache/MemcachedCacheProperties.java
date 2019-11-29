/*
 * Copyright 2017 Sixhours.
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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.util.StringUtils;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactoryBuilder;

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
     * Cache expiration in seconds. The default is 60 seconds.
     */
    private Integer expiration = Default.EXPIRATION;

    private Map<String, Integer> expirations;

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
    private Long operationTimeout = Default.OPERATION_TIMEOUT;

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

    @DeprecatedConfigurationProperty(reason = "As of release {@code 1.3.0}. To be removed in next major release. This " +
            "value is expected to be extracted from property 'expirations'.", replacement = "memcached.cache.expirations")
    public Integer getExpiration() {
        return expiration;
    }

    public void setExpiration(Integer expiration) {
        this.expiration = expiration;
    }

    public Map<String, Integer> getExpirations() {
        return expirations;
    }

    public void setExpirations(String value) {
        this.expirations = new HashMap<>();

        // remove whitespaces from expirations value
        value = value.replaceAll("\\s+","");

        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Expiration list is empty");
        }

        final String[] expirationPerCacheNames = value.split("(?:\\s|,)+");

        Stream.of(expirationPerCacheNames).forEach(v -> {
            final int colonIndex = v.lastIndexOf(':');

            if (colonIndex < 1) {
                // global expiration (without colon in value)
                this.expiration = Integer.valueOf(v);
            } else {
                // expiration per cache name
                final String cacheName = v.substring(0, colonIndex);
                final String expirationValue = v.substring(colonIndex + 1);

                this.expirations.put(cacheName, Integer.valueOf(expirationValue));
            }
        });
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

    public Long getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(Long operationTimeout) {
        if (operationTimeout <= 0) {
            throw new IllegalArgumentException("Operation timeout must be greater then zero");
        }
        this.operationTimeout = operationTimeout;
    }

    public enum Protocol {
        TEXT, BINARY;

        public ConnectionFactoryBuilder.Protocol value() {
            return ConnectionFactoryBuilder.Protocol.valueOf(this.name());
        }
    }
}
