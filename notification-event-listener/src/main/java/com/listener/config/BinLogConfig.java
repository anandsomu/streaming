package com.listener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.shyiko.mysql.binlog.BinaryLogClient;


@Configuration
public class BinLogConfig {

    @Value("${mysql.binlog.host:localhost}")
    private String host;

    @Value("${mysql.binlog.port:3306}")
    private int port;

    @Value("${mysql.binlog.user}")
    private String user;

    @Value("${mysql.binlog.password}")
    private String password;

    @Bean
    BinaryLogClient binaryLogClient() {
        return new BinaryLogClient(host, port, user, password);
    }
}
