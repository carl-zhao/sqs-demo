package cn.carlzone.amazon.sqs.consumer;

import cn.carlzone.amazon.sqs.messageing.MessageContent;
import cn.carlzone.amazon.sqs.messageing.annotation.AmazonSQSListener;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author zhaoyong_sh
 * @see LocalMessageListener
 * @since 2020-01-17 23:55
 */
@Component
public class LocalMessageListener {

	@AmazonSQSListener(queue = "amazon.local.mock")
	public void onMessage(MessageContent messageContent){
		System.out.println(messageContent);
	}

}
