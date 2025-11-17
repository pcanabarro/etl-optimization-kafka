package com.pcanabarro;

import com.pcanabarro.database.Database;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

class ConsumerWorker implements Runnable {
    private final String consumerName;

    public ConsumerWorker(String consumerName) {
        this.consumerName = consumerName;
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");
        props.put("group.id", "etl-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("client.id", consumerName);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("etl-topic"));
            System.out.println(consumerName + " esperando mensagens...");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    long startTime = System.nanoTime();
                    System.out.printf("%s consumido [key=%s, partition=%s, value=%s, offset=%d]\n",
                            consumerName, record.key(), record.partition(), record.value(), record.offset());

//                    insertMessageIntoDatabase(record);

                    long endTime = System.nanoTime();
                    long durationMs = (endTime - startTime) / 1_000_000;
                    System.out.printf("Processing time: %d ms\n", durationMs);
                    System.out.println(" ");
                }
            }
        }
    }

    private static void insertMessageIntoDatabase(ConsumerRecord<String, String> record) {
        //create a method to transform message from source_db type to destination_db type
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                    "INSERT INTO messages (message_key, message_value, message_offset) VALUES ('%s', '%s', %d)",
                    record.key(), record.value(), record.offset()
            );
            stmt.executeUpdate(sql);
            System.out.printf("Inserted into DB: [key=%s, value=%s, offset=%d]\n",
                    record.key(), record.value(), record.offset());
        } catch (SQLException e) {
            System.err.println("DB insert error: " + e.getMessage());
        }
    }
}