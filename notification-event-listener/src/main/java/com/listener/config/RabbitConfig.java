package com.listener.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Value("${powerme.notification.rabbitmq.queue}")
    private String QUEUE_NOTIFICATION;

    @Value("${powerme.notification.rabbitmq.exchange}")
    private String EXCHANGE_NOTIFICATION;

    @Bean
    Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION).build();
    }

    @Bean
    TopicExchange notificationsExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange(EXCHANGE_NOTIFICATION).build();
    }

    @Bean
    Binding binding(Queue notificationsQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                             .to(notificationsExchange)
                             .with(QUEUE_NOTIFICATION);
    }
}
