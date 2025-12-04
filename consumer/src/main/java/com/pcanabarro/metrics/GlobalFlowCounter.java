package com.pcanabarro.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class GlobalFlowCounter {

    public static final AtomicLong END_TO_END_MESSAGES = new AtomicLong(0);
    public static final AtomicLong LAST_BATCH_START_NS = new AtomicLong(System.nanoTime());
}
