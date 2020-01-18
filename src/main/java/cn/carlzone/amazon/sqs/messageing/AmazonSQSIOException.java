package cn.carlzone.amazon.sqs.messageing;

import java.io.IOException;

/**
 * AmazonSQSTimeoutException
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSIOException extends AmazonSQSException {

    public AmazonSQSIOException(IOException cause) {
        super(cause);
    }

    public AmazonSQSIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
