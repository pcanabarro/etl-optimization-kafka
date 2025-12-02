package com.pcanabarro.kafka;

import com.pcanabarro.metrics.EtlMetrics;
import com.pcanabarro.service.DatabaseService;
import com.pcanabarro.service.TransformService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EtlKafkaListener {

    private final TransformService transformService;
    private final DatabaseService databaseService;
    private final EtlMetrics metrics;

    public EtlKafkaListener(TransformService transformService,
                            DatabaseService databaseService,
                            EtlMetrics metrics) {
        this.transformService = transformService;
        this.databaseService = databaseService;
        this.metrics = metrics;
    }

    @KafkaListener(topics = "${etl.kafka.topic}", groupId = "${etl.kafka.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {
        long tStart = System.nanoTime();
        System.out.println("Received message: " + record.value());
        String sql = transformService.transform(record.value());
        long transformNs = System.nanoTime() - tStart;

        long dbStart = System.nanoTime();
        boolean error = false;
        try {
            databaseService.execute(sql);
        } catch (Exception ex) {
            error = true;
        }
        long dbNs = System.nanoTime() - dbStart;

        metrics.record(transformNs, dbNs, error);
    }
}
