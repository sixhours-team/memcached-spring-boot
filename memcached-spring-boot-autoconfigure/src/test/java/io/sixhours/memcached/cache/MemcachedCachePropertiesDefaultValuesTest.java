/*
 * Copyright 2016-2023 Sixhours
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
import static org.assertj.core.api.Assertions.tuple;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { MemcachedCacheProperties.class }, initializers = { ConfigFileApplicationContextInitializer.class })
@EnableConfigurationProperties
public class MemcachedCachePropertiesDefaultValuesTest {

    @Autowired
    private MemcachedCacheProperties memcachedCacheProperties;

    @Test
    public void whenGetProvider_thenCorrectValue() {
        MemcachedCacheProperties.Provider result = memcachedCacheProperties.getProvider();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.Provider.STATIC);
    }

    @Test
    public void whenGetServers_thenCorrectValue() {
        List<InetSocketAddress> result = memcachedCacheProperties.getServers();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .extracting("hostName", "port")
                .containsExactly(
                        tuple("localhost", 11211)
                );
    }

    @Test
    public void whenGetAuthentication_thenNullValue() {
        MemcachedCacheProperties.Authentication result = memcachedCacheProperties.getAuthentication();

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getPassword()).isNull();
        assertThat(result.getMechanism())
                .isEqualTo(MemcachedCacheProperties.Authentication.Mechanism.PLAIN);
    }

    @Test
    public void whenGetPrefix_thenCorrectValue() {
        String result = memcachedCacheProperties.getPrefix();

        assertThat(result)
                .isNotNull()
                .isEqualTo("memcached:spring-boot");
    }

    @Test
    public void whenGetProtocol_thenCorrectValue() {
        MemcachedCacheProperties.Protocol result = memcachedCacheProperties.getProtocol();

        assertThat(result)
                .isNotNull()
                .isEqualByComparingTo(MemcachedCacheProperties.Protocol.TEXT);
    }

    @Test
    public void whenGetOperationTimeout_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getOperationTimeout();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(2500));
    }

    @Test
    public void whenGetDisabledCaches_thenCorrectValue() {
        Set<String> result = memcachedCacheProperties.getDisabledCacheNames();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void whenGetMetricsCaches_thenCorrectValue() {
        List<String> result = memcachedCacheProperties.getMetricsCacheNames();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void whenGetExpiration_thenCorrectValue() {
        Duration result = memcachedCacheProperties.getExpiration();

        assertThat(result)
                .isNotNull()
                .isEqualTo(Duration.ofMillis(0));
    }

    @Test
    public void whenGetExpirationPerCache_thenCorrectValue() {
        Map<String, Duration> result = memcachedCacheProperties.getExpirationPerCache();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void whenGetHashStrategy_thenCorrectValue() {
        MemcachedCacheProperties.HashStrategy result = memcachedCacheProperties.getHashStrategy();

        assertThat(result)
                .isNotNull()
                .isEqualTo(MemcachedCacheProperties.HashStrategy.STANDARD);
    }
}
