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
    private final Map<String, ReportLine> reportData;

    public MetricAnalyser() {
        this.reportData = new HashMap<>();
    }

    public void addMetric(final Metric newMetric) {
        logger.info("New Metric received: {}", newMetric);
        double timeDuration = newMetric.finishTime() - newMetric.startTime();
        double throughputMB = ((double) newMetric.sizeBytes() * 1_000_000_000.0) / (1024 * timeDuration);

        logger.info("Current data for Test: {}: Throughput : {}MB/s, Max latency: {}ms",
                newMetric.testName(),
                String.format("%.03f", throughputMB),
                String.format("%.03f", newMetric.latencyNanos() / 1_000_000.0));

        if (reportData.containsKey(newMetric.testName())) {
            reportData.put(newMetric.testName(), new ReportLine(reportData.get(newMetric.testName()), newMetric.sizeBytes(), newMetric.finishTime(), newMetric.latencyNanos()));
        } else {
            reportData.put(newMetric.testName(),
                    new ReportLine(newMetric.sizeBytes(), newMetric.startTime(), newMetric.finishTime(), newMetric.latencyNanos()));
        }
        showReport();
    }

    private void showReport() {
        reportData.forEach(this::showTestInfo);
    }

    private void showTestInfo(final String testName, final ReportLine reportLine) {
        logger.info("Full report Test: {}: Throughput : {}MB/s, Max latency: {}ms, Max latency: {}s",
                testName,
                String.format("%.03f", reportLine.getThroughputMBs()),
                String.format("%.03f", reportLine.getMaxLatencyNanos() / 1_000_000.0),
                String.format("%.03f", reportLine.getMaxLatencyNanos() / 1_000_000_000.0));
    }
}
