package com.subscriber.eventsubscriber.dao;

import com.subscriber.eventsubscriber.beans.stagingdb.ProductDistribution;
import com.subscriber.eventsubscriber.config.ProductDistributionQueryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDistributionDao {
    private final JdbcTemplate jdbcTemplate;
    private final ProductDistributionQueryConfig queryConfig;

    public void insertRecords(ProductDistribution pd) {
        update(queryConfig.getInsertQuery(), pd);
    }

    public void updateRecords(ProductDistribution pd) {
        update(queryConfig.getUpdateQuery(), pd);
    }

    private void update(String query, ProductDistribution pd) {
        jdbcTemplate.update(query, pd.getId(), pd.getCompanyName(), pd.getProductName(), pd.getDistributor(), pd.getLocation(), pd.getDesc());
    }
}
