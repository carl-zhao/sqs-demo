package cn.carlzone.amazon.sqs.messageing;

/**
 * ListenerExecutionFailedException
 *
 * @author zhaoyong_sh
 */
public class ListenerExecutionFailedException extends AmazonSQSException {

    private final MessageContent failedMessage;

    public ListenerExecutionFailedException(String msg, Throwable cause, MessageContent failedMessage) {
        super(msg, cause);
        this.failedMessage = failedMessage;
    }

    public MessageContent getFailedMessage() {
        return this.failedMessage;
    }

}
