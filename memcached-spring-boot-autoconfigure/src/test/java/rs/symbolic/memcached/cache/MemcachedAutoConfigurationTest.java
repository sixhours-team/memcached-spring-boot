package io.sixhours.memcached.cache;

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Memcached auto-configuration tests.
 *
 * @author Igor Bolic
 */
public class MemcachedAutoConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    @After
    public void teardown() {
        applicationContext.close();
    }

    @Test
    public void thatMemcachedNotLoadedWhenCachingNotEnabled() throws Exception {
        loadContext(EmptyConfiguration.class);

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void thatMemcachedNotLoadedWhenUsingCustomCacheManager() throws Exception {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void thatMemcachedCustomCacheManagerIsLoaded() throws Exception {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager.getClass(), equalTo(ConcurrentMapCacheManager.class));
    }

    @Test
    public void thatMemcachedWithDefaultConfigurationIsLoaded() throws Exception {
        loadContext(CacheConfiguration.class);

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.HOST, Default.PORT, Default.CLIENT_MODE);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void thatMemcachedWithNonCustomConfigurationIsLoadedWhenCacheManagerBeanAlreadyInContext() throws Exception {
        // add cache manager to the context before triggering auto-configuration on context load
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(mock(MemcachedClient.class));
        RootBeanDefinition cacheManagerBeanDefinition = new RootBeanDefinition(
                MemcachedCacheManager.class,
                constructorArgumentValues,
                null);
        applicationContext.registerBeanDefinition("cacheManager", cacheManagerBeanDefinition);

        loadContext(CacheConfiguration.class, "memcached.cache.expiration=3600",
                "memcached.cache.prefix=custom:prefix",
                "memcached.cache.namespace=custom_namespace");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        assertThat("Auto-configured disposable instance should not be loaded in context", memcachedCacheManager, not(instanceOf(MemcachedCacheAutoConfiguration.DisposableMemcachedCacheManager.class)));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void thatMemcachedWithCustomConfigurationIsLoaded() throws Exception {
        loadContext(CacheConfiguration.class, "memcached.cache.host=192.168.99.100",
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
        loadContext(CacheConfiguration.class, "memcached.cache.host=192.168.99.100",
                "memcached.cache.port=12345",
                "memcached.cache.prefix=custom:prefix");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, "192.168.99.100", 12345, Default.CLIENT_MODE);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, "custom:prefix", Default.NAMESPACE);
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

    private void loadContext(Class<?> configuration, String... environment) {
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);

        applicationContext.register(configuration);
        applicationContext.register(MemcachedCacheAutoConfiguration.class);
        applicationContext.refresh();
    }

    @Configuration
    static class EmptyConfiguration {
    }

    @Configuration
    @EnableCaching
    static class CacheConfiguration {
    }

    @Configuration
    @EnableCaching
    static class CacheWithCustomCacheManagerConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

}
