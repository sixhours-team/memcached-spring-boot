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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("config-per-cache-test")
@ContextConfiguration(classes = { MemcachedCacheProperties.class }, initializers = { ConfigFileApplicationContextInitializer.class })
@EnableConfigurationProperties
public class MemcachedCachePropertiesConfigPerCacheTest {

    @Autowired
    private MemcachedCacheProperties memcachedCacheProperties;

    @Test
    public void whenGetProvider_thenCorrectValue() {
        MemcachedCacheProperties.Provider result = memcachedCacheProperties.getProvider();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(MemcachedCacheProperties.Provider.APPENGINE);
    }

    @Test
    public void whenGetServers_thenCorrectValue() {
        List<InetSocketAddress> result = memcachedCacheProperties.getServers();

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isEqualTo(2);
        // @formatter:off
        assertThat(result).extracting("hostName", "port")
                .containsExactly(
                        tuple("example1.com", 12345),
                        tuple("example2.com", 12346)
                );
        // @formatter:on
    }

    @Test
    public void whenGetPrefix_thenCorrectValue() {
        String result = memcachedCacheProperties.getPrefix();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("memcached:my-app");
    }

    @Test
    public void whenGetProtocol_thenCorrectValue() {
        MemcachedCacheProperties.Protocol result = memcachedCacheProperties.getProtocol();

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(MemcachedCacheProperties.Protocol.BINARY);
    }

    @Test
    public void whenGetOperationTimeout_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getOperationTimeout();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(Duration.ofMillis(2000));
    }

    @Test
    public void whenGetServersRefreshInterval_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getServersRefreshInterval();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(Duration.ofMillis(30000));
    }

    @Test
    public void whenGetExpiration_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getExpiration();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(Duration.ofMillis(86400000));
    }

    @Test
    public void whenGetConfigurationPerCache_thenCorrectValue() {
        Map<String, MemcachedCacheProperties.CacheConfig> result = memcachedCacheProperties.getConfigurationPerCache();

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isEqualTo(6);

        assertThat(result.get("cache_name1")).isNotNull();
        assertThat(result.get("cache_name1").getExpiration()).isEqualByComparingTo(Duration.ofMinutes(2));
        assertThat(result.get("cache_name1").isMetricsEnabled()).isTrue();
        assertThat(result.get("cache_name1").isDisabled()).isFalse();

        assertThat(result.get("cache_name2")).isNotNull();
        assertThat(result.get("cache_name2").getExpiration()).isEqualByComparingTo(Duration.ofHours(30));
        assertThat(result.get("cache_name2").isMetricsEnabled()).isFalse();
        assertThat(result.get("cache_name2").isDisabled()).isFalse();

        assertThat(result.get("cache_name3")).isNotNull();
        assertThat(result.get("cache_name3").getExpiration()).isEqualByComparingTo(Duration.ofHours(2));
        assertThat(result.get("cache_name3").isMetricsEnabled()).isFalse();
        assertThat(result.get("cache_name3").isDisabled()).isFalse();

        assertThat(result.get("cache_name4")).isNotNull();
        assertThat(result.get("cache_name4").getExpiration()).isEqualByComparingTo(Duration.ofSeconds(0));
        assertThat(result.get("cache_name4").isMetricsEnabled()).isTrue();

        assertThat(result.get("cache_name5")).isNotNull();
        assertThat(result.get("cache_name5").getExpiration()).isEqualByComparingTo(Duration.ofSeconds(0));
        assertThat(result.get("cache_name5").isMetricsEnabled()).isFalse();
        assertThat(result.get("cache_name5").isDisabled()).isTrue();

        assertThat(result.get("cache_name6")).isNotNull();
        assertThat(result.get("cache_name6").getExpiration()).isEqualByComparingTo(Duration.ofMinutes(2));
        assertThat(result.get("cache_name6").isMetricsEnabled()).isTrue();
        assertThat(result.get("cache_name6").isDisabled()).isTrue();
    }
}
