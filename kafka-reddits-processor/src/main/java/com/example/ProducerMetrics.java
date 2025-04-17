package com.example;

import com.example.model.Metric;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProducerMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ProducerMetrics.class);

    private final Producer<String, Metric> producer;
    private final String topic = System.getenv().getOrDefault("OUTPUT_METRIC_TOPIC", "metric");

    public ProducerMetrics(final Properties properties) {
        this.producer = new KafkaProducer<>(properties);
    }

    public Future<RecordMetadata> sendEvent(final Metric metric) {
        logger.info("Sent MetricEvent: {}", metric);
        return producer.send(new ProducerRecord<>(this.topic, metric));
    }
}
