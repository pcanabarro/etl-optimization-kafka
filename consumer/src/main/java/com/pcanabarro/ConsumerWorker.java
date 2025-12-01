package com.pcanabarro;

import com.pcanabarro.database.Database;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.json.JSONObject;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.pcanabarro.database.Database.executePostgresStatement;

class ConsumerWorker implements Runnable {
    private final String consumerName;

    public ConsumerWorker(String consumerName) {
        this.consumerName = consumerName;
    }

    @Override
    public void run() {
        Properties props = new Properties();
        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            props.load(input);
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.get("kafka.bootstrap.servers"));
            props.put(ConsumerConfig.GROUP_ID_CONFIG, props.get("kafka.group.id"));
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,  props.get("kafka.auto.offset.reset"));
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerName);
        } catch (Exception e) {
            System.out.println("unable to load config.properties");
            e.printStackTrace();
        }

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(props.getProperty("kafka.topic")));
            System.out.println(consumerName + " esperando mensagens...");

            long batchStartTime = System.nanoTime();
            int messageCount = 0;

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    long startTime = System.nanoTime();
                    System.out.printf("%s consumido [key=%s, partition=%s, value=%s, offset=%d]\n",
                            consumerName, record.key(), record.partition(), record.value(), record.offset());

                    String sqlStatement = transformMessageForPostgres(record);
                    long transformTime = System.nanoTime() - startTime;
                    System.out.printf("Transform Time: %d ms", transformTime);

                    executePostgresStatement(sqlStatement);

                    long endTime = System.nanoTime();
                    long durationMs = (endTime - startTime) / 1_000_000;
                    System.out.printf("Processing time: %d ms\n", durationMs);
                    System.out.println(" ");

                    messageCount++;
                    if (messageCount % 10_000 == 0) {
                        long batchEndTime = System.nanoTime();
                        long batchDurationMs = (batchEndTime - batchStartTime) / 1_000_000;
                        System.out.printf("Processed 10,000 messages in %d ms\n", batchDurationMs);
                        batchStartTime = System.nanoTime();
                    }
                }
            }
        }
    }

    private static String transformMessageForPostgres(ConsumerRecord<String, String> record) {
        try {
            String messageValue = record.value();
            JSONObject json = new JSONObject(messageValue);
            JSONObject after = json.optJSONObject("after");

            if (after == null) {
                return null;
            }

            JSONObject source = json.getJSONObject("source");
            String table = source.getString("table");
            String op = json.getString("op");

            return switch (op) {
                case "c" -> insertMessageToPostgres(after, table);
                case "u" -> updateMessageToPostgres(after, table);
                case "d" -> deleteMessageToPostgres(after, table);
                default -> "Unknown operation: " + op;
            };
        } catch (Exception e) {
            System.err.println("Transformation error: " + e.getMessage());
            return null;
        }
    }

    private static String insertMessageToPostgres(JSONObject after, String table) {
        switch (table) {
            case "job_position":
                int id = after.getInt("id");
                String title = after.getString("title");
                String department = after.getString("department");
                String createdAt = after.getString("created_at");
                return String.format(
                        "INSERT INTO job_position (id, title, department, created_at) VALUES (%d, '%s', '%s', '%s')",
                        id, title.replace("'", "''"), department.replace("'", "''"), createdAt
                );
            case "salary":
                int salaryId = after.getInt("id");
                int employeeId = after.getInt("employee_id");
                String amount = after.getString("amount");
                int effectiveFrom = after.getInt("effective_from");
                return String.format(
                        "INSERT INTO salary (id, employee_id, amount, effective_from) VALUES (%d, %d, '%s', %d)",
                        salaryId, employeeId, amount, effectiveFrom
                );
            case "employee":
                int empId = after.getInt("id");
                String name = after.getString("name");
                String email = after.getString("email");
                int jobPositionId = after.getInt("job_position_id");
                int hiredAt = after.getInt("hired_at");
                return String.format(
                        "INSERT INTO employee (id, name, email, job_position_id, hired_at) VALUES (%d, '%s', '%s', %d, %d)",
                        empId, name.replace("'", "''"), email.replace("'", "''"), jobPositionId, hiredAt
                );
            default:
                System.err.println("Unknown table: " + table);
                return null;
        }
    }

    private static String updateMessageToPostgres(JSONObject after, String table) {
        switch (table) {
            case "job_position":
                int id = after.getInt("id");
                String title = after.getString("title");
                String department = after.getString("department");
                String createdAt = after.getString("created_at");
                return String.format(
                        "UPDATE job_position SET title='%s', department='%s', created_at='%s' WHERE id=%d",
                        title.replace("'", "''"), department.replace("'", "''"), createdAt, id
                );
            case "salary":
                int salaryId = after.getInt("id");
                int employeeId = after.getInt("employee_id");
                String amount = after.getString("amount");
                int effectiveFrom = after.getInt("effective_from");
                return String.format(
                        "UPDATE salary SET employee_id=%d, amount='%s', effective_from=%d WHERE id=%d",
                        employeeId, amount, effectiveFrom, salaryId
                );
            case "employee":
                int empId = after.getInt("id");
                String name = after.getString("name");
                String email = after.getString("email");
                int jobPositionId = after.getInt("job_position_id");
                int hiredAt = after.getInt("hired_at");
                return String.format(
                        "UPDATE employee SET name='%s', email='%s', job_position_id=%d, hired_at=%d WHERE id=%d",
                        name.replace("'", "''"), email.replace("'", "''"), jobPositionId, hiredAt, empId
                );
            default:
                System.err.println("Unknown table for update: " + table);
                return null;
        }
    }

    private static String deleteMessageToPostgres(JSONObject after, String table) {
        int id = after.getInt("id");
        return String.format("DELETE FROM %s WHERE id=%d", table, id);
}
}