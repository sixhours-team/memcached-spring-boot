package io.sixhours.memcached.cache;

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import net.spy.memcached.protocol.ascii.AsciiOperationFactory;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class MemcachedAssertions {

    public static void assertMemcachedCacheManager(MemcachedCacheManager memcachedCacheManager, int expiration, String prefix, String namespace) {
        int actualExpiration = (int) ReflectionTestUtils.getField(memcachedCacheManager, "expiration");
        assertThat(actualExpiration).isEqualTo(expiration);

        String actualPrefix = (String) ReflectionTestUtils.getField(memcachedCacheManager, "prefix");
        assertThat(actualPrefix).isEqualTo(prefix);

        String actualNamespace = (String) ReflectionTestUtils.getField(memcachedCacheManager, "namespace");
        assertThat(actualNamespace).isEqualTo(namespace);
    }

    public static void assertMemcachedClient(MemcachedClient memcachedClient) {
        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Default.PROTOCOL, Default.SERVERS.get(0));
    }

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
}
