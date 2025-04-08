package com.example;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProcessorApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorApplication.class);

    private final Consumer<String, String> consumer;

    final String outputTopic = System.getenv().getOrDefault("OUTPUT_TOPIC", "subreddits");
    final static String OUTPUT_FILE_PATH = System.getenv().getOrDefault("OUTPUT_FILE_PATH", "reddit-output.csv");

    public ProcessorApplication(Properties properties) {
        this.consumer = new KafkaConsumer<>(properties);
    }

    public void runConsume(final List<String> topicNames) {
        try {
            consumer.subscribe(topicNames);
            logger.info("Subscribed to topic {}", topicNames);
            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    writeToFile(consumerRecord);
                }
            }
        } finally {
            logger.info("Closing consumer");
            consumer.close();
        }
    }

    private void writeToFile(ConsumerRecord<String, String> consumerRecord) {
        final String[] parts = consumerRecord.value().split(",");
        String name = parts[0];
        String createdAt = parts[10];

        StringJoiner joiner = new StringJoiner(",")
        .add(name)
        .add(createdAt);

        Path path = Paths.get(OUTPUT_FILE_PATH);
        try {
            ensureSinkFileExists();
            Files.writeString(path, joiner.toString() + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Error writing to file: {}", OUTPUT_FILE_PATH, e);
        }
    }

    private void ensureSinkFileExists() throws IOException {
        File file = new File(OUTPUT_FILE_PATH);
        file.createNewFile();
    }

    public static void main(String[] args) {
        final Properties consumerProperties = new Properties() {{
            // User-specific properties that you must set
            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "localhost:29092, localhost:39092, localhost:49092"));

            // Fixed properties
            put(KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
            put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            put(GROUP_ID_CONFIG,                 System.getenv().getOrDefault("GROUP_ID", "group-1"));
            put(AUTO_OFFSET_RESET_CONFIG,        "earliest");
        }};


        final String inputTopic = System.getenv().getOrDefault("INPUT_TOPIC", "subreddits");

        final ProcessorApplication processorApp = new ProcessorApplication(consumerProperties);
        processorApp.runConsume(List.of(inputTopic));
    }
}
