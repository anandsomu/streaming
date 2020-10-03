package com.listener.beans;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base bean for all the Notification events
 *
 */

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NotificationEvent implements Serializable {
    private NotificationType notificationType;
    private long utcTimestamp;
    private List<NotificationDataRow> notificationData = new ArrayList<>();
}
