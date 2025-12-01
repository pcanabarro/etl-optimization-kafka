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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static com.pcanabarro.database.Database.executePostgresStatement;

class ConsumerWorker implements Runnable {
    private final String consumerName;
    private static final AtomicLong GLOBAL_COUNTER = new AtomicLong(0);
    private static final AtomicLong GLOBAL_BATCH_START = new AtomicLong(System.nanoTime());
    private static final Map<String, List<String>> DATE_FIELDS = Map.of(
            "salary", List.of("effective_from"),
            "employee", List.of("hired_at")
    );

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

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    long startTime = System.nanoTime();
//                    System.out.printf("%s consumido [key=%s, partition=%s, value=%s, offset=%d]\n",
//                            consumerName, record.key(), record.partition(), record.value(), record.offset());

                    String sqlStatement = transformMessageForPostgres(record);
                    long transformTime = System.nanoTime() - startTime;

                    executePostgresStatement(sqlStatement);

                    long endTime = System.nanoTime();
                    long durationMs = endTime - startTime;

                    System.out.printf("Transform Time: %d ns\n", transformTime);
                    System.out.printf("Processing time: %d ns\n", durationMs);
                    System.out.println(" ");

                    long totalProcessed = GLOBAL_COUNTER.incrementAndGet();
                    if (totalProcessed % 10_000 == 0) {
                        long now = System.nanoTime();
                        long start = GLOBAL_BATCH_START.getAndSet(now);
                        long batchDurationMs = (now - start) / 1_000_000;

                        System.out.printf(
                                "[GLOBAL] %d messages processed (last 10k took %d ms) — triggered by %s\n",
                                totalProcessed, batchDurationMs, this.consumerName
                        );
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
            String op = json.getString("op");
            JSONObject source = json.getJSONObject("source");
            String table = source.getString("table");

            if (after == null) {
                if (op.equals("d")) {
                    JSONObject before = json.optJSONObject("before");
                    return deleteMessageToPostgres(before, table);
                }
            }

            validateAndTransformAfter(after, table);
            return insertAndUpdatePostgresSql(after, table, op);
        } catch (Exception e) {
            System.err.println("Transformation error: " + e.getMessage());
            return null;
        }
    }

    private static String insertAndUpdatePostgresSql(JSONObject after, String table, String operation) {
        switch (table) {
            case "job_position":
                int id = after.getInt("id");
                String title = after.getString("title");
                String department = after.getString("department");
                String createdAt = after.getString("created_at");

                return switch (operation) {
                    case "c" -> String.format(
                            "INSERT INTO job_position (id, title, department, created_at) VALUES (%d, '%s', '%s', '%s')",
                            id, title.replace("'", "''"), department.replace("'", "''"), createdAt
                    );
                    case "u" -> String.format(
                            "UPDATE job_position SET title='%s', department='%s', created_at='%s' WHERE id=%d",
                            title.replace("'", "''"), department.replace("'", "''"), createdAt, id
                    );
                    default -> {
                        System.err.println("Unknown operation for job_position: " + operation);
                        yield null;
                    }
                };
            case "salary":
                int salaryId = after.getInt("id");
                int employeeId = after.getInt("employee_id");
                String amount = after.getString("amount");
                String effectiveFrom = after.getString("effective_from");

                return switch (operation) {
                    case "c" -> String.format(
                            "INSERT INTO salary (id, employee_id, amount, effective_from) VALUES (%d, %d, '%s', '%s')",
                            salaryId, employeeId, amount, effectiveFrom
                    );
                    case "u" -> String.format(
                            "UPDATE salary SET employee_id=%d, amount='%s', effective_from='%s' WHERE id=%d",
                            employeeId, amount, effectiveFrom, salaryId
                    );
                    default -> {
                        System.err.println("Unknown operation for salary: " + operation);
                        yield null;
                    }
                };
            case "employee":
                int empId = after.getInt("id");
                String name = after.getString("name");
                String email = after.getString("email");
                int jobPositionId = after.getInt("job_position_id");
                String hiredAt = after.getString("hired_at");

                return switch (operation) {
                    case "c" -> String.format(
                            "INSERT INTO employee (id, name, email, job_position_id, hired_at) VALUES (%d, '%s', '%s', %d, '%s')",
                            empId, name.replace("'", "''"), email.replace("'", "''"), jobPositionId, hiredAt
                    );
                    case "u" -> String.format(
                            "UPDATE employee SET name='%s', email='%s', job_position_id=%d, hired_at='%s' WHERE id=%d",
                            name.replace("'", "''"), email.replace("'", "''"), jobPositionId, hiredAt, empId
                    );
                    default -> {
                        System.err.println("Unknown operation for employee: " + operation);
                        yield null;
                    }
                };
            default:
                System.err.println("Unknown table: " + table);
                return null;
        }
    }

    private static String deleteMessageToPostgres(JSONObject before, String table) {
        int id = before.getInt("id");
        return String.format("DELETE FROM %s WHERE id=%d", table, id);
    }

    private static void validateAndTransformAfter(JSONObject after, String table) {
        if (after == null || table == null) return;

        List<String> fieldsToConvert = DATE_FIELDS.get(table);
        if (fieldsToConvert == null) return;

        for (String field : fieldsToConvert) {
            if (!after.has(field)) continue;

            Object value = after.get(field);

            if (value instanceof Number) {
                int days = ((Number) value).intValue();
                LocalDate convertedDate = LocalDate.ofEpochDay(days);
                after.put(field, convertedDate.toString());
            }
            else if (value instanceof String) {
                return;
            }
        }
    }
}