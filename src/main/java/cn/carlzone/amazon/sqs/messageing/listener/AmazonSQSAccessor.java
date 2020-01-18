package cn.carlzone.amazon.sqs.messageing.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Amazon SQS Accessor
 *
 * @author zhaoyong_sh
 */
public class AmazonSQSAccessor implements InitializingBean {

    private volatile AmazonSQS amazonSQS;

    public final void setAmazonSQS(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    public AmazonSQS getAmazonSQS() {
        return amazonSQS;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.amazonSQS, "amazonSQS is required");
    }

    protected RuntimeException convertAmazonSQSAccessorException(Exception ex) {
        return AmazonSQSExceptionTranslator.convertAmazonSQSAccessException(ex);
    }

}
