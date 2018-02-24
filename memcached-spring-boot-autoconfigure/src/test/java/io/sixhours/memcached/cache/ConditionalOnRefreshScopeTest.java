package io.sixhours.memcached.cache;

import org.junit.Test;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalOnRefreshScopeTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Test
    public void whenMissingRefreshAutoConfigurationThenOutcomeShouldNotMatch() {
        this.context.register(OnRefreshScopeConfig.class);
        this.context.refresh();

        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    public void whenHavingRefreshAutoConfigurationThenOutcomeShouldMatch() {
        this.context.register(OnRefreshScopeConfig.class, RefreshAutoConfiguration.class);
        this.context.refresh();

        assertThat(this.context.getBean(org.springframework.cloud.context.scope.refresh.RefreshScope.class)).isNotNull();
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Configuration
    @ConditionalOnRefreshScope
    static class OnRefreshScopeConfig {

        @Bean
        public String foo() {
            return "foo-" + UUID.randomUUID();
        }
    }
}