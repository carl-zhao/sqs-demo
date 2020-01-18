package cn.carlzone.amazon.sqs.messageing;

import lombok.Data;

/**
 * 业务对象
 *
 * @author zhaoyong_sh
 * @see MessageContent
 * @since 2020-01-17 23:18
 */
@Data
public class MessageContent {

	private String queueName;

	private String message;

}
