package com.pcanabarro.consumer.config;

public class KafkaConstants {
    public static String KAFKA_BROKER = "localhost:29092";
    public static String TOPIC = "etl_topic";
    public static String GROUP_ID = "etl-group";
    public static String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
}