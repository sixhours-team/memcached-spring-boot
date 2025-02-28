/*
 * Copyright 2016-2025 Sixhours
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@ActiveProfiles("config-test")
@ContextConfiguration(classes = { MemcachedCacheProperties.class }, initializers = { ConfigFileApplicationContextInitializer.class })
@EnableConfigurationProperties
public class MemcachedCachePropertiesTest {

    @Autowired
    private MemcachedCacheProperties memcachedCacheProperties;

    @Test
    public void whenGetProvider_thenCorrectValue() {
        MemcachedCacheProperties.Provider result = memcachedCacheProperties.getProvider();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.Provider.AWS);
    }

    @Test
    public void whenGetServers_thenCorrectValue() {
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
    public void whenGetAuthenticationUsername_thenCorrectValue() {
        String result = memcachedCacheProperties.getAuthentication().getUsername();

        assertThat(result)
                .isNotNull()
                .isEqualTo("user_config");
    }

    @Test
    public void whenGetAuthenticationPassword_thenCorrectValue() {
        String result = memcachedCacheProperties.getAuthentication().getPassword();

        assertThat(result)
                .isNotNull()
                .isEqualTo("pwd_config");
    }

    @Test
    public void whenGetAuthenticationMechanism_thenCorrectValue() {
        MemcachedCacheProperties.Authentication.Mechanism result =
                memcachedCacheProperties.getAuthentication().getMechanism();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.Authentication.Mechanism.PLAIN);
    }

    @Test
    public void whenGetPrefix_thenCorrectValue() {
        String result = memcachedCacheProperties.getPrefix();

        assertThat(result)
                .isNotNull()
                .isEqualTo("memcached:my-app");
    }

    @Test
    public void whenGetProtocol_thenCorrectValue() {
        MemcachedCacheProperties.Protocol result = memcachedCacheProperties.getProtocol();

        assertThat(result)
                .isNotNull()
                .isEqualByComparingTo(MemcachedCacheProperties.Protocol.BINARY);
    }

    @Test
    public void whenGetOperationTimeout_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getOperationTimeout();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(7200));
    }

    @Test
    public void whenGetServersRefreshInterval_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getServersRefreshInterval();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(30000));
    }

    @Test
    public void whenGetExpiration_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getExpiration();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofSeconds(86400));
    }

    @Test
    public void whenGetDisabledCaches_thenCorrectValue() {
        Set<String> result = memcachedCacheProperties.getDisabledCacheNames();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .contains("disabled_cache_name", "something");
    }

    @Test
    public void whenGetExpirationPerCache_thenCorrectValue() {
        Map<String, Duration> result = memcachedCacheProperties.getExpirationPerCache();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(4)
                .contains(
                    entry("cache_name1", Duration.ofSeconds(3600)),
                    entry("cache_name2", Duration.ofSeconds(108000)),
                    entry("cache_name3", Duration.ofSeconds(7200))
                );
    }

    @Test
    public void whenGetHashStrategy_thenCorrectValue() {
        MemcachedCacheProperties.HashStrategy result = memcachedCacheProperties.getHashStrategy();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.HashStrategy.KETAMA);
    }

    @Test
    public void whenGetMetricsCacheName_thenCorrectValue() {
        List<String> result = memcachedCacheProperties.getMetricsCacheNames();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(6)
                .containsOnly(
                        "cache_name1",
                        "cache_name2",
                        "cache_name3",
                        "cache_name4",
                        "cache_name5",
                        "cache_name6"
                );
    }
}
