package cn.carlzone.amazon.sqs.config.support;

/**
 * 环境变量非本地
 *
 * @author zhaoyong_sh
 */
public class NonLocalCondition extends LocalCondition {

    @Override
    public boolean matcherInternal(boolean localEnv) {
        return !localEnv;
    }

}
