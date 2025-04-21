package com.example;

import com.example.model.deserializers.MetricDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * @author Oleksandr Havrylenko
 **/
public class ReportApplication {
    public static void main(String[] args) {
        final Properties consumerProperties = new Properties() {{

            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "broker-1:19092, broker-2:19092, broker-3:19092"));
            put(KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
            put(VALUE_DESERIALIZER_CLASS_CONFIG, MetricDeserializer.class);
            put(GROUP_ID_CONFIG,                 System.getenv().getOrDefault("GROUP_ID", "metric-group-1"));
            put(AUTO_OFFSET_RESET_CONFIG,        "earliest");
            put(MAX_POLL_RECORDS_CONFIG,        500);
        }};

        final ConsumerMetric consumerMetric = new ConsumerMetric(consumerProperties, new MetricAnalyser());
        consumerMetric.consume();
    }
}
