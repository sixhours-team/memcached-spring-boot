/*
 * Copyright 2016-2026 Sixhours
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemcachedCachePropertiesValidationTest {

    private MemcachedCacheProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MemcachedCacheProperties();
    }

    @Test
    void whenSetServersThenValidationOk() {
        properties.setServers("example1.com:1122");

        assertThat(properties.getServers()).hasSize(1);
        assertThat(properties.getServers().get(0)).isNotNull();
        assertThat(properties.getServers().get(0).getHostName()).isNotNull();
        assertThat(properties.getServers().get(0).getHostName()).isEqualTo("example1.com");
        assertThat(properties.getServers().get(0).getPort()).isEqualTo(1122);
    }

    @Test
    void whenSetNullServersThenValidationFails() {
        assertThatThrownBy(() -> properties.setServers(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Server list is empty");
    }

    @Test
    void whenSetEmptyServersThenValidationFails() {
        assertThatThrownBy(() -> properties.setServers(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Server list is empty");
    }

    @Test
    void whenSetBlankServersThenValidationFails() {
        assertThatThrownBy(() -> properties.setServers(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Server list is empty");
    }

    @Test
    void whenSetAuthenticationThenValidationOk() {
        MemcachedCacheProperties.Authentication authentication = new MemcachedCacheProperties.Authentication();
        authentication.setUsername("test-user");
        authentication.setPassword("test-pwd");
        authentication.setMechanism(MemcachedCacheProperties.Authentication.Mechanism.CRAM_MD5);

        properties.setAuthentication(authentication);

        assertThat(properties.getAuthentication()).isNotNull();
        assertThat(properties.getAuthentication().getUsername()).isEqualTo("test-user");
        assertThat(properties.getAuthentication().getPassword()).isEqualTo("test-pwd");
        assertThat(properties.getAuthentication().getMechanism())
                .isEqualTo(MemcachedCacheProperties.Authentication.Mechanism.CRAM_MD5);
    }

    @Test
    void whenSetAuthenticationWithNullValuesThenValidationOk() {
        MemcachedCacheProperties.Authentication authentication = new MemcachedCacheProperties.Authentication();
        authentication.setUsername(null);
        authentication.setPassword(null);
        authentication.setMechanism(null);

        properties.setAuthentication(authentication);

        assertThat(properties.getAuthentication()).isNotNull();
        assertThat(properties.getAuthentication().getUsername()).isNull();
        assertThat(properties.getAuthentication().getPassword()).isNull();
        assertThat(properties.getAuthentication().getMechanism())
                .isEqualTo(MemcachedCacheProperties.Authentication.Mechanism.PLAIN);
    }

    @Test
    void whenSetDisabledCacheNamesThenValidationOk() {
        Set<String> names = new HashSet<>();
        names.add("cache-1");
        properties.setDisabledCacheNames(names);

        assertThat(properties.getDisabledCacheNames()).hasSize(1);
        assertThat(properties.getDisabledCacheNames()).containsExactly("cache-1");
    }

    @Test
    void whenSetEmptyDisabledCacheNamesThenValidationOk() {
        properties.setDisabledCacheNames(new HashSet<>());

        assertThat(properties.getDisabledCacheNames()).isEmpty();
    }

    @Test
    void whenSetMetricsCacheNamesThenValidationOk() {
        List<String> names = new ArrayList<>();
        names.add("cache-1");
        properties.setMetricsCacheNames(names);

        assertThat(properties.getMetricsCacheNames()).hasSize(1);
        assertThat(properties.getMetricsCacheNames()).containsExactly("cache-1");
    }

    @Test
    void whenSetEmptyMetricsCacheNamesThenValidationOk() {
        properties.setMetricsCacheNames(new ArrayList<>());

        assertThat(properties.getMetricsCacheNames()).isEmpty();
    }

    @Test
    void whenSetNullMetricsCacheNamesThenValidationOk() {
        properties.setMetricsCacheNames(null);

        assertThat(properties.getMetricsCacheNames()).isNull();
    }

    @Test
    void whenSetProviderThenValidationOk() {
        properties.setProvider(MemcachedCacheProperties.Provider.STATIC);

        assertThat(properties.getProvider()).isEqualTo(MemcachedCacheProperties.Provider.STATIC);
    }

    @Test
    void whenSetNullProviderThenValidationOk() {
        properties.setProvider(null);

        assertThat(properties.getProvider()).isNull();
    }

    @Test
    void whenSetZeroExpirationThenValidationOk() {
        properties.setExpiration(Duration.ZERO);

        assertThat(properties.getExpiration()).isEqualTo(Duration.ZERO);
    }

    @Test
    void whenSetPositiveExpirationThenValidationOk() {
        properties.setExpiration(Duration.ofDays(40));

        assertThat(properties.getExpiration()).isEqualTo(Duration.ofDays(40));
    }

    @Test
    void whenSetNullExpirationThenValidationFails() {
        assertThatThrownBy(() -> properties.setExpiration(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid expiration. Duration must be greater than or equal to 0 (zero) seconds.");
    }

    @Test
    void whenSetNegativeExpirationThenValidationFails() {
        Duration negativeExpiration = Duration.ofSeconds(-1);

        assertThatThrownBy(() -> properties.setExpiration(negativeExpiration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid expiration. Duration must be greater than or equal to 0 (zero) seconds.");
    }

    @Test
    void whenSetExpirationPerCacheThenValidationOk() {
        Map<String, String> perCache = new HashMap<>();
        perCache.put("cache-1", "1d");
        perCache.put("cache-2", "15h");
        perCache.put("cache-3", "30d");
        perCache.put("cache-4", "40d");

        properties.setExpirationPerCache(perCache);

        assertThat(properties.getExpirationPerCache()).hasSize(4);
        assertThat(properties.getExpirationPerCache()).containsEntry("cache-1", Duration.ofDays(1));
        assertThat(properties.getExpirationPerCache()).containsEntry("cache-2", Duration.ofHours(15));
        assertThat(properties.getExpirationPerCache()).containsEntry("cache-3", Duration.ofDays(30));
        assertThat(properties.getExpirationPerCache()).containsEntry("cache-4", Duration.ofDays(40));
    }

    @Test
    void whenSetNullExpirationPerCacheThenValidationOk() {
        properties.setExpirationPerCache(null);

        assertThat(properties.getExpirationPerCache()).isEmpty();
    }

    @Test
    void whenSetNegativeExpirationPerCacheThenValidationFails() {
        Map<String, String> perCache = new HashMap<>();
        perCache.put("cache-1", "-1d");
        perCache.put("cache-4", "40d");

        assertThatThrownBy(() -> properties.setExpirationPerCache(perCache))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid expiration. Duration must be greater than or equal to 0 (zero) seconds.");
    }

    @Test
    void whenSetPrefixThenValidationOk() {
        properties.setPrefix("my-cache");

        assertThat(properties.getPrefix()).isEqualTo("my-cache");
    }

    @Test
    void whenSetNullPrefixThenValidationOk() {
        properties.setPrefix(null);

        assertThat(properties.getPrefix()).isNull();
    }

    @Test
    void whenSetProtocolThenValidationOk() {
        properties.setProtocol(MemcachedCacheProperties.Protocol.TEXT);

        assertThat(properties.getProtocol()).isEqualTo(MemcachedCacheProperties.Protocol.TEXT);
    }

    @Test
    void whenSetNullProtocolThenValidationOk() {
        properties.setProtocol(null);

        assertThat(properties.getProtocol()).isNull();
    }

    @Test
    void whenSetOperationTimeoutThenValidationOk() {
        properties.setOperationTimeout(Duration.ofHours(4));

        assertThat(properties.getOperationTimeout()).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void whenSetNullOperationTimeoutThenValidationFails() {
        assertThatThrownBy(() -> properties.setOperationTimeout(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Operation timeout must be greater then zero");
    }

    @Test
    void whenSetZeroOperationTimeoutThenValidationFails() {
        assertThatThrownBy(() -> properties.setOperationTimeout(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Operation timeout must be greater then zero");
    }

    @Test
    void whenSetNegativeOperationTimeoutThenValidationFails() {
        Duration negativeOperationTimeout = Duration.ofDays(-1);

        assertThatThrownBy(() -> properties.setOperationTimeout(negativeOperationTimeout))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Operation timeout must be greater then zero");
    }

    @Test
    void whenSetServersRefreshIntervalThenValidationOk() {
        properties.setServersRefreshInterval(Duration.ofHours(4));

        assertThat(properties.getServersRefreshInterval()).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void whenSetNullServersRefreshIntervalThenValidationFails() {
        assertThatThrownBy(() -> properties.setServersRefreshInterval(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Servers refresh interval must be greater then zero");
    }

    @Test
    void whenSetZeroServersRefreshIntervalThenValidationFails() {
        assertThatThrownBy(() -> properties.setServersRefreshInterval(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Servers refresh interval must be greater then zero");
    }

    @Test
    void whenSetNegativeServersRefreshIntervalThenValidationFails() {
        Duration negativeServersRefreshInterval = Duration.ofDays(-1);

        assertThatThrownBy(() -> properties.setServersRefreshInterval(negativeServersRefreshInterval))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Servers refresh interval must be greater then zero");
    }

    @Test
    void whenSetHashStrategyThenValidationOk() {
        properties.setHashStrategy(MemcachedCacheProperties.HashStrategy.KETAMA);

        assertThat(properties.getHashStrategy()).isEqualTo(MemcachedCacheProperties.HashStrategy.KETAMA);
    }

    @Test
    void whenSetNullHashStrategyThenValidationOk() {
        properties.setHashStrategy(null);

        assertThat(properties.getHashStrategy()).isNull();
    }
}