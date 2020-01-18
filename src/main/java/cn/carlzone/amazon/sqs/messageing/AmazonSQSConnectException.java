package cn.carlzone.amazon.sqs.messageing;

/**
 * AmazonSQSTimeoutException
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSConnectException extends AmazonSQSException {

    public AmazonSQSConnectException(Exception cause) {
        super(cause);
    }

    public AmazonSQSConnectException(String message, Throwable cause) {
        super(message, cause);
    }

}
