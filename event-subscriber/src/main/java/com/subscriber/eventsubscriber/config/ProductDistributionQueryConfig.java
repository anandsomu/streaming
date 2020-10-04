package com.subscriber.eventsubscriber.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ProductDistributionQueryConfig {
    @Value("${pd.insertQuery}")
    private String insertQuery;
    @Value("${pd.updateQuery}")
    private String updateQuery;
}
