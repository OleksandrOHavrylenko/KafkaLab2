package com.example;

import com.example.model.Metric;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * @author Oleksandr Havrylenko
 **/
public class ConsumerMetric {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerMetric.class);

    private final String inputTopic = System.getenv().getOrDefault("INPUT_TOPIC", "metric");

    private final Consumer<String, Metric> consumer;
    private final MetricAnalyser metricAnalyser;

    public ConsumerMetric(final Properties properties, final MetricAnalyser metricAnalyser) {
        this.consumer = new KafkaConsumer<>(properties);
        this.metricAnalyser = metricAnalyser;
    }

    public void consume() {
        final List<String> topicNames = List.of(inputTopic);
        consumer.subscribe(topicNames);
        logger.info("Subscribed to topics {}", topicNames);

        while (true) {
            final ConsumerRecords<String, Metric> metricRecords = consumer.poll(Duration.ofSeconds(1));

            for (ConsumerRecord<String, Metric> metricRecord : metricRecords) {
                metricAnalyser.addMetric(metricRecord.value());
            }
        }
    }
}
