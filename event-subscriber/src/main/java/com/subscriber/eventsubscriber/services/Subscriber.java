package com.subscriber.eventsubscriber.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.model.NotificationEvent;
import com.subscriber.eventsubscriber.beans.stagingdb.ProductDistribution;
import com.subscriber.eventsubscriber.dao.ProductDistributionDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.subscriber.eventsubscriber.beans.stagingdb.ProductDistributionEnum.*;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "${notification.rabbitmq.queue}")
public class Subscriber {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ProductDistributionDao pdDao;

    @RabbitHandler
    private void notitifactionEvent(NotificationEvent event) {
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
                log.warn("Unknown event type:{}", event.getNotificationType());
        }
    }

    private void processInsertEvent(NotificationEvent event) {
        event.getNotificationData().parallelStream().forEach(row -> {
            final Map<String, ? extends Serializable> newValues = row.getNewValues();
            log.info("Insert Row:{}", row);
            //read values and update in staging
            try {
                ProductDistribution pd = transformToPDBean(newValues);
                pdDao.insertRecords(pd);
            } catch (Exception e) {
                log.error("Error while iserting records in Stage", e);
            }
        });
    }

    private void processUpdateEvent(NotificationEvent event) {
        event.getNotificationData().parallelStream().forEach(row -> {
            final Map<String, ? extends Serializable> newValues = row.getNewValues();
            log.info("Update Row:{}", row);
            //read values and update in staging
            try {
                ProductDistribution pd = transformToPDBean(newValues);
                pdDao.updateRecords(pd);
            } catch (Exception e) {
                log.error("Error while updating records in stage", e);
            }
        });
    }

    private ProductDistribution transformToPDBean(Map<String, ? extends Serializable> newValues) {
        String company = new String((byte[]) newValues.get(COMPANY_NAME.getVal()), StandardCharsets.UTF_8);
        String distributor = new String((byte[]) newValues.get(DISTRIBUTOR.getVal()), StandardCharsets.UTF_8);
        String desc = new String((byte[]) newValues.get(DESC.getVal()), StandardCharsets.UTF_8);
        String location = new String((byte[]) newValues.get(LOCATION.getVal()), StandardCharsets.UTF_8);
        String prduct = new String((byte[]) newValues.get(PRODUCT_NAME.getVal()), StandardCharsets.UTF_8);
        return ProductDistribution.builder()
                .id((Long) newValues.get(ID.getVal()))
                .companyName(company)
                .distributor(distributor)
                .desc(desc)
                .location(location)
                .productName(prduct)
                .build();
    }
}
