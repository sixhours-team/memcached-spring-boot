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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("config-duration-test")
@ContextConfiguration(classes = {MemcachedCacheProperties.class}, initializers = {ConfigDataApplicationContextInitializer.class})
@EnableConfigurationProperties
class MemcachedCachePropertiesDurationTest {

    @Autowired
    private MemcachedCacheProperties memcachedCacheProperties;

    @Test
    void whenGetProvider_thenCorrectValue() {
        MemcachedCacheProperties.Provider result = memcachedCacheProperties.getProvider();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.Provider.APPENGINE);
    }

    @Test
    void whenGetServers_thenCorrectValue() {
        List<InetSocketAddress> result = memcachedCacheProperties.getServers();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .extracting("hostName", "port")
                .containsExactly(
                        tuple("example1.com", 12345),
                        tuple("example2.com", 12346)
                );
    }

    @Test
    void whenGetPrefix_thenCorrectValue() {
        String result = memcachedCacheProperties.getPrefix();

        assertThat(result)
                .isNotNull()
                .isEqualTo("memcached:my-app");
    }

    @Test
    void whenGetProtocol_thenCorrectValue() {
        MemcachedCacheProperties.Protocol result = memcachedCacheProperties.getProtocol();

        assertThat(result)
                .isNotNull()
                .isEqualByComparingTo(MemcachedCacheProperties.Protocol.BINARY);
    }

    @Test
    void whenGetOperationTimeout_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getOperationTimeout();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(2000));
    }

    @Test
    void whenGetServersRefreshInterval_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getServersRefreshInterval();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(30000));
    }

    @Test
    void whenGetExpiration_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getExpiration();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(86400000));
    }

    @Test
    void whenGetExpirationPerCache_thenCorrectValue() {
        Map<String, Duration> result = memcachedCacheProperties.getExpirationPerCache();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .contains(
                    entry("cache_name1", Duration.ofMillis(120000)),
                    entry("cache_name2", Duration.ofMillis(108000000)),
                    entry("cache_name3", Duration.ofMillis(7200000))
                );
    }
}
