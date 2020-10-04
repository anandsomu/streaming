package com.subscriber.eventsubscriber.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriber.eventsubscriber.beans.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "${notification.rabbitmq.queue}")
public class Subscriber {
    private static final ObjectMapper mapper = new ObjectMapper();

    @RabbitHandler
    private void notitifactionEvent(NotificationEvent event){
        log.debug("Notification Event : {}", mapper.valueToTree(event).toString());
        if (isNull(event.getNotificationData()) || event.getNotificationData().isEmpty()) {
            log.error("Notification data for {} event is empty",
                    event.getNotificationType().name());
            return;
        }
        switch (event.getNotificationType()) {
            case INSERT:
                processInsertEvent(event);
                break;
            case UPDATE:
                processUpdateEvent(event);
                break;
            default:
                log.warn("Unknown event type:{}",event.getNotificationType());
        }
    }

    private void processInsertEvent(NotificationEvent event){
        event.getNotificationData().parallelStream().forEach(row->{
            final Map<String, ? extends Serializable> newValues = row.getNewValues();
            log.info("Insert Row:{}",row);
            //read values and update in staging
        });
    }

    private void processUpdateEvent(NotificationEvent event){
        event.getNotificationData().parallelStream().forEach(row->{
            final Map<String, ? extends Serializable> newValues = row.getNewValues();
            log.info("Update Row:{}",row);
            //read values and update in staging
        });
    }
}
