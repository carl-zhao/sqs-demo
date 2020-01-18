package cn.carlzone.amazon.sqs.config.support;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 环境变量是本地
 *
 * @author zhaoyong_sh
 */
public class LocalCondition implements Condition {

    private static final String ENVIRONMENT = "environment";

    private static final String LOCAL = "local";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String environment = context.getEnvironment().getProperty(ENVIRONMENT);
        boolean localEnv = LOCAL.equals(environment);
        return matcherInternal(localEnv);
    }

    protected boolean matcherInternal(boolean localEnv){
        return localEnv;
    }

}
