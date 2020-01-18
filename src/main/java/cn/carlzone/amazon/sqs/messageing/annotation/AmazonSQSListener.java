package cn.carlzone.amazon.sqs.messageing.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Amazon SQS Listener 标记类
 *
 * @author zhaoyong_sh
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AmazonSQSListener {

    /**
     * 队列名称
     * @return
     */
    String queue() default "";

    /**
     * Amazon SQS 并发消费者(值必须大于 1 且小于 CPU 核心的 2 倍)
     * @return
     */
    int consumers() default 0;

}