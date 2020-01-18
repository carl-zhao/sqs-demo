package cn.carlzone.amazon.sqs.messageing.listener;

import cn.carlzone.amazon.sqs.messageing.MessageContent;
import cn.carlzone.amazon.sqs.messageing.ListenerExecutionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 抽象消息监听容器
 *
 * @author zhaoyong_sh
 */
@Slf4j
public abstract class AbstractMessageListenerContainer extends AmazonSQSAccessor implements MessageListenerContainer, DisposableBean {

    public static final int DEFAULT_PREFETCH_COUNT = 250;

    private Executor taskExecutor = new SimpleAsyncTaskExecutor();

    private final Object lifecycleMonitor = new Object();

    private volatile MessageListener messageListener;

    private String queueName;

    private volatile int prefetchCount = DEFAULT_PREFETCH_COUNT;

    private volatile boolean active = false;

    private volatile boolean running = false;

    private volatile boolean initialized;

    private boolean forceCloseChannel = true;

    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public boolean isForceCloseChannel() {
        return forceCloseChannel;
    }

    public void setForceCloseChannel(boolean forceCloseChannel) {
        this.forceCloseChannel = forceCloseChannel;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
        decorateTaskExecutorName(queueName);
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        if (!this.initialized) {
            synchronized (this.lifecycleMonitor) {
                if (!this.initialized) {
                    afterPropertiesSet();
                }
            }
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting Rabbit listener container.");
            }
            doStart();
        } catch (Exception e) {
            throw convertAmazonSQSAccessorException(e);
        }
    }

    public final boolean isActive() {
        synchronized (this.lifecycleMonitor) {
            return this.active;
        }
    }

    protected void doStart() throws Exception {
        synchronized (this.lifecycleMonitor) {
            this.active = true;
            this.running = true;
            this.lifecycleMonitor.notifyAll();
        }
    }

    @Override
    public void stop() {
        try {
            doStop();
        }
        catch (Exception ex) {
            throw convertAmazonSQSAccessorException(ex);
        }
        finally {
            synchronized (this.lifecycleMonitor) {
                this.running = false;
                this.lifecycleMonitor.notifyAll();
            }
        }
    }

    protected void invokeListener(MessageContent message) throws Exception {
        MessageListener listener = getMessageListener();
        try {
            listener.onMessage(message);
        }
        catch (Exception e) {
            throw wrapToListenerExecutionFailedExceptionIfNeeded(e, message);
        }
    }

    protected Exception wrapToListenerExecutionFailedExceptionIfNeeded(Exception e, MessageContent message) {
        if (!(e instanceof ListenerExecutionFailedException)) {
            // Wrap exception to ListenerExecutionFailedException.
            return new ListenerExecutionFailedException("Listener threw exception", e, message);
        }
        return e;
    }

    protected void doStop() {
        shutdown();
    }

    @Override
    public final boolean isRunning() {
        synchronized (this.lifecycleMonitor) {
            return (this.running);
        }
    }

    @Override
    public final void afterPropertiesSet() {
        super.afterPropertiesSet();
        initialize();
    }

    @Override
    public void setupMessageListener(MessageListener messageListener) {
        setMessageListener(messageListener);
    }

    @Override
    public void destroy() {
        shutdown();
    }

    private void initialize() {
        try {
            doInitialize();
            this.initialized = true;
        } catch (Exception e) {
            throw convertAmazonSQSAccessorException(e);
        }
    }

    protected abstract void doInitialize() throws Exception;

    public void shutdown() {
        synchronized (this.lifecycleMonitor) {
            if (!isActive()) {
                log.info("Shutdown ignored - container is not active already");
                return;
            }
            this.active = false;
            this.lifecycleMonitor.notifyAll();
        }

        log.debug("Shutting down Rabbit listener container");

        // Shut down the invokers.
        try {
            doShutdown();
        }
        catch (Exception ex) {
            throw convertAmazonSQSAccessorException(ex);
        }
        finally {
            synchronized (this.lifecycleMonitor) {
                this.running = false;
                this.lifecycleMonitor.notifyAll();
            }
        }
    }

    protected abstract void doShutdown();

    private void decorateTaskExecutorName(String queueName) {
        if(this.taskExecutor == null) {
            return;
        }
        if(!(taskExecutor instanceof SimpleAsyncTaskExecutor)){
            return;
        }
        SimpleAsyncTaskExecutor asyncTaskExecutor = SimpleAsyncTaskExecutor.class.cast(taskExecutor);
        if(queueName.indexOf("/") != -1){
            String taskExecutorName = queueName.substring(queueName.lastIndexOf("/") + 1);
            asyncTaskExecutor.setThreadNamePrefix(taskExecutorName);
        } else {
            asyncTaskExecutor.setThreadNamePrefix(queueName);
        }
    }

}
