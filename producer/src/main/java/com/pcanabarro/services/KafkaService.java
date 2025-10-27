package com.pcanabarro.services;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class KafkaService {
    private final Properties props;

    public KafkaService() {
        props = new Properties();
        try (InputStream input = new ClassPathResource("kafka.properties").getInputStream()) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load kafka.properties", e);
        }
    }

    public void sendMessage(String topic, String key, String value) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, key, value));
        }
    }
}

