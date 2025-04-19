package com.example.model.deserializers;

import com.example.model.Metric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oleksandr Havrylenko
 **/
public class MetricDeserializer implements Deserializer<Metric> {
    private static final Logger logger = LoggerFactory.getLogger(MetricDeserializer.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Metric deserialize(String topic, byte[] data) {
        try {
            if (data == null){
                logger.info("Null received at deserializing Metric");
                return null;
            }
            logger.debug("Deserializing Metric object");
            return objectMapper.readValue(new String(data, "UTF-8"), Metric.class);
        } catch (Exception e) {
            logger.error("Error when deserializing byte[] to Metric object", e);
            throw new SerializationException("Error when deserializing byte[] to Metric object");
        }
    }
}
