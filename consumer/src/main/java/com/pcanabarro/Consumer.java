package com.pcanabarro;

import com.pcanabarro.database.Database;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Successfully connected to PostgreSQL!");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM SALARY");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") + ", Amount: " + rs.getDouble("amount"));
                }
            } else {
                System.out.println("❌ Failed to connect to PostgreSQL.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");
        props.put("group.id", "etl-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

//        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
//            consumer.subscribe(Collections.singletonList("etl_topic"));
//
//            System.out.println("Esperando mensagens...");
//
//            while (true) {
//                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
//                for (ConsumerRecord<String, String> record : records) {
//                    System.out.printf("Consumido [key=%s, value=%s, offset=%d]\n",
//                            record.key(), record.value(), record.offset());
//                }
//            }
//        }
    }
}
