package com.example.model.serializers;

import com.example.ConsumerReddits;
import com.example.model.Metric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Oleksandr Havrylenko
 **/
public class MetricSerializer implements Serializer<Metric> {
    private static final Logger logger = LoggerFactory.getLogger(MetricSerializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Metric data) {
        try {
            if (data == null){
                logger.info("Null received at serializing Metric object");
                return null;
            }
            logger.debug("Serializing Metric");
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            logger.error("Error when serializing Metric to byte[]", e);
            throw new SerializationException("Error when serializing Metric to byte[]");
        }
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Metric data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
    }
}
