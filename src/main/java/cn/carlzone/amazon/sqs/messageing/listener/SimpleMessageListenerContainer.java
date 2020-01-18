package cn.carlzone.amazon.sqs.messageing.listener;

import cn.carlzone.amazon.sqs.messageing.AmazonSQSIllegalStateException;
import cn.carlzone.amazon.sqs.messageing.AmazonSQSStartupException;
import cn.carlzone.amazon.sqs.messageing.MessageContent;
import com.amazonaws.services.sqs.AmazonSQS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Amazon SQS 消费者容器
 *
 * @author zhaoyong_sh
 */
@Slf4j
public class SimpleMessageListenerContainer extends AbstractMessageListenerContainer {

    private static final long DEFAULT_START_CONSUMER_MIN_INTERVAL = 10000;

    private static final long DEFAULT_STOP_CONSUMER_MIN_INTERVAL = 60000;

    private static final long DEFAULT_CONSUMER_START_TIMEOUT = 60000L;

    private static final int DEFAULT_CONSECUTIVE_ACTIVE_TRIGGER = 10;

    private static final int DEFAULT_CONSECUTIVE_IDLE_TRIGGER = 10;

    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000;

    protected final Object consumersMonitor = new Object(); //NOSONAR

    private volatile int txSize = 1;

    private volatile int consecutiveActiveTrigger = DEFAULT_CONSECUTIVE_ACTIVE_TRIGGER;

    private volatile int consecutiveIdleTrigger = DEFAULT_CONSECUTIVE_IDLE_TRIGGER;

    private volatile Integer maxConcurrentConsumers;

    private final AtomicReference<Thread> containerStoppingForAbort = new AtomicReference<>();

    private volatile long lastConsumerStarted;

    private volatile long lastConsumerStopped;

    private volatile long startConsumerMinInterval = DEFAULT_START_CONSUMER_MIN_INTERVAL;

    private volatile long stopConsumerMinInterval = DEFAULT_STOP_CONSUMER_MIN_INTERVAL;

    private volatile int concurrentConsumers = 1;

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    private Set<BlockingQueueConsumer> consumers;

    private long consumerStartTimeout = DEFAULT_CONSUMER_START_TIMEOUT;

    public SimpleMessageListenerContainer() {}

    public SimpleMessageListenerContainer(AmazonSQS amazonSQS){
        super.setAmazonSQS(amazonSQS);
    }

    public final void setStartConsumerMinInterval(long startConsumerMinInterval) {
        Assert.isTrue(startConsumerMinInterval > 0, "'startConsumerMinInterval' must be > 0");
        this.startConsumerMinInterval = startConsumerMinInterval;
    }

    public final void setStopConsumerMinInterval(long stopConsumerMinInterval) {
        Assert.isTrue(stopConsumerMinInterval > 0, "'stopConsumerMinInterval' must be > 0");
        this.stopConsumerMinInterval = stopConsumerMinInterval;
    }

    public final void setConsecutiveActiveTrigger(int consecutiveActiveTrigger) {
        Assert.isTrue(consecutiveActiveTrigger > 0, "'consecutiveActiveTrigger' must be > 0");
        this.consecutiveActiveTrigger = consecutiveActiveTrigger;
    }

