package cn.carlzone.amazon.sqs.messageing;

/**
 * UncategorizedAmasonSQSException
 *
 * @author zhaoyong_sh
 */
public class UncategorizedAmazonSQSException extends AmazonSQSException {

    public UncategorizedAmazonSQSException(Throwable cause) {
        super(cause);
    }

    public UncategorizedAmazonSQSException(String message, Throwable cause) {
        super(message, cause);
    }

}
