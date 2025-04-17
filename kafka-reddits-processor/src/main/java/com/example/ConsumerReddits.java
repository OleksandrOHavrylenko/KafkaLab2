package com.example;

import com.example.model.Metric;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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

/**
 * @author Oleksandr Havrylenko
 **/
public class ConsumerReddits {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerReddits.class);
    final static String OUTPUT_FILE_PATH = System.getenv().getOrDefault("OUTPUT_FILE_PATH", "output/reddit-output.csv");

    private final String inputTopic = System.getenv().getOrDefault("INPUT_TOPIC", "subreddits");
    private final String testName = System.getenv().getOrDefault("TEST_NAME", "test");

    private final Consumer<String, String> consumer;
    private final ProducerMetrics producerMetrics;

    public ConsumerReddits(final Properties properties, final ProducerMetrics producerMetrics) {
        this.consumer = new KafkaConsumer<>(properties);
        this.producerMetrics = producerMetrics;
    }

    public void runConsume() {
        try {
            List<String> topicNames = List.of(inputTopic);
            consumer.subscribe(topicNames);
            logger.info("Subscribed to topics {}", topicNames);

            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(500));

                long maxLatencyNanos = 0L;
                long recordSizeBytes = 0L;
                long startTime = System.nanoTime();

                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    Thread.sleep(1000);

                    writeToFile(consumerRecord);

                    long currentLatency = getLatencyNanoSeconds(consumerRecord);
                    if (currentLatency > maxLatencyNanos) {
                        maxLatencyNanos = currentLatency;
                    }
                    recordSizeBytes += consumerRecord.serializedValueSize();
                }

                long timeDuration = System.nanoTime() - startTime;
                double throughputMB = ((double) recordSizeBytes * 1_000_000_000.0) / (1024 * timeDuration);

                producerMetrics.sendEvent(new Metric(testName, recordSizeBytes, timeDuration, maxLatencyNanos));

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
        return System.nanoTime() - createdAt;
    }

    private void ensureSinkFileExists() throws IOException {
        File file = new File(OUTPUT_FILE_PATH);
        file.createNewFile();
    }
}