    public final void setConsecutiveIdleTrigger(int consecutiveIdleTrigger) {
        Assert.isTrue(consecutiveIdleTrigger > 0, "'consecutiveIdleTrigger' must be > 0");
        this.consecutiveIdleTrigger = consecutiveIdleTrigger;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public void setTxSize(int txSize) {
        Assert.isTrue(txSize > 0, "'txSize' must be > 0");
        this.txSize = txSize;
    }

    public void setConcurrentConsumers(final int concurrentConsumers) {
        Assert.isTrue(concurrentConsumers > 0, "'concurrentConsumers' value must be at least 1 (one)");
        if (this.maxConcurrentConsumers != null) {
            Assert.isTrue(concurrentConsumers <= this.maxConcurrentConsumers,
                    "'concurrentConsumers' cannot be more than 'maxConcurrentConsumers'");
        }
        synchronized (this.consumersMonitor) {
            if (log.isDebugEnabled()) {
                log.debug("Changing consumers from " + this.concurrentConsumers + " to " + concurrentConsumers);
            }
            int delta = this.concurrentConsumers - concurrentConsumers;
            this.concurrentConsumers = concurrentConsumers;
            if (isActive()) {
                adjustConsumers(delta);
            }
        }
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        Assert.isTrue(maxConcurrentConsumers >= this.concurrentConsumers,
                "'maxConcurrentConsumers' value must be at least 'concurrentConsumers'");
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        synchronized (this.consumersMonitor) {
            if (this.consumers != null) {
                throw new IllegalStateException("A stopped container should not have consumers");
            }
            int newConsumers = initializeConsumers();
            if (this.consumers == null) {
                log.info("Consumers were initialized and then cleared (presumably the container was stopped concurrently)");
                return;
            }
            if (newConsumers <= 0) {
                if (log.isInfoEnabled()) {
                    log.info("Consumers are already running");
                }
                return;
            }
            Set<AsyncMessageProcessingConsumer> processors = new HashSet<>();
            for (BlockingQueueConsumer consumer : this.consumers) {
                AsyncMessageProcessingConsumer processor = new AsyncMessageProcessingConsumer(consumer);
                processors.add(processor);
                getTaskExecutor().execute(processor);
            }
            for (AsyncMessageProcessingConsumer processor : processors) {
                AmazonSQSStartupException startupException = processor.getStartupException();
                if (startupException != null) {
                    throw new AmazonSQSIllegalStateException("Fatal exception on listener startup", startupException);
                }
            }
        }
    }

    @Override
    protected void doShutdown() {
        Thread thread = this.containerStoppingForAbort.get();
        if (thread != null && !thread.equals(Thread.currentThread())) {
            log.info("Shutdown ignored - container is stopping due to an aborted consumer");
            return;
        }

        try {
            List<BlockingQueueConsumer> canceledConsumers = new ArrayList<>();
            synchronized (this.consumersMonitor) {
                if (this.consumers != null) {
                    Iterator<BlockingQueueConsumer> consumerIterator = this.consumers.iterator();
                    while (consumerIterator.hasNext()) {
                        BlockingQueueConsumer consumer = consumerIterator.next();
                        canceledConsumers.add(consumer);
                        consumerIterator.remove();
                        if (consumer.declaring) {
                            consumer.thread.interrupt();
                        }
                    }
                }
                else {
                    log.info("Shutdown ignored - container is already stopped");
                    return;
                }
            }
            log.info("Workers not finished.");
            if (isForceCloseChannel()) {
                canceledConsumers.forEach(consumer -> {
                    if (log.isWarnEnabled()) {
                        log.warn("Closing channel for unresponsive consumer: " + consumer);
                    }
                    consumer.stop();
                });
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted waiting for workers.  Continuing with shutdown.");
        }

        synchronized (this.consumersMonitor) {
            this.consumers = null;
        }

    }

    protected int initializeConsumers() {
        int count = 0;
        synchronized (this.consumersMonitor) {
            if (this.consumers == null) {
                this.consumers = new HashSet<>(this.concurrentConsumers);
                for (int i = 0; i < this.concurrentConsumers; i++) {
                    BlockingQueueConsumer consumer = createBlockingQueueConsumer();
                    this.consumers.add(consumer);
                    count++;
                }
            }
        }
        return count;
    }

    private BlockingQueueConsumer createBlockingQueueConsumer() {
        String queueName = getQueueName();
        int actualPrefetchCount = getPrefetchCount() > this.txSize ? getPrefetchCount() : this.txSize;
        BlockingQueueConsumer consumer = new BlockingQueueConsumer(getAmazonSQS(), getTaskExecutor(), queueName, actualPrefetchCount);
        return consumer;
    }

    @Override
    protected void doInitialize() throws Exception {
        // do nothing
    }

    private boolean isActive(BlockingQueueConsumer consumer) {
        boolean consumerActive;
        synchronized (this.consumersMonitor) {
            consumerActive = this.consumers != null && this.consumers.contains(consumer);
        }
        return consumerActive && this.isActive();
    }

    private final class AsyncMessageProcessingConsumer implements Runnable {

        private final BlockingQueueConsumer consumer;

        private final CountDownLatch start;

        private volatile AmazonSQSStartupException startupException;

        private AmazonSQSStartupException getStartupException() throws TimeoutException, InterruptedException {
            if (!this.start.await(
                    SimpleMessageListenerContainer.this.consumerStartTimeout, TimeUnit.MILLISECONDS)) {
                log.error("Consumer failed to start in "
                        + SimpleMessageListenerContainer.this.consumerStartTimeout
                        + " milliseconds; does the task executor have enough threads to sqs the container "
                        + "concurrency?");
            }
            return this.startupException;
        }

        AsyncMessageProcessingConsumer(BlockingQueueConsumer consumer) {
            this.consumer = consumer;
            this.start = new CountDownLatch(1);
        }

        @Override
        public void run() {
            if (!isActive()) {
                return;
            }
            int consecutiveIdles = 0;
            int consecutiveMessages = 0;
            try {
                try {
                    this.consumer.start();
                    this.start.countDown();
                } catch (Throwable t) {
                    this.start.countDown();
                    throw t;
                }
                while (isActive(this.consumer) || this.consumer.hasDelivery()) {
                    boolean receivedOk = receiveAndExecute(this.consumer);
                    if (receivedOk) {
                        if (isActive(this.consumer)) {
                            if (consecutiveMessages++ > SimpleMessageListenerContainer.this.consecutiveActiveTrigger) {
                                considerAddingAConsumer();
                                consecutiveMessages = 0;
                            }
                        }
                    } else {
                        consecutiveMessages = 0;
                        if (consecutiveIdles++ > SimpleMessageListenerContainer.this.consecutiveIdleTrigger) {
                            considerStoppingAConsumer(this.consumer);
                            consecutiveIdles = 0;
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.debug("Consumer thread interrupted, processing stopped.");
                Thread.currentThread().interrupt();
            } catch (Error e){
                log.error("Consumer thread error, thread abort.", e);
            } catch (Throwable t) {
                log.debug("Consumer raised exception, processing can restart if the connection factory supports it", t);
            }			// In all cases count down to allow container to progress beyond startup
            this.start.countDown();
            if (!isActive(this.consumer)){
                log.debug("Cancelling " + this.consumer);
                this.consumer.stop();
            }
        }
    }

    private boolean receiveAndExecute(final BlockingQueueConsumer consumer) throws Throwable {
        return doReceiveAndExecute(consumer);
    }

    private boolean doReceiveAndExecute(BlockingQueueConsumer consumer) throws Throwable { //NOSONAR
        for (int i = 0; i < this.txSize; i++) {
            log.trace("Waiting for message from consumer.");
            MessageContent message = consumer.nextMessage(this.receiveTimeout);
            if (message == null) {
                break;
            }
            try {
                invokeListener(message);
            } catch (Throwable e) { //NOSONAR
                if (log.isDebugEnabled()) {
                    log.debug("process message fail the reason is : " + e.getMessage() + "the message is " + message);
                }
                break;
            }
        }
        return true;
    }

    protected void adjustConsumers(int delta) {
        synchronized (this.consumersMonitor) {
            if (isActive() && this.consumers != null) {
                if (delta > 0) {
                    Iterator<BlockingQueueConsumer> consumerIterator = this.consumers.iterator();
                    while (consumerIterator.hasNext() && delta > 0
                            && (this.maxConcurrentConsumers == null
                            || this.consumers.size() > this.maxConcurrentConsumers)) {
                        BlockingQueueConsumer consumer = consumerIterator.next();
                        consumerIterator.remove();
                        delta--;
                    }
                }
                else {
                    addAndStartConsumers(-delta);
                }
            }
        }
    }

    private void considerAddingAConsumer() {
        synchronized (this.consumersMonitor) {
            if (this.consumers != null
                    && this.maxConcurrentConsumers != null && this.consumers.size() < this.maxConcurrentConsumers) {
                long now = System.currentTimeMillis();
                if (this.lastConsumerStarted + this.startConsumerMinInterval < now) {
                    this.addAndStartConsumers(1);
                    this.lastConsumerStarted = now;
                }
            }
        }
    }

    private void considerStoppingAConsumer(BlockingQueueConsumer consumer) {
        synchronized (this.consumersMonitor) {
            if (this.consumers != null && this.consumers.size() > this.concurrentConsumers) {
                long now = System.currentTimeMillis();
                if (this.lastConsumerStopped + this.stopConsumerMinInterval < now) {
                    this.consumers.remove(consumer);
                    if (log.isDebugEnabled()) {
                        log.debug("Idle consumer terminating: " + consumer);
                    }
                    this.lastConsumerStopped = now;
                }
            }
        }
    }

    protected void addAndStartConsumers(int delta) {
        synchronized (this.consumersMonitor) {
            if (this.consumers != null) {
                for (int i = 0; i < delta; i++) {
                    if (this.maxConcurrentConsumers != null
                            && this.consumers.size() >= this.maxConcurrentConsumers) {
                        break;
                    }
                    BlockingQueueConsumer consumer = createBlockingQueueConsumer();
                    this.consumers.add(consumer);
                    AsyncMessageProcessingConsumer processor = new AsyncMessageProcessingConsumer(consumer);
                    if (log.isDebugEnabled()) {
                        log.debug("Starting a new consumer: " + consumer);
                    }
                    getTaskExecutor().execute(processor);
                    try {
                        AmazonSQSStartupException startupException = processor.getStartupException();
                        if (startupException != null) {
                            this.consumers.remove(consumer);
                            throw new AmazonSQSIllegalStateException("Fatal exception on listener startup", startupException);
                        }
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    catch (Exception e) {
                        consumer.stop();
                        log.error("Error starting new consumer", e);
                        this.consumers.remove(consumer);
                    }
                }
            }
        }
    }

}
