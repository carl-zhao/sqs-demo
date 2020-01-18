package cn.carlzone.amazon.sqs.messageing.listener;

import cn.carlzone.amazon.sqs.messageing.MessageContent;

import java.lang.reflect.InvocationTargetException;

/**
 * 消息监听者
 *
 * @author zhaoyong_sh
 */
@FunctionalInterface
public interface MessageListener {

    void onMessage(MessageContent message) throws InvocationTargetException, IllegalAccessException;

}
