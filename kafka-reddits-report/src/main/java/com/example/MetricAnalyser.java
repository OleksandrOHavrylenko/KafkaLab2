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
    private final Map<String, Analysis> analysisData;

    public MetricAnalyser() {
        this.analysisData = new HashMap<>();
    }

    public void addMetric(final Metric newMetric) {
        logger.info("New Metric received: {}", newMetric);
        if (analysisData.containsKey(newMetric.testName())) {
            analysisData.put(newMetric.testName(), new Analysis(analysisData.get(newMetric.testName()), newMetric.sizeBytes(), newMetric.finishTime(), newMetric.latencyNanos()));
        } else {
            analysisData.put(newMetric.testName(),
                    new Analysis(newMetric.sizeBytes(), newMetric.startTime(), newMetric.finishTime(), newMetric.latencyNanos()));
        }
        showReport();
    }

    private void showReport() {
        analysisData.forEach(this::showTestInfo);
    }

    private void showTestInfo(final String testName, final Analysis analysis) {
        logger.info("Test: {} - Throughput : {}MB/s, Max latency: {}ms", testName, analysis.getThroughputMBs(), analysis.getMaxLatencyNanos());
    }
}
