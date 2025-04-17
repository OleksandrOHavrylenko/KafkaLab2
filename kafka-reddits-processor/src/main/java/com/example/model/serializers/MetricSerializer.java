package com.example.model.serializers;

import com.example.model.Metric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author Oleksandr Havrylenko
 **/
public class MetricSerializer implements Serializer<Metric> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Metric data) {
        try {
            if (data == null){
                System.out.println("Null received at serializing Metric object");
                return null;
            }
            System.out.println("Serializing Metric");
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
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
