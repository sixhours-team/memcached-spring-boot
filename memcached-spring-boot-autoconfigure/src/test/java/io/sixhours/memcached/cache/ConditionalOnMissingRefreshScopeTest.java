package io.sixhours.memcached.cache;

import org.junit.After;
import org.junit.Test;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalOnMissingRefreshScopeTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void whenMissingRefreshAutoConfigurationThenOutcomeShouldMatch() {
        this.context.register(OnMissingRefreshScopeConfig.class);
        this.context.refresh();

        assertThat(this.context.containsBean("foo")).isTrue();
        assertThat((String) this.context.getBean("foo")).startsWith("foo");
    }

    @Test
    public void whenHavingRefreshAutoConfigurationThenOutcomeShouldNotMatch() {
        this.context.register(OnMissingRefreshScopeConfig.class, RefreshAutoConfiguration.class);
        this.context.refresh();

        assertThat(this.context.getBean(org.springframework.cloud.context.scope.refresh.RefreshScope.class)).isNotNull();
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Configuration
    @ConditionalOnMissingRefreshScope
    static class OnMissingRefreshScopeConfig {

        @Bean
        public String foo() {
            return "foo-" + UUID.randomUUID();
        }
    }
}