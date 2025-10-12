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

import io.sixhours.memcached.cache.MemcachedCacheProperties.Protocol;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

import java.net.InetSocketAddress;
import java.util.Collections;

import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedCacheManager;
import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedClient;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * XMemcached client authentication integration tests.
 * <p>
 * Using bitnami memcached container which supports PLAIN authentication mechanism.
 */
public class XMemcachedAuthenticationIT {

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
    public void whenBinaryProtocolAndCredentialsThenMemcachedClientSuccessful() {
        loadContext(
                "memcached.cache.protocol=binary",
                "memcached.cache.authentication.username=my_user",
                "memcached.cache.authentication.password=my_password",
                "memcached.cache.authentication.mechanism=plain"
        );

        MemcachedCacheManager memcachedCacheManager = this.applicationContext.getBean(MemcachedCacheManager.class);
        IMemcachedClient memcachedClient = memcachedCacheManager.memcachedClient;

        // Client should be able to access memcached, and return null value for non-existing key
        Object value = memcachedClient.get("key");
        assertThat(value).isNull();

        assertMemcachedClient(memcachedClient, Protocol.BINARY, Default.OPERATION_TIMEOUT, new InetSocketAddress(memcachedHost1, memcachedPort1), new InetSocketAddress(memcachedHost2, memcachedPort2));
        assertMemcachedCacheManager(memcachedCacheManager, Default.EXPIRATION, Collections.emptyMap(), Default.PREFIX, Default.NAMESPACE);
    }

    private void loadContext(String... environment) {
        TestPropertyValues.of(environment).applyTo(applicationContext);
        TestPropertyValues.of(
                String.format("memcached.cache.servers=%s:%d, %s:%d", memcachedHost1, memcachedPort1, memcachedHost2, memcachedPort2),
                "memcached.cache.provider=static"
        ).applyTo(applicationContext);

        applicationContext.register(CacheConfiguration.class);
        applicationContext.register(MemcachedCacheAutoConfiguration.class);
        applicationContext.register(CacheAutoConfiguration.class);
        applicationContext.refresh();
    }

    @Configuration
    @EnableCaching
    static class CacheConfiguration {
    }
}
