package cn.carlzone.amazon.sqs.messageing.listener;

import cn.carlzone.amazon.sqs.messageing.MessageContent;
import com.alibaba.fastjson.JSON;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *  Amazon SQS 消费者
 *
 *  @author zhaoyong_sh
 */
@Slf4j
public class BlockingQueueConsumer {

    private final BlockingQueue<Message> queue;

    private final AmazonSQS amazonSQS;

    private final String queueName;

    private final Executor taskExecutor;

    volatile Thread thread;

    volatile boolean declaring;

    public BlockingQueueConsumer(AmazonSQS amazonSQS, Executor taskExecutor, String queueName, int prefetchCount) {
        this.amazonSQS = amazonSQS;
        this.taskExecutor = taskExecutor;
        this.queueName = queueName;
        this.queue = new LinkedBlockingQueue<>(prefetchCount);
    }

    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    public void start() {
        log.info("Starting consumer to consume queue : " + queueName );
        this.thread = Thread.currentThread();
        this.declaring = true;
        consumeFromQueue(queueName);
    }

    public synchronized void stop() {
        this.queue.clear(); // in case we still have a client thread blocked
    }

    private void consumeFromQueue(String queueName) {
        InternalConsumer consumer = new InternalConsumer(this.amazonSQS, queueName);
        getTaskExecutor().execute(consumer);
    }

    public MessageContent nextMessage() throws InterruptedException {
        return handle(this.queue.take());
    }

    public MessageContent nextMessage(long timeout) throws InterruptedException {
        MessageContent message = handle(this.queue.poll(timeout, TimeUnit.MILLISECONDS));
        return message;
    }

    protected boolean hasDelivery() {
        return !this.queue.isEmpty();
    }

    private MessageContent handle(Message message) {
        if(message == null) {
            return null;
        }
        if(StringUtils.isEmpty(message.getBody())) {
            return null;
        }
        MessageContent notifyMessageContent = JSON.parseObject(message.getBody(), MessageContent.class);
        return notifyMessageContent;
    }

    private final class InternalConsumer implements Runnable {

        private final AmazonSQS amazonSQS;

        private final String queueName;

        public InternalConsumer(AmazonSQS amazonSQS, String queueName) {
            this.amazonSQS = amazonSQS;
            this.queueName = queueName;
        }

        @Override
        public void run() {
            while(true){
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName);
                receiveMessageRequest.withMaxNumberOfMessages(10);
                //WaitTimeSeconds 使用长轮询
                receiveMessageRequest.withWaitTimeSeconds(15);
                //设定消息不可见时间，若消息未处理或者宕机，消息将在10分钟后被其他通知服务消费
                receiveMessageRequest.setVisibilityTimeout(600);
                List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
                if(CollectionUtils.isNotEmpty(messages)) {
                    for (Message message : messages) {
                        try {
                            log.info(Thread.currentThread().getName() + "receive message : " + message.getBody());
                            BlockingQueueConsumer.this.queue.put(message);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            String receiptHandle = message.getReceiptHandle();
                            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueName, receiptHandle);
                            amazonSQS.deleteMessage(deleteMessageRequest);
                        }
                    }
                }
            }
        }
    }

}
