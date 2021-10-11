package com.ducks.goodsduck.commons.component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MessageProcessor {

    private final String queueName = "main_queue.fifo";
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    public MessageProcessor(AmazonSQS amazonSqs) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSqs);
    }

    public void send(String data) {
        Message<String> message = MessageBuilder.withPayload(data)
                .setHeader("message-group-id", UUID.randomUUID().toString())
                .setHeader("ContentBasedDeduplication", "true")
                .build();

        System.out.println("message = " + message.getPayload());
        System.out.println("message = " + message.toString());
        queueMessagingTemplate.send(queueName, message);
    }

//    @SqsListener(value = queueName)
//    public void receive(String message) {
//        log.info("Event : {}", message);
//    }
}