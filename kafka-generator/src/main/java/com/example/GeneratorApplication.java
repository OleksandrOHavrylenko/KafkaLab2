package com.example;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * @author Oleksandr Havrylenko
 **/
public class GeneratorApplication {
    private static final Logger logger = LoggerFactory.getLogger(GeneratorApplication.class);

    private final Producer<String, String> producer;
    final String topic = System.getenv().getOrDefault("OUTPUT_TOPIC", "subreddits");

    public GeneratorApplication(Properties properties) {
        this.producer = new KafkaProducer<>(properties);
    }

    public String getTopic() {
        return topic;
    }

    public static void main(String[] args) {
        final Properties producerProperties = new Properties() {{
            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "broker-1:19092, broker-2:19092, broker-3:19092"));
            put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(CLIENT_ID_CONFIG, System.getenv().getOrDefault("CLIENT_ID", "subreddits-producer"));
            put(ACKS_CONFIG, System.getenv().getOrDefault("ACKS", "1"));
        }};


        String filePath = System.getenv().getOrDefault("INPUT_FILE", "input/subreddits.csv");

        final GeneratorApplication generatorApp = new GeneratorApplication(producerProperties);

        try {
            logger.info("Sending subreddits events to kafka topic: {}.", generatorApp.getTopic());
            List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));
            linesToProduce.stream()
                    .skip(1)
                    .map(generatorApp::createProducerRecord)
                    .forEach(generatorApp::sendEvent);

            logger.info("Produced {} events to kafka topic: {}.", linesToProduce.size(), generatorApp.getTopic());

        } catch (IOException e) {
            logger.error("Error reading file {} due to ", filePath, e);
        } finally {
            logger.info("ProducerApp shutdown ");
            generatorApp.shutdown();
        }

    }

    public ProducerRecord<String, String> createProducerRecord(final String line) {
        return new ProducerRecord<>(this.topic, line);
    }

    public Future<RecordMetadata> sendEvent(final ProducerRecord<String, String> record) {
        return producer.send(record);
    }

    public Future<RecordMetadata> sendEvent(final ProducerRecord<String, String> record, final Callback callback) {
        return producer.send(record, callback);
    }

    public void shutdown() {
        producer.close();
    }
}