package com.example;

import com.example.model.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleksandr Havrylenko
 **/
public class MetricAnalyser {
    private static final Logger logger = LoggerFactory.getLogger(MetricAnalyser.class);
    private final Map<String, Analysis> metrics;

    public MetricAnalyser() {
        this.metrics = new HashMap<>();
    }

    public void addMetric(final Metric metric) {
        logger.info("New metric received: {}", metric);
        if (metrics.containsKey(metric.testName())) {
            Analysis currentData = metrics.get(metric.testName());

            long durationNanos = currentData.durationNanos();
            long sizeBytes = currentData.sizeBytes();
            long latencyNanos = Math.max(currentData.maxLatencyNanos(), metric.latencyNanos());

            metrics.put(metric.testName(), new Analysis(sizeBytes + metric.sizeBytes(), durationNanos + metric.durationNanos(), latencyNanos));
        } else {
            metrics.put(metric.testName(), new Analysis(metric.sizeBytes(), metric.durationNanos(), metric.latencyNanos()));
        }
        showReport();
    }

    private void showReport() {
        metrics.forEach(this::showTestInfo);
    }

    private void showTestInfo(final String testName, final Analysis analysis) {
        long sizeBytes = analysis.sizeBytes();
        long durationNanos = analysis.durationNanos();
        double throughputMB = ((double) sizeBytes * 1_000_000_000.0) / (1024 * durationNanos);
        double maxLatencyMillis = analysis.maxLatencyNanos() / 1_000_000.0;
//        TODO remove after successful testing
        logger.info("MaxLatency: {}", analysis.maxLatencyNanos() / 1_000_000);

        logger.info("Test: {} - Throughput : {}MB/s, Max latency: {}ms", testName, throughputMB, maxLatencyMillis);
    }
}
