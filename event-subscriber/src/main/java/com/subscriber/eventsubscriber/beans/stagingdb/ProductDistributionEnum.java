package com.subscriber.eventsubscriber.beans.stagingdb;


import lombok.Getter;

public enum ProductDistributionEnum {
    ID("id"), COMPANY_NAME("company_name"), PRODUCT_NAME("product_name"), DISTRIBUTOR("distributor"),
    LOCATION("location"),DESC("description");

    @Getter
    private String val;

    ProductDistributionEnum(String val) {
        this.val = val;
    }
}
