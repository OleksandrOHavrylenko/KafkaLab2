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

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * @author Oleksandr Havrylenko
 **/
public class ProcessorApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorApplication.class);

    private final Consumer<String, String> consumer;

    final String outputTopic = System.getenv().getOrDefault("OUTPUT_TOPIC", "results");
    final static String OUTPUT_FILE_PATH = System.getenv().getOrDefault("OUTPUT_FILE_PATH", "output/reddit-output.csv");

    public ProcessorApplication(Properties properties) {
        this.consumer = new KafkaConsumer<>(properties);
    }

    public void runConsume(final List<String> topicNames) {
        try {
            consumer.subscribe(topicNames);
            logger.info("Subscribed to topic {}", topicNames);
            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10));

                long maxLatency = 0L;
                long recordSizeBytes = 0L;
                long startTime = System.nanoTime();
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    Thread.sleep(1000);
                    writeToFile(consumerRecord);
                    long currentLatency = getLatencyNanoSeconds(consumerRecord);
                    if (currentLatency > maxLatency) {
                        maxLatency = currentLatency;
                    }
                    recordSizeBytes += consumerRecord.serializedValueSize();
                }
                double throughputMB = 0.0;

                if(consumerRecords.count() != 0) {
                    long timeDuration = System.nanoTime() - startTime;

                    throughputMB = ((double) recordSizeBytes * 1_000_000_000) / (1024 * timeDuration);
                }
                logger.info("Max latency: {}ms", maxLatency/1_000_000.0);
                logger.info("Throughput : {}MB/s", throughputMB);
            }
        } catch (Exception e) {
            logger.error("Interrupted exception: ", e);
        } finally {
            logger.info("Closing consumer");
            consumer.close();
        }
    }

    private void writeToFile(ConsumerRecord<String, String> consumerRecord) {
        final String[] parts = consumerRecord.value().split(",");
        if (parts.length == 10) {
            String createdAt = parts[8];
            Path path = Paths.get(OUTPUT_FILE_PATH);
            try {
                ensureSinkFileExists();
                Files.writeString(path, createdAt + "\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.error("Error writing to file: {}", OUTPUT_FILE_PATH, e);
            }
        } else {
            logger.error("Wrong number of columns in csv line: {}, should be 10.", consumerRecord.value());
        }
    }

    private static long getLatencyNanoSeconds(ConsumerRecord<String, String> consumerRecord) {
        byte[] serializedValue = consumerRecord.headers().lastHeader("createdAt").value();
        long createdAt = ByteUtils.bytesToLong(serializedValue);
        long currentTime = System.nanoTime();
        return currentTime - createdAt;
    }

    private void ensureSinkFileExists() throws IOException {
        File file = new File(OUTPUT_FILE_PATH);
        file.createNewFile();
    }

    public static void main(String[] args) {
        final Properties consumerProperties = new Properties() {{
            // User-specific properties that you must set
            put(BOOTSTRAP_SERVERS_CONFIG, System.getenv()
                    .getOrDefault("BOOTSTRAP_SERVERS", "broker-1:19092, broker-2:19092, broker-3:19092"));

            // Fixed properties
            put(KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
            put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            put(GROUP_ID_CONFIG,                 System.getenv().getOrDefault("GROUP_ID", "group-1"));
            put(AUTO_OFFSET_RESET_CONFIG,        "earliest");
            put(MAX_POLL_RECORDS_CONFIG,        100);
        }};


        final String inputTopic = System.getenv().getOrDefault("INPUT_TOPIC", "subreddits");

        final ProcessorApplication processorApp = new ProcessorApplication(consumerProperties);
        processorApp.runConsume(List.of(inputTopic));
    }
}
