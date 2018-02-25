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

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import net.spy.memcached.protocol.ascii.AsciiOperationFactory;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion methods for tests.
 *
 * @author Igor Bolic
 */
public final class TestAssertions {

    /**
     * Asserts {@link MemcachedCacheManager} against the expected values.
     *
     * @param memcachedCacheManager {@link MemcachedCacheManager}
     * @param expiration            Expected expiration
     * @param prefix                Expected prefix
     * @param namespace             Expected namespace
     */
    public static void assertMemcachedCacheManager(MemcachedCacheManager memcachedCacheManager, int expiration, String prefix, String namespace) {
        int actualExpiration = (int) ReflectionTestUtils.getField(memcachedCacheManager, "expiration");
        assertThat(actualExpiration).isEqualTo(expiration);

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
    public static void assertMemcachedClient(MemcachedClient memcachedClient) {
        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Default.PROTOCOL, Default.SERVERS.get(0));
    }

    /**
     * Asserts {@link MemcachedClient} against expected configuration values.
     *
     * @param memcachedClient {@link MemcachedClient}
     * @param clientMode      Expected client mode
     * @param protocol        Expected protocol
     * @param servers         Expected server list
     */
    public static void assertMemcachedClient(MemcachedClient memcachedClient, ClientMode clientMode, MemcachedCacheProperties.Protocol protocol, InetSocketAddress... servers) {
        List<NodeEndPoint> nodeEndPoints = (List<NodeEndPoint>) memcachedClient.getAllNodeEndPoints();

        assertThat(nodeEndPoints)
                .as("The number of memcached node endpoints should match server list size")
                .hasSize(servers.length);

        ConnectionFactory cf = (ConnectionFactory) ReflectionTestUtils.getField(memcachedClient, "connFactory");

        for (int i = 0; i < nodeEndPoints.size(); i++) {
            NodeEndPoint nodeEndPoint = nodeEndPoints.get(i);
            InetSocketAddress server = servers[i];

            String host = server.getHostString();
            int port = server.getPort();

            assertThat(host.matches("\\w+") ? nodeEndPoint.getHostName() : nodeEndPoint.getIpAddress())
                    .as("Memcached node endpoint host is incorrect")
                    .isEqualTo(host);
            assertThat(nodeEndPoint.getPort())
                    .as("Memcached node endpoint port is incorrect")
                    .isEqualTo(port);
        }

        assertThat(cf.getClientMode())
                .as("Memcached node endpoint mode is incorrect")
                .isEqualTo(clientMode);

        assertThat(cf.getOperationFactory())
                .as("Memcached node endpoint protocol is incorrect")
                .isInstanceOf(protocol == MemcachedCacheProperties.Protocol.TEXT ? AsciiOperationFactory.class : BinaryOperationFactory.class);
    }

    /**
     * Asserts that object is annotated with given {@code annotationClass}.
     *
     * @param object          {@code java.lang.Object}
     * @param annotationClass The class object of the annotation type
     * @param <T>             The type of the annotation to query for
     */
    public static <T extends Annotation> void assertAnnotation(Object object, Class<T> annotationClass) {
        assertAnnotation(object.getClass(), annotationClass);
    }

    /**
     * Asserts that class is annotated with given {@code annotationClass}.
     *
     * @param clazz           {@code java.lang.Class}
     * @param annotationClass The class object of the annotation type
     * @param <T>             The type of the annotation to query for
     */
    public static <T extends Annotation> void assertAnnotation(Class<?> clazz, Class<T> annotationClass) {
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        assertThat(annotations).isNotEmpty();
        assertThat(annotations).hasSize(1);
        assertThat(annotations[0].annotationType()).isEqualTo(annotationClass);
    }
}
