package com.example;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProcessorApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorApplication.class);

    public static void main(String[] args) {
        final Properties consumerProperties = new Properties() {{

            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "broker-1:19092, broker-2:19092, broker-3:19092"));
            put(KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
            put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            put(GROUP_ID_CONFIG,                 System.getenv().getOrDefault("GROUP_ID", "group-1"));
            put(AUTO_OFFSET_RESET_CONFIG,        "earliest");
            put(MAX_POLL_RECORDS_CONFIG,        100);
        }};

        final ProcessorReddits processor = new ProcessorReddits(consumerProperties);
        processor.runConsume();
    }
}
