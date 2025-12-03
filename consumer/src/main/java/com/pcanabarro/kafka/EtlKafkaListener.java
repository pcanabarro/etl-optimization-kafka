package com.pcanabarro.kafka;

import com.pcanabarro.metrics.EtlMetrics;
import com.pcanabarro.service.DatabaseService;
import com.pcanabarro.service.TransformService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

@Component
public class EtlKafkaListener {

    private final TransformService transformService;
    private final DatabaseService databaseService;
    private final EtlMetrics metrics;
    private final ConsumerFactory<String, String> consumerFactory;

    public EtlKafkaListener(TransformService transformService,
                            DatabaseService databaseService,
                            EtlMetrics metrics,
                            ConsumerFactory<String, String> consumerFactory) {
        this.transformService = transformService;
        this.databaseService = databaseService;
        this.metrics = metrics;
        this.consumerFactory = consumerFactory;
    }

    @KafkaListener(topics = "${etl.kafka.topic}", groupId = "${etl.kafka.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {

        long msgStart = System.nanoTime();
        long endStart = System.nanoTime();

        String payload = record.value();
        long msgSizeBytes = payload.getBytes().length;

        long transformNs;
        long dbNs;
        boolean error = false;
        boolean slowQuery = false;

        metrics.incrementPartitionCount(record.partition());
        metrics.incrementThreadCount(Thread.currentThread().getName());

        long tStart = System.nanoTime();
        String sql = transformService.transform(payload);
        transformNs = System.nanoTime() - tStart;

        long dbStart = System.nanoTime();
        try {
            slowQuery = databaseService.execute(sql); // returns true if slow
        } catch (Exception e) {
            System.out.println("Error executing SQL: " + sql);
            error = true;
        }
        dbNs = System.nanoTime() - dbStart;

        long endToEndNs = System.nanoTime() - endStart;
        long kafkaProcessingNs = System.nanoTime() - msgStart;

        metrics.record(
                transformNs,
                dbNs,
                endToEndNs,
                kafkaProcessingNs,
                error,
                slowQuery,
                msgSizeBytes
        );
    }
}
