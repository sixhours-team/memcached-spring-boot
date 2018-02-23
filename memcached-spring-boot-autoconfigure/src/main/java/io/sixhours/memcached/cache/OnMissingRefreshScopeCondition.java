package io.sixhours.memcached.cache;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

public class OnMissingRefreshScopeCondition extends AnyNestedCondition {

    public OnMissingRefreshScopeCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnMissingClass("org.springframework.cloud.context.scope.refresh.RefreshScope")
    static class MissingRefreshScope {
    }

    @ConditionalOnMissingBean(RefreshAutoConfiguration.class)
    static class MissingRefreshAutoConfiguration {
    }

}
