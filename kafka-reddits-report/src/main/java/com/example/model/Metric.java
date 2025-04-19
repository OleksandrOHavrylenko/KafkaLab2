package com.example.model;

/**
 * @author Oleksandr Havrylenko
 **/
public record Metric(String testName, long sizeBytes, long durationNanos, long latencyNanos) {
}
