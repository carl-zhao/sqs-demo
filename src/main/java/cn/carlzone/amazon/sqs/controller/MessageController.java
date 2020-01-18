package cn.carlzone.amazon.sqs.controller;

import cn.carlzone.amazon.sqs.config.support.MockAmazonSQS;
import cn.carlzone.amazon.sqs.messageing.MessageContent;
import com.alibaba.fastjson.JSON;
import com.amazonaws.services.sqs.model.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 消息处理类
 *
 * @author zhaoyong_sh
 * @see MessageController
 * @since 2020-01-17 23:46
 */
@RestController
@RequestMapping("message")
public class MessageController {

	@RequestMapping(value = "add")
	public String addMessage(MessageContent content){
		Map<String, Message> messages = MockAmazonSQS.getMessages();
		messages.put(content.getQueueName(), genMessage(content.getMessage()));
		return "ok";
	}

	private Message genMessage(String message) {
		Message localMessage = new Message();
		MessageContent localMessageContent = new MessageContent();
		localMessageContent.setMessage(message);
		localMessage.setBody(JSON.toJSONString(localMessageContent));
		return localMessage;
	}

}
