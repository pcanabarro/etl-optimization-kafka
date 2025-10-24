package com.pcanabarro;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.pcanabarro.database.Database;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Properties;

@Service
public class ProducerService {
    public void sendMessages() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (
                KafkaProducer<String, String> producer = new KafkaProducer<>(props);
                Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM employee");
        ) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            int i = 1;
            while (rs.next()) {
                StringBuilder message = new StringBuilder();
                for (int col = 1; col <= columnCount; col++) {
                    message.append(meta.getColumnName(col)).append(": ").append(rs.getString(col));
                    if (col < columnCount) message.append(", ");
                }
                producer.send(new ProducerRecord<>("etl_topic", Integer.toString(i), message.toString()));
                System.out.println("Enviado: " + message);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
