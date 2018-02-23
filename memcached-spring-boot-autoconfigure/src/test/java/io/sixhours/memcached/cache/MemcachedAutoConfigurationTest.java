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

import io.sixhours.memcached.cache.MemcachedCacheProperties.Protocol;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import net.spy.memcached.protocol.ascii.AsciiOperationFactory;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.mock;

/**
 * Memcached auto-configuration tests.
 *
 * @author Igor Bolic
 * @author Sasa Bolic
 */
public class MemcachedAutoConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    @After
    public void tearDown() {
        applicationContext.close();
    }

    @Test
    public void whenCachingNotEnabledThenMemcachedNotLoaded() {
        loadContext(EmptyConfiguration.class);

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsNoneThenMemcachedNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsNoneThenNoOpCacheLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=none");

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(NoOpCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsSimpleThenMemcachedNotLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=simple");

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsSimpleThenSimpleCacheLoaded() {
        loadContext(CacheConfiguration.class, "spring.cache.type=simple");

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
    }

    @Test
    public void whenCacheTypeIsInvalidThenContextNotLoaded() {
        thrown.expect(BeanCreationException.class);
        thrown.expectMessage(containsString("Field error in object 'spring.cache' on field 'type': rejected value [invalid-type]"));

        loadContext(CacheConfiguration.class, "spring.cache.type=invalid-type");
    }

    @Test
    public void whenUsingCustomCacheManagerThenMemcachedNotLoaded() {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No qualifying bean of type 'io.sixhours.memcached.cache.MemcachedCacheManager' available");

        this.applicationContext.getBean(MemcachedCacheManager.class);
    }

    @Test
    public void whenUsingCustomCacheManagerThenMemcachedCustomCacheManagerLoaded() {
        loadContext(CacheWithCustomCacheManagerConfiguration.class);

        CacheManager cacheManager = this.applicationContext.getBean(CacheManager.class);

        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
    }

    @Test
    public void whenCachingThenMemcachedWithDefaultConfigurationLoaded() {
        loadContext(CacheConfiguration.class);

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Default.PROTOCOL, Default.SERVERS.toArray(new InetSocketAddress[0]));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenCachingWithRefreshAutoConfigurationThenRefreshConfigurationIsLoaded() {
        loadContext(CachedWithRefreshAutoConfiguration.class);

        assertThat(this.applicationContext
                .getBeanDefinition("scopedTarget.cacheManager").getScope()).isEqualTo("refresh");
    }

    @Test
    public void whenCacheManagerBeanAlreadyInContextThenMemcachedWithNonCustomConfigurationLoaded() {
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

        assertThat(memcachedCacheManager)
                .as("Auto-configured disposable instance should not be loaded in context")
                .isNotInstanceOf(MemcachedCacheManagerFactory.DisposableMemcachedCacheManager.class);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenDynamicModeAndMultipleServerListThenMemcachedNotLoaded() {
        thrown.expect(BeanCreationException.class);
        thrown.expectMessage("Only one configuration endpoint is valid with dynamic client mode.");
        thrown.expectCause(isA(BeanInstantiationException.class));

        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:11212, 192.168.99.101:11211",
                "memcached.cache.mode=dynamic");
    }

    @Test
    public void whenStaticModeAndMultipleServerListThenMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:11212,192.168.99.101:11211",
                "memcached.cache.mode=static");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, ClientMode.Static, Default.PROTOCOL, new InetSocketAddress("192.168.99.100", 11212), new InetSocketAddress("192.168.99.101", 11211));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenTextProtocolAndMultipleServerListThenMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:11212,192.168.99.101:11211",
                "memcached.cache.protocol=text");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Protocol.TEXT, new InetSocketAddress("192.168.99.100", 11212), new InetSocketAddress("192.168.99.101", 11211));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenBinaryProtocolAndMultipleServerListThenMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:11212,192.168.99.101:11211",
                "memcached.cache.protocol=binary");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Protocol.BINARY, new InetSocketAddress("192.168.99.100", 11212), new InetSocketAddress("192.168.99.101", 11211));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenStaticModeAndEmptyServerListThenMemcachedNotLoaded() {
        thrown.expect(UnsatisfiedDependencyException.class);
        thrown.expectMessage("Server list is empty");
        thrown.expectCause(isA(BeanCreationException.class));

        loadContext(CacheConfiguration.class, "memcached.cache.servers=",
                "memcached.cache.mode=static");
    }

    @Test
    public void whenCustomConfigurationThenMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:11212",
                "memcached.cache.mode=static",
                "memcached.cache.expiration=3600",
                "memcached.cache.prefix=custom:prefix",
                "memcached.cache.namespace=custom_namespace");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, ClientMode.Static, Default.PROTOCOL, new InetSocketAddress("192.168.99.100", 11212));
        assertMemcachedCacheManager(memcachedCacheManager, 3600, "custom:prefix", Default.NAMESPACE);
    }

    @Test
    public void whenPartialConfigurationValuesThenMemcachedLoaded() {
        loadContext(CacheConfiguration.class, "memcached.cache.servers=192.168.99.100:12345",
                "memcached.cache.prefix=custom:prefix");

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);

        MemcachedClient memcachedClient = (MemcachedClient) ReflectionTestUtils.getField(memcachedCacheManager, "memcachedClient");

        assertMemcachedClient(memcachedClient, Default.CLIENT_MODE, Default.PROTOCOL, new InetSocketAddress("192.168.99.100", 12345));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, "custom:prefix", Default.NAMESPACE);
    }

    private void assertMemcachedClient(MemcachedClient memcachedClient, ClientMode clientMode, Protocol protocol, InetSocketAddress... servers) {
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
                .isInstanceOf(protocol == Protocol.TEXT ? AsciiOperationFactory.class : BinaryOperationFactory.class);
    }

    private void assertMemcachedCacheManager(MemcachedCacheManager memcachedCacheManager, int expiration, String prefix, String namespace) {
        int actualExpiration = (int) ReflectionTestUtils.getField(memcachedCacheManager, "expiration");
        assertThat(actualExpiration).isEqualTo(expiration);

        String actualPrefix = (String) ReflectionTestUtils.getField(memcachedCacheManager, "prefix");
        assertThat(actualPrefix).isEqualTo(prefix);

        String actualNamespace = (String) ReflectionTestUtils.getField(memcachedCacheManager, "namespace");
        assertThat(actualNamespace).isEqualTo(namespace);
    }

    private void loadContext(Class<?> configuration, String... environment) {
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);

        applicationContext.register(configuration);
        applicationContext.register(MemcachedCacheAutoConfiguration.class);
        applicationContext.register(CacheAutoConfiguration.class);
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
    static class CacheWithCustomCacheManagerConfiguration extends CacheConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Configuration
    @Import(RefreshAutoConfiguration.class)
    static class CachedWithRefreshAutoConfiguration extends CacheConfiguration {
    }

}
