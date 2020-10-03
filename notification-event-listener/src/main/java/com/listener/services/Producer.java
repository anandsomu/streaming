package com.listener.services;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class Producer {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${notification.rabbitmq.queue}")
    private String QUEUE_NOTIFICATION;

    @Value("${notification.rabbitmq.exchange}")
    private String EXCHANGE_NOTIFICATION;

    public void produceMsg(Object msg) {
        amqpTemplate.convertAndSend(EXCHANGE_NOTIFICATION, QUEUE_NOTIFICATION, msg);
        log.info("Send msg = " + msg);
    }
}
