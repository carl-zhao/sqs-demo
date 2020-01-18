package cn.carlzone.amazon.sqs.messageing.listener;

import cn.carlzone.amazon.sqs.messageing.AmazonSQSConnectException;
import cn.carlzone.amazon.sqs.messageing.AmazonSQSException;
import cn.carlzone.amazon.sqs.messageing.AmazonSQSIOException;
import cn.carlzone.amazon.sqs.messageing.AmazonSQSTimeoutException;
import cn.carlzone.amazon.sqs.messageing.AmazonSQSUnsupportedEncodingException;
import cn.carlzone.amazon.sqs.messageing.UncategorizedAmazonSQSException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

/**
 * Amazon SQS 异常转换器
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSExceptionTranslator {

    public static AmazonSQSException convertAmazonSQSAccessException(Throwable ex) {
        Assert.notNull(ex, "Exception must not be null");
        if (ex instanceof AmazonSQSException) {
            return (AmazonSQSException) ex;
        }
        if (ex instanceof ConnectException) {
            return new AmazonSQSConnectException((ConnectException) ex);
        }
        if (ex instanceof UnsupportedEncodingException) {
            return new AmazonSQSUnsupportedEncodingException(ex);
        }
        if (ex instanceof IOException) {
            return new AmazonSQSIOException((IOException) ex);
        }
        if (ex instanceof TimeoutException) {
            return new AmazonSQSTimeoutException(ex);
        }
        // fallback
        return new UncategorizedAmazonSQSException(ex);
    }

}
