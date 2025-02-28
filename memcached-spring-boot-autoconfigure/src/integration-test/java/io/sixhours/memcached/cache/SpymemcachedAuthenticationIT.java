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

import net.spy.memcached.OperationTimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;

import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedCacheManager;
import static io.sixhours.memcached.cache.MemcachedAssertions.assertSpymemcachedClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spymemcached client authentication integration tests.
 * <p>
 * Using bitnami memcached container which supports PLAIN authentication mechanism.
 */
@Ignore("Until the https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java/pull/5 issue FIX is " +
        "released for AWS elasticache-client (1.2.1 version), this test should be ignored, since MemcachedClient initialization will fail with NPE")
public class SpymemcachedAuthenticationIT {

    @ClassRule
    public static GenericContainer MEMCACHED_1 = new GenericContainer("bitnami/memcached:latest")
            .withEnv("MEMCACHED_USERNAME", "my_user")
            .withEnv("MEMCACHED_PASSWORD", "my_password")
            .withExposedPorts(11211);

    @ClassRule
    public static GenericContainer MEMCACHED_2 = new GenericContainer("bitnami/memcached:latest")
            .withEnv("MEMCACHED_USERNAME", "my_user")
            .withEnv("MEMCACHED_PASSWORD", "my_password")
            .withExposedPorts(11211);

    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    private String memcachedHost1;
    private int memcachedPort1;

    private String memcachedHost2;
    private int memcachedPort2;

    @Before
    public void setUp() {
        memcachedHost1 = MEMCACHED_1.getHost();
        memcachedPort1 = MEMCACHED_1.getFirstMappedPort();

        memcachedHost2 = MEMCACHED_2.getHost();
        memcachedPort2 = MEMCACHED_2.getFirstMappedPort();
    }

    @After
    public void tearDown() {
        applicationContext.close();
    }

    @Test
    public void whenTextProtocolAndCredentialsThenMemcachedClientFails() {
        loadContext(
                "memcached.cache.protocol=text",
                "memcached.cache.authentication.username=my_user",
                "memcached.cache.authentication.password=my_password"
        );

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);
        IMemcachedClient memcachedClient = memcachedCacheManager.memcachedClient;

        // Should fail to get key due to using TEXT protocol with credentials
        assertThatThrownBy(() -> memcachedClient.get("key"))
                .isInstanceOf(OperationTimeoutException.class)
                .hasMessageStartingWith("Timeout waiting for value: waited 2,500 ms.")
                .getRootCause()
                .hasMessageStartingWith("Timed out waiting for operation - failing node: /");

        assertSpymemcachedClient(memcachedClient, Default.OPERATION_TIMEOUT, new InetSocketAddress(memcachedHost1, memcachedPort1), new InetSocketAddress(memcachedHost2, memcachedPort2));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Collections.emptyMap(), Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenBinaryProtocolAndCredentialsThenMemcachedClientSuccessful() {
        loadContext(
                "memcached.cache.protocol=binary",
                "memcached.cache.authentication.username=my_user",
                "memcached.cache.authentication.password=my_password"
        );

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);
        IMemcachedClient memcachedClient = memcachedCacheManager.memcachedClient;

        // Client should be able to access memcached, and return null value for non-existing key
        Object value = memcachedClient.get("key");
        assertThat(value).isNull();


        assertSpymemcachedClient(memcachedClient, Default.OPERATION_TIMEOUT, new InetSocketAddress(memcachedHost1, memcachedPort1), new InetSocketAddress(memcachedHost2, memcachedPort2));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Collections.emptyMap(), Default.PREFIX, Default.NAMESPACE);
    }

    @Test
    public void whenBinaryProtocolAndIncorrectCredentialsThenMemcachedNotLoaded() {
        loadContext(
                "memcached.cache.protocol=binary",
                "memcached.cache.authentication.username=my_user_incorrect",
                "memcached.cache.authentication.password=my_password"
        );

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);
        IMemcachedClient memcachedClient = memcachedCacheManager.memcachedClient;

        // Should fail to get key due to incorrect credentials i.e. no available connections
        assertThatThrownBy(() -> memcachedClient.get("key"))
                .isInstanceOf(OperationTimeoutException.class)
                .hasMessageStartingWith("Timeout waiting for value: waited 2,500 ms.")
                .getRootCause()
                .hasMessageStartingWith("Timed out waiting for operation - failing node: <unknown>");

        // There should be no available server for client
        assertSpymemcachedClient(memcachedClient, Default.OPERATION_TIMEOUT);
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Collections.emptyMap(), Default.PREFIX, Default.NAMESPACE);
    }

    private void loadContext(String... environment) {
        TestPropertyValues.of(environment).applyTo(applicationContext);
        TestPropertyValues.of(
                String.format("memcached.cache.servers=%s:%d, %s:%d", memcachedHost1, memcachedPort1, memcachedHost2, memcachedPort2),
                "memcached.cache.provider=static"
        ).applyTo(applicationContext);

        applicationContext.register(CacheConfiguration.class);
        applicationContext.register(SpyMemcachedCacheAutoConfiguration.class);
        applicationContext.register(CacheAutoConfiguration.class);
        applicationContext.refresh();
    }

    @Configuration
    @EnableCaching
    @EnableConfigurationProperties(MemcachedCacheProperties.class)
    static class CacheConfiguration {

        @Bean
        public MemcachedCacheManager cacheManager(MemcachedCacheProperties properties) throws IOException {
            return new SpyMemcachedCacheManagerFactory(properties, builder -> {
            }).create();
        }
    }
}
