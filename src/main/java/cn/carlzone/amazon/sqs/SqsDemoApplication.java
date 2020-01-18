package cn.carlzone.amazon.sqs;

import cn.carlzone.amazon.sqs.messageing.annotation.EnableAmazonSQS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAmazonSQS
@SpringBootApplication
public class SqsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SqsDemoApplication.class, args);
	}

}
