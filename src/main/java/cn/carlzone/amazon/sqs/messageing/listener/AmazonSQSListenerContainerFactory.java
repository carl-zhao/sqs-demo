package cn.carlzone.amazon.sqs.messageing.listener;

/**
 * Amazon SQA 消息监听容器工厂
 *
 * @param <C> 消息监听容器
 * @author zhaoyong_sh
 */
public interface AmazonSQSListenerContainerFactory<C extends MessageListenerContainer> {

    C createListenerContainer(String queueName, MessageListener messageListener);

}
