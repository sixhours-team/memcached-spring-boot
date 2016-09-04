package rs.symbolic.cache.memcached;

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Memcached auto-configuration tests
 *
 * @author Igor Bolic
 */
public class MemcachedAutoConfigurationTest {

    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    @After
    public void teardown() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Test
    public void thatMemcachedWithDefaultConfigurationIsLoaded() throws Exception {
        loadContext();

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.HOST, Default.PORT, Default.CLIENT_MODE);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE_KEY);
    }

    @Test
    public void thatMemcachedWithCustomConfigurationIsLoaded() throws Exception {
        loadContext("memcached.cache.host=192.168.99.100",
                "memcached.cache.port=11212",
                "memcached.cache.mode=dynamic",
                "memcached.cache.expiration=3600",
                "memcached.cache.prefix=custom:prefix",
                "memcached.cache.namespace=custom_namespace");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, "192.168.99.100", 11212, ClientMode.Dynamic);
        assertMemcachedCacheManager(memcachedCacheManager, 3600, "custom:prefix", "custom_namespace");
    }

    @Test
    public void thatMemcachedWithMissingConfigurationValuesIsLoaded() throws Exception {
        loadContext("memcached.cache.host=192.168.99.100",
                "memcached.cache.port=12345",
                "memcached.cache.prefix=custom:prefix");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, "192.168.99.100", 12345, Default.CLIENT_MODE);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, "custom:prefix", Default.NAMESPACE_KEY);
    }

    private void assertMemcachedClient(MemcachedClient memcachedClient, String host, int port, ClientMode clientMode) {
        List<NodeEndPoint> nodeEndPoints = (List<NodeEndPoint>) memcachedClient.getAllNodeEndPoints();

        assertThat("There should be only one memcached node endpoint", nodeEndPoints.size(), equalTo(1));

        ConnectionFactory cf = (ConnectionFactory) ReflectionTestUtils.getField(memcachedClient, "connFactory");
        NodeEndPoint nodeEndPoint = nodeEndPoints.get(0);

        assertThat("Memcached node endpoint host is incorrect", nodeEndPoint.getHostName(), is(host));
        assertThat("Memcached node endpoint port is incorrect", nodeEndPoint.getPort(), is(port));
        assertThat("Memcached node endpoint mode is incorrect", cf.getClientMode(), is(clientMode));
    }

    private void assertMemcachedCacheManager(MemcachedCacheManager memcachedCacheManager, int expiration, String prefix, String namespace) {
        int actualExpiration = (int) ReflectionTestUtils.getField(memcachedCacheManager, "expiration");
        assertThat(actualExpiration, is(expiration));

        String actualPrefix = (String) ReflectionTestUtils.getField(memcachedCacheManager, "prefix");
        assertThat(actualPrefix, is(prefix));

        String actualNamespace = (String) ReflectionTestUtils.getField(memcachedCacheManager, "namespace");
        assertThat(actualNamespace, is(namespace));
    }

    private void loadContext(String... environment) {
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);

        applicationContext.register(MemcachedCacheAutoConfiguration.class);
        applicationContext.refresh();
    }
}
