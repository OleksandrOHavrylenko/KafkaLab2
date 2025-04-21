package com.example.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Oleksandr Havrylenko
 **/
class ByteUtilsTest {

    @Test
    void longToBytesToLong10() {
        long expected = 10L;
        byte[] bytes = ByteUtils.longToBytes(expected);
        long actual = ByteUtils.bytesToLong(bytes);
        assertEquals(expected, actual);
    }

    @Test
    void longToBytesToLongMin() {
        long expected = Long.MIN_VALUE;
        byte[] bytes = ByteUtils.longToBytes(expected);
        long actual = ByteUtils.bytesToLong(bytes);
        assertEquals(expected, actual);
    }

    @Test
    void longToBytesToLongMax() {
        long expected = Long.MAX_VALUE;
        byte[] bytes = ByteUtils.longToBytes(expected);
        long actual = ByteUtils.bytesToLong(bytes);
        assertEquals(expected, actual);
    }

    @Test
    void longToBytesToLongZero() {
        long expected = 0L;
        byte[] bytes = ByteUtils.longToBytes(expected);
        long actual = ByteUtils.bytesToLong(bytes);
        assertEquals(expected, actual);
    }
}