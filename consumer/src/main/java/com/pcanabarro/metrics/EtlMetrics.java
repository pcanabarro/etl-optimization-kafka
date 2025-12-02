package com.pcanabarro.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class EtlMetrics {

    private final Counter errors;
    private final Counter processed;
    private final DistributionSummary transformNs;
    private final DistributionSummary dbNs;

    public EtlMetrics(MeterRegistry reg) {
        processed = reg.counter("etl.processed.total");
        errors = reg.counter("etl.errors.total");

        transformNs = DistributionSummary
                .builder("etl.transform.ns")
                .register(reg);

        dbNs = DistributionSummary
                .builder("etl.db.ns")
                .register(reg);
    }

    public void record(long transform, long db, boolean error) {
        processed.increment();
        transformNs.record(transform);
        dbNs.record(db);
        if (error) errors.increment();
    }
}
