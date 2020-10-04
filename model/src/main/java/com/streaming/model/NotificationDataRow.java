package com.streaming.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NotificationDataRow implements Serializable {
    private Map<String, ? extends Serializable> oldValues = new HashMap<>();
    private Map<String, ? extends Serializable> newValues = new HashMap<>();

    public NotificationDataRow(Map<String, ? extends Serializable> newValues) {
        this.newValues = newValues;
    }
}
