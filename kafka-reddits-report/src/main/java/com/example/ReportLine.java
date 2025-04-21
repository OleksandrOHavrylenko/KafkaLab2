package com.example;

/**
 * @author Oleksandr Havrylenko
 **/
public class ReportLine {
    final private long sizeBits;
    final private long startTime;
    final private long finishTime;
    final private long maxLatencyNanos;

    public ReportLine(long sizeBits, long startTime, long finishTime, long maxLatencyNanos) {
        this.sizeBits = sizeBits;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.maxLatencyNanos = maxLatencyNanos;
    }

    public ReportLine(ReportLine currentData, long sizeBits, long finishTime, long latencyNanos) {
        this(currentData.getSizeBits() + sizeBits,
                currentData.getStartTime(),
                Math.max(currentData.getFinishTime(), finishTime),
                Math.max(currentData.getMaxLatencyNanos(), latencyNanos)
        );
    }

    public double getThroughputMbps() {
        return ((double) sizeBits * 1_000_000_000.0) / (1024 * (finishTime - startTime));
    }

    public long getSizeBits() {
        return sizeBits;
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
