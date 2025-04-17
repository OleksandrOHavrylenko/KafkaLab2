package com.example;

import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProducerReddits {
    private static final Logger logger = LoggerFactory.getLogger(ProducerReddits.class);

    private final Producer<String, String> producer;
    private final String topic = System.getenv().getOrDefault("OUTPUT_TOPIC", "subreddits");
    private final String filePath = System.getenv().getOrDefault("INPUT_FILE", "input/subreddits.csv");

    public ProducerReddits(Properties properties) {
        this.producer = new KafkaProducer<>(properties);;
    }

    public void produce() {
        try {
            logger.info("Sending subreddits events to kafka topic: {}.", this.getTopic());
            List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));
            linesToProduce.stream()
                    .skip(1)
                    .map(this::createProducerRecord)
                    .forEach(this::sendEvent);

            logger.info("Produced {} events to kafka topic: {}.", linesToProduce.size(), getTopic());
        } catch (IOException e) {
            logger.error("Error reading file {} due to ", filePath, e);
        } finally {
            logger.info("ProducerApp shutdown ");
            shutdown();
        }
    }

    private ProducerRecord<String, String> createProducerRecord(final String line) {
        ProducerRecord<String, String> record = new ProducerRecord<>(this.topic, line);
        record.headers().add("createdAt", ByteUtils.longToBytes(System.nanoTime()));
        return record;
    }

    private Future<RecordMetadata> sendEvent(final ProducerRecord<String, String> record) {
        return producer.send(record);
    }

    private Future<RecordMetadata> sendEvent(final ProducerRecord<String, String> record, final Callback callback) {
        return producer.send(record, callback);
    }

    private String getTopic() {
        return topic;
    }

    private void shutdown() {
        producer.close();
    }
}
