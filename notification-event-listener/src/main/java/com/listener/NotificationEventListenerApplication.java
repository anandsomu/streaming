package com.listener;

import static com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.listener.handler.LogEventHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.event.EventListener;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.network.SSLMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class NotificationEventListenerApplication {

    private final BinaryLogClient binaryLogClient;
    private final LogEventHandler logEventHandler;

    public static void main(String[] args) {
        final SpringApplicationBuilder builder =
                        new SpringApplicationBuilder(NotificationEventListenerApplication.class);
        builder.web(false).listeners(new ApplicationPidFileWriter("./listener.pid")).run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws Exception {
        log.info("Starting binary log listener");

        EventDeserializer eventDeserializer = new EventDeserializer();

        eventDeserializer.setCompatibilityMode(DATE_AND_TIME_AS_LONG);

        binaryLogClient.setEventDeserializer(eventDeserializer);
        binaryLogClient.setSSLMode(SSLMode.DISABLED);
        binaryLogClient.registerEventListener(logEventHandler);
        binaryLogClient.setHeartbeatInterval(TimeUnit.SECONDS.toMillis(5));
        binaryLogClient.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                log.info("connected");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                log.error("Communication Failure", ex);
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                log.error("Deserialization Failure", ex);
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                log.info("disconnected");
            }
        });

        if (!binaryLogClient.isConnected()) {
            binaryLogClient.connect();
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (binaryLogClient != null) {
                binaryLogClient.unregisterEventListener(logEventHandler);
                binaryLogClient.disconnect();
            }
        } catch (IOException e) {
            log.error("Error in stop", e);
        }
    }
}
