package com.listener.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Producer {
    private final AmqpTemplate amqpTemplate;

    @Value("${notification.rabbitmq.queue}")
    private String QUEUE_NOTIFICATION;

    @Value("${notification.rabbitmq.exchange}")
    private String EXCHANGE_NOTIFICATION;

    public void produceMsg(Object msg) {
        amqpTemplate.convertAndSend(EXCHANGE_NOTIFICATION, QUEUE_NOTIFICATION, msg);
        log.info("Send msg = " + msg);
    }
}
