package cn.carlzone.amazon.sqs.messageing;

/**
 * AmazonSQSTimeoutException
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSException extends RuntimeException {

    public AmazonSQSException(String message) {
        super(message);
    }

    public AmazonSQSException(Throwable cause) {
        super(cause);
    }

    public AmazonSQSException(String message, Throwable cause) {
        super(message, cause);
    }

}
