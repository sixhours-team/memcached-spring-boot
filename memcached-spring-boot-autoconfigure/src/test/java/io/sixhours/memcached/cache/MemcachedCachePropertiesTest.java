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

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("config-test")
@ContextConfiguration(classes = { MemcachedCacheProperties.class }, initializers = { ConfigFileApplicationContextInitializer.class })
@EnableConfigurationProperties
public class MemcachedCachePropertiesTest {

    @Autowired
    private MemcachedCacheProperties memcachedCacheProperties;

    @Test
    public void whenGetMode_thenCorrectValue() {
        ClientMode result = memcachedCacheProperties.getMode();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(ClientMode.Dynamic);
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
    public void whenGetOperationTimeout_thenCorrectValue() {
        Long result = memcachedCacheProperties.getOperationTimeout();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(7200);
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
        assertThat(result.value()).isEqualByComparingTo(ConnectionFactoryBuilder.Protocol.valueOf(MemcachedCacheProperties.Protocol.BINARY.name()));
    }

    @Test
    public void whenGetExpiration_thenCorrectValue() {
        Integer result = memcachedCacheProperties.getExpiration();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(86400);
    }

    @Test
    public void whenGetExpirationPerCache_thenCorrectValue() {
        Map<String, Integer> result = memcachedCacheProperties.getExpirationPerCache();

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isEqualTo(3);
        // @formatter:off
        assertThat(result).contains(
                entry("cache_name1", 3600),
                entry("cache_name2", 108000),
                entry("cache_name3", 7200)
        );
        // @formatter:on
    }
}
