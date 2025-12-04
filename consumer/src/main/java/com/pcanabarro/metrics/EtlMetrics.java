package com.pcanabarro.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EtlMetrics {

    private final MeterRegistry reg;

    private final Counter processed;
    private final Counter errors;

    private final DistributionSummary transformNs;
    private final DistributionSummary dbNs;
    private final DistributionSummary endToEndNs;

    private final Counter dbSlowQueries;

    private final Counter kafkaRecords;
    private final Counter kafkaBytes;
    private final DistributionSummary kafkaLag;
    private final DistributionSummary kafkaProcessingNs;

    private final Map<Integer, Counter> partitionCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> threadCounters = new ConcurrentHashMap<>();

    public EtlMetrics(MeterRegistry reg) {
        this.reg = reg;

        processed = reg.counter("etl.processed.total");
        errors = reg.counter("etl.errors.total");

        transformNs = DistributionSummary.builder("etl.transform.ns")
                .description("Transformation time (ns)")
                .register(reg);

        dbNs = DistributionSummary.builder("etl.db.ns")
                .description("DB execution time (ns)")
                .register(reg);

        endToEndNs = DistributionSummary.builder("etl.end_to_end.ns")
                .description("Kafka → Transform → DB latency (ns)")
                .register(reg);

        dbSlowQueries = reg.counter("etl.db.slow_queries.total");

        kafkaRecords = reg.counter("etl.kafka.records.total");
        kafkaBytes = reg.counter("etl.kafka.bytes.total");

        kafkaLag = DistributionSummary.builder("etl.kafka.lag")
                .description("Kafka consumer lag per message")
                .register(reg);

        kafkaProcessingNs = DistributionSummary.builder("etl.kafka.processing.ns")
                .description("Entire Kafka message handling duration (ns)")
                .register(reg);
    }

    public void incrementPartitionCount(int partition) {
        partitionCounters
                .computeIfAbsent(partition, p ->
                        Counter.builder("etl.kafka.partition.records")
                                .tag("partition", String.valueOf(p))
                                .description("Messages consumed per partition")
                                .register(reg)
                )
                .increment();
    }

    public void incrementThreadCount(String threadName) {
        threadCounters
                .computeIfAbsent(threadName, t ->
                        Counter.builder("etl.kafka.thread.records")
                                .tag("thread", t)
                                .description("Messages processed per consumer thread")
                                .register(reg)
                )
                .increment();
    }

    public void record(long transform, long db, long endToEnd, long kafkaProcessing,
                       boolean error, boolean slowQuery, long msgSize) {

        processed.increment();

        transformNs.record(transform);
        dbNs.record(db);
        endToEndNs.record(endToEnd);
        kafkaProcessingNs.record(kafkaProcessing);

        kafkaRecords.increment();
        kafkaBytes.increment(msgSize);

        if (slowQuery) dbSlowQueries.increment();
        if (error) errors.increment();
    }
}
