package io.sixhours.memcached.cache;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMissingRefreshScopeCondition.class)
@interface ConditionalOnMissingRefreshScope {
}
