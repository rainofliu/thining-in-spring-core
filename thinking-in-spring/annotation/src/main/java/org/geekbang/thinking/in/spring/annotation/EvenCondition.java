package org.geekbang.thinking.in.spring.annotation;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 偶数Profile条件
 *
 * @author ajin
 */

public class EvenCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 条件上下文 ConditionContext
        Environment environment = context.getEnvironment();

       return environment.acceptsProfiles("even");
    }
}
