package cn.carlzone.amazon.sqs.messageing;

/**
 * AmazonSQSTimeoutException
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSIllegalStateException extends AmazonSQSException {

    public AmazonSQSIllegalStateException(String message) {
        super(message);
    }

    public AmazonSQSIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
