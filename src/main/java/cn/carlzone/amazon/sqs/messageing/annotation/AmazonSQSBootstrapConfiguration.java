package cn.carlzone.amazon.sqs.messageing.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Amazon Queue Config
 *
 * @author zhaoyong_sh
 */
@Configuration
public class AmazonSQSBootstrapConfiguration {

    @Bean(name = "cn.carlzone.amazon.sqs.messageing.annotation.innerAmazonSQSListenerAnnotationBeanPostProcessor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AmazonSQSListenerAnnotationBeanPostProcessor amazonSQSListenerAnnotationProcessor() {
        return new AmazonSQSListenerAnnotationBeanPostProcessor();
    }
}
