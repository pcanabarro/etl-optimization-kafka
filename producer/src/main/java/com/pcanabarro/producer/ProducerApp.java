package com.pcanabarro.producer;

import com.pcanabarro.producer.config.KafkaConstants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092"); // porta exposta no docker-compose
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 100; i++) {
                String message = "Mensagem " + i;
                producer.send(new ProducerRecord<>(KafkaConstants.TOPIC, Integer.toString(i), message));
                System.out.println("Enviado: " + message);
            }
        }
    }
}
