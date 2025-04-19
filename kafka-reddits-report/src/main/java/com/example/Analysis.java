package com.example;

/**
 * @author Oleksandr Havrylenko
 **/
public class Analysis {
    final private long sizeBytes;
    final private long startTime;
    final private long finishTime;
    final private long maxLatencyNanos;

    public Analysis(long sizeBytes, long startTime, long finishTime, long maxLatencyNanos) {
        this.sizeBytes = sizeBytes;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.maxLatencyNanos = maxLatencyNanos;
    }

    public Analysis(Analysis currentData, long sizeBytes, long finishTime, long latencyNanos) {
        this(currentData.getSizeBytes() + sizeBytes,
                currentData.getStartTime(),
                Math.max(currentData.getFinishTime(), finishTime),
                Math.max(currentData.getMaxLatencyNanos(), latencyNanos)
        );
    }

    public double getThroughputMBs() {
        return ((double) sizeBytes * 1_000_000_000.0) / (1024 * (finishTime - startTime));
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getMaxLatencyNanos() {
        return maxLatencyNanos;
    }
}
