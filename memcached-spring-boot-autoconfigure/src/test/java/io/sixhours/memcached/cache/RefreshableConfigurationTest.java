package io.sixhours.memcached.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static io.sixhours.memcached.cache.MemcachedAssertions.assertMemcachedClient;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshableConfigurationTest.TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefreshableConfigurationTest {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ContextRefresher refresher;

    @Test
    public void whenContextLoadedThenMemcachedCacheManagerInitialized() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(MemcachedCacheManager.class);

        Object memcachedClient = ReflectionTestUtils.getField(cacheManager, "memcachedClient");
        assertThat(memcachedClient).isNotNull();
        assertThat(memcachedClient).isInstanceOf(MemcachedClient.class);
        assertMemcachedClient((MemcachedClient) memcachedClient);
    }

    @Test
    public void whenConfigurationChangeThenMemcachedClientReinitialized() {
        Object beforeRefresh = ReflectionTestUtils.getField(cacheManager, "memcachedClient");
        assertMemcachedClient((MemcachedClient) beforeRefresh);

        EnvironmentTestUtils.addEnvironment(environment,
                "memcached.cache.prefix:test-prefix",
                "memcached.cache.protocol:binary");

        refresher.refresh();

        Object expiration = ReflectionTestUtils.getField(cacheManager, "expiration");
        Object prefix = ReflectionTestUtils.getField(cacheManager, "prefix");
        Object afterRefresh = ReflectionTestUtils.getField(cacheManager, "memcachedClient");

        assertThat(expiration).isEqualTo(Default.EXPIRATION);
        assertThat(prefix).isEqualTo("test-prefix");
        assertMemcachedClient((MemcachedClient) afterRefresh,
                Default.CLIENT_MODE, MemcachedCacheProperties.Protocol.BINARY, Default.SERVERS.get(0));
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCaching
    protected static class TestConfiguration {
    }
}
