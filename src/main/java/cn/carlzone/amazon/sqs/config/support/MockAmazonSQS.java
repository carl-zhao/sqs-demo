package cn.carlzone.amazon.sqs.config.support;

import cn.carlzone.amazon.sqs.messageing.MessageContent;
import com.alibaba.fastjson.JSON;
import com.amazonaws.services.sqs.AbstractAmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Amazon 客户端用于本地测试
 * 
 * @author zhaoyong_sh
 */
public class MockAmazonSQS extends AbstractAmazonSQSAsync {

    @Getter
    private static final Map<String, Message> messages = new ConcurrentHashMap<>();

    static {
        mockMessage();
    }

    @Override
    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest request) {
        ReceiveMessageResult result = new ReceiveMessageResult();
        String queueUrl = request.getQueueUrl();
        Message message = messages.get(queueUrl);
        if(message != null) {
            result.setMessages(Lists.newArrayList(message));
        }
        return result;
    }

    @Override
    public SendMessageResult sendMessage(SendMessageRequest request){
        String queueUrl = request.getQueueUrl();
        Message message = new Message();
        message.setBody(request.getMessageBody());
        messages.put(queueUrl, message);
        return new SendMessageResult();
    }

    private static void mockMessage() {
        messages.put("amazon.local.mock", genMessage("mock message"));
    }

    @Override
    public DeleteMessageResult deleteMessage(DeleteMessageRequest deleteMessageRequest){
        String queueUrl = deleteMessageRequest.getQueueUrl();
        messages.remove(queueUrl);
        return new DeleteMessageResult();
    }


    private static Message genMessage(String message) {
        Message localMessage = new Message();
        MessageContent localMessageContent = new MessageContent();
        localMessageContent.setMessage(message);
        localMessage.setBody(JSON.toJSONString(localMessageContent));
        return localMessage;
    }

}
