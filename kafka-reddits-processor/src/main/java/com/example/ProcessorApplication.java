package com.example;

import com.example.model.serializers.MetricSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProcessorApplication {

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

        final Properties producerProperties = new Properties() {{
            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "broker-1:19092, broker-2:19092, broker-3:19092"));
            put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(VALUE_SERIALIZER_CLASS_CONFIG, MetricSerializer.class);
            put(CLIENT_ID_CONFIG, System.getenv().getOrDefault("CLIENT_ID", "metrics-producer"));
            put(ACKS_CONFIG, System.getenv().getOrDefault("ACKS", "1"));
        }};

        final ConsumerReddits processor = new ConsumerReddits(consumerProperties, new ProducerMetrics(producerProperties));
        processor.runConsume();
    }
}
