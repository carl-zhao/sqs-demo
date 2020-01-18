package cn.carlzone.amazon.sqs.messageing.listener;

/**
 * Amazon SQS 消息监听容器创建工厂
 *
 * @author zhaoyong_sh
 */
public class SimpleAmazonSQSListenerContainerFactory extends AbstractAmazonSQSListenerContainerFactory<SimpleMessageListenerContainer> {

    private Integer txSize;

    private Integer concurrentConsumers;

    private Integer maxConcurrentConsumers;

    private Long startConsumerMinInterval;

    private Long stopConsumerMinInterval;

    private Integer consecutiveActiveTrigger;

    private Integer consecutiveIdleTrigger;

    private Long receiveTimeout;

    public void setTxSize(Integer txSize) {
        this.txSize = txSize;
    }

    public void setConcurrentConsumers(Integer concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public void setMaxConcurrentConsumers(Integer maxConcurrentConsumers) {
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    public void setStartConsumerMinInterval(Long startConsumerMinInterval) {
        this.startConsumerMinInterval = startConsumerMinInterval;
    }

    public void setStopConsumerMinInterval(Long stopConsumerMinInterval) {
        this.stopConsumerMinInterval = stopConsumerMinInterval;
    }

    public void setConsecutiveActiveTrigger(Integer consecutiveActiveTrigger) {
        this.consecutiveActiveTrigger = consecutiveActiveTrigger;
    }

    public void setConsecutiveIdleTrigger(Integer consecutiveIdleTrigger) {
        this.consecutiveIdleTrigger = consecutiveIdleTrigger;
    }

    public void setReceiveTimeout(Long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    @Override
    public SimpleMessageListenerContainer createContainerInstance() {
        return new SimpleMessageListenerContainer();
    }

    @Override
    protected void initializeContainer(SimpleMessageListenerContainer instance) {
        super.initializeContainer(instance);
        if(this.txSize != null) {
            instance.setTxSize(txSize);
        }
        if(this.concurrentConsumers != null) {
            instance.setConcurrentConsumers(concurrentConsumers);
        }
        if (this.maxConcurrentConsumers != null) {
            instance.setMaxConcurrentConsumers(this.maxConcurrentConsumers);
        }
        if (this.startConsumerMinInterval != null) {
            instance.setStartConsumerMinInterval(this.startConsumerMinInterval);
        }
        if (this.stopConsumerMinInterval != null) {
            instance.setStopConsumerMinInterval(this.stopConsumerMinInterval);
        }
        if (this.consecutiveActiveTrigger != null) {
            instance.setConsecutiveActiveTrigger(this.consecutiveActiveTrigger);
        }
        if (this.consecutiveIdleTrigger != null) {
            instance.setConsecutiveIdleTrigger(this.consecutiveIdleTrigger);
        }
        if (this.receiveTimeout != null) {
            instance.setReceiveTimeout(this.receiveTimeout);
        }
    }

}
