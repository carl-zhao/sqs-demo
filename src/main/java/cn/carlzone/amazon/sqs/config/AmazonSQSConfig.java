package cn.carlzone.amazon.sqs.config;

import cn.carlzone.amazon.sqs.config.support.LocalCondition;
import cn.carlzone.amazon.sqs.config.support.MockAmazonSQS;
import cn.carlzone.amazon.sqs.config.support.NonLocalCondition;
import cn.carlzone.amazon.sqs.messageing.listener.SimpleAmazonSQSListenerContainerFactory;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Amazon SQS Config
 *
 * @author zhaoyong_sh
 * @see AmazonSQSConfig
 * @since 2020-01-17 23:31
 */
@Configuration
@ConditionalOnClass({AmazonSQSClientBuilder.class, AmazonSQS.class})
public class AmazonSQSConfig {

	@Bean
	@Conditional(NonLocalCondition.class)
	public AmazonSQS realAmazonSQS() {
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		return sqs;
	}

	@Bean(name = "amazonSQSListenerContainerFactory")
	public SimpleAmazonSQSListenerContainerFactory amazonSQSListenerContainerFactory(AmazonSQS amazonSQS){
		SimpleAmazonSQSListenerContainerFactory factory = new SimpleAmazonSQSListenerContainerFactory();
		factory.setAmazonSQS(amazonSQS);
		return factory;
	}

	//-------------------------------------------------------------`--------
	// Local Test
	//---------------------------------------------------------------------
	@Bean
	@Conditional(LocalCondition.class)
	public AmazonSQS mockAmazonSQS(){
		return new MockAmazonSQS();
	}


}
