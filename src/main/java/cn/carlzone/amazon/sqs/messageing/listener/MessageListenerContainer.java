package cn.carlzone.amazon.sqs.messageing.listener;

import org.springframework.context.Lifecycle;

/**
 * 消息监听容器
 *
 * @author zhaoyong_sh
 */
public interface MessageListenerContainer extends Lifecycle {

    void setupMessageListener(MessageListener messageListener);

}
