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

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.impl.MemcachedConnector;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
     * Asserts XMemcached implementation of {@link MemcachedClient} against default configuration values.
     *
     * @param memcachedClient {@link IMemcachedClient}
     */
    public static void assertMemcachedClient(IMemcachedClient memcachedClient) {
        assertMemcachedClient(memcachedClient, Default.PROTOCOL, Default.OPERATION_TIMEOUT);
    }

    /**
     * Asserts XMemcached implementation of {@link IMemcachedClient} against expected configuration values.
     *
     * @param memcachedClient {@link IMemcachedClient}
     * @param protocol        Expected protocol
     * @param servers         Expected server list
     */
    public static void assertMemcachedClient(IMemcachedClient memcachedClient, MemcachedCacheProperties.Protocol protocol, long operationTimeout, InetSocketAddress... servers) {
        assertThat(memcachedClient.nativeClient()).isInstanceOf(MemcachedClient.class);
        final MemcachedClient nativeClient = (MemcachedClient) memcachedClient.nativeClient();

        final MemcachedConnector connector = (MemcachedConnector) nativeClient.getConnector();
        final InetSocketAddress[] availableServers = nativeClient.getAvailableServers().toArray(new InetSocketAddress[]{});

        assertThat(nativeClient.getOpTimeout()).isEqualTo(operationTimeout);
        assertThat(connector.getProtocol().name().toUpperCase()).isEqualTo(protocol.name());

        final List<InetSocketAddress> actualServers = Arrays.asList(servers);

        if (actualServers.size() > 0) {
            assertThat(availableServers)
                    .as("The number of memcached node endpoints should match server list size")
                    .hasSize(servers.length);

            for (InetSocketAddress address : availableServers) {
                assertThat(actualServers).contains(address);
            }
        }
    }

    /**
     * Asserts Spymemcached implementation of {@link IMemcachedClient} against expected configuration values.
     *
     * @param memcachedClient {@link IMemcachedClient}
     * @param servers         Expected server list
     */
    public static void assertSpymemcachedClient(IMemcachedClient memcachedClient, long operationTimeout, InetSocketAddress... servers) {
        assertThat(memcachedClient.nativeClient()).isInstanceOf(net.spy.memcached.MemcachedClient.class);
        final net.spy.memcached.MemcachedClient nativeClient = (net.spy.memcached.MemcachedClient) memcachedClient.nativeClient();

        final SocketAddress[] availableServers = nativeClient.getAvailableServers().toArray(new SocketAddress[0]);
        assertThat(nativeClient.getOperationTimeout()).isEqualTo(operationTimeout);

        final List<InetSocketAddress> actualServers = Arrays.asList(servers);

        if (actualServers.size() > 0) {
            assertThat(availableServers)
                    .as("The number of memcached node endpoints should match server list size")
                    .hasSize(servers.length);

            for (SocketAddress availableServer : availableServers) {
                InetSocketAddress address = (InetSocketAddress) availableServer;
                assertThat(actualServers).contains(address);
            }
        }
    }
}
