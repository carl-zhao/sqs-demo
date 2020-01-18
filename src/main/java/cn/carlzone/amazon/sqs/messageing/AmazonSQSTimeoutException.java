package cn.carlzone.amazon.sqs.messageing;

/**
 * AmazonSQSTimeoutException
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSTimeoutException extends AmazonSQSException {

    public AmazonSQSTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmazonSQSTimeoutException(String message) {
        super(message);
    }

    public AmazonSQSTimeoutException(Throwable cause) {
        super(cause);
    }

}
