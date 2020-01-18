package cn.carlzone.amazon.sqs.messageing.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 激活 {@link AmazonSQSListener} 注解
 *
 * @author zhayong_sh
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AmazonSQSBootstrapConfiguration.class)
public @interface EnableAmazonSQS {

}
