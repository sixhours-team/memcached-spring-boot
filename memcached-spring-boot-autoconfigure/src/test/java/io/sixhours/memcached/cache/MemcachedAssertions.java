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

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.impl.MemcachedConnector;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion methods for {@link MemcachedCacheManager} and {@link MemcachedClient}.
 *
 * @author Igor Bolic
 */
public final class MemcachedAssertions {

    /**
     * Asserts {@link MemcachedCacheManager} against the expected values.
     *
     * @param memcachedCacheManager {@link MemcachedCacheManager}
     * @param expiration            Expected expiration
     * @param prefix                Expected prefix
     * @param namespace             Expected namespace
     */
    public static void assertMemcachedCacheManager(MemcachedCacheManager memcachedCacheManager, int expiration, Map<String, Integer> expirations, String prefix, String namespace) {
        int actualExpiration = (int) ReflectionTestUtils.getField(memcachedCacheManager, "expiration");
        assertThat(actualExpiration).isEqualTo(expiration);

        Map<String, Integer> actualExpirations = (Map<String, Integer>) ReflectionTestUtils.getField(memcachedCacheManager, "expirationPerCache");
        assertThat(actualExpirations).isEqualTo(expirations);

        String actualPrefix = (String) ReflectionTestUtils.getField(memcachedCacheManager, "prefix");
        assertThat(actualPrefix).isEqualTo(prefix);

        String actualNamespace = (String) ReflectionTestUtils.getField(memcachedCacheManager, "namespace");
        assertThat(actualNamespace).isEqualTo(namespace);
    }

    /**
     * Asserts {@link MemcachedClient} against default configuration values.
     *
     * @param memcachedClient {@link MemcachedClient}
     */
    public static void assertMemcachedClient(IMemcachedClient memcachedClient) {
        assertMemcachedClient(memcachedClient, Default.PROTOCOL, Default.OPERATION_TIMEOUT);
    }

    /**
     * Asserts {@link MemcachedClient} against expected configuration values.
     *
     * @param memcachedClient {@link MemcachedClient}
     * @param protocol        Expected protocol
     * @param servers         Expected server list
     */
    public static void assertMemcachedClient(IMemcachedClient memcachedClient, MemcachedCacheProperties.Protocol protocol, long operationTimeout, InetSocketAddress... servers) {
        final MemcachedClient nativeCache = (MemcachedClient) memcachedClient.nativeCache();

        final MemcachedConnector connector = (MemcachedConnector) nativeCache.getConnector();
        final InetSocketAddress[] availableServers = nativeCache.getAvailableServers().toArray(new InetSocketAddress[]{});

        assertThat(nativeCache.getOpTimeout()).isEqualTo(operationTimeout);
        assertThat(connector.getProtocol().name().toUpperCase()).isEqualTo(protocol.name());

        final List<InetSocketAddress> actualServers = Arrays.asList(servers);

        if (actualServers.size() > 0) {
            assertThat(availableServers)
                    .as("The number of memcached node endpoints should match server list size")
                    .hasSize(servers.length);

            for (int i = 0; i < availableServers.length; i++) {
                InetSocketAddress address = availableServers[i];

                assertThat(actualServers).contains(address);
            }
        }
    }
}
