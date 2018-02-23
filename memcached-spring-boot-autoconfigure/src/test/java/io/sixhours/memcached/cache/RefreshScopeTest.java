package io.sixhours.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshScopeTest.TestConfiguration.class)
public class RefreshScopeTest {

    //    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ContextRefresher contextRefresher;

    @Test
    public void whenContextLoadedThenMemcachedCacheManagerInitialized() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(MemcachedCacheManager.class);

        assertMemcachedClient();
    }

    @Test
    public void whenConfigurationChangeThenMemcachedCliendReinitialized() {

    }

    private void assertMemcachedClient() {
        Object memcachedClient = ReflectionTestUtils.getField(cacheManager, "memcachedClient");
        assertThat(memcachedClient).isNotNull();
        assertThat(memcachedClient).isInstanceOf(MemcachedClient.class);
        assertThat(MemcachedClient.class.cast(memcachedClient).getAllNodeEndPoints()).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCaching
    protected static class TestConfiguration {
    }
}
