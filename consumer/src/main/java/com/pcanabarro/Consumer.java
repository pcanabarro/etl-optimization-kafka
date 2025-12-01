package com.pcanabarro;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer {
    public static void main(String[] args) {
        int numConsumers = 1; // Number of threads/consumers
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        for (int i = 0; i < numConsumers; i++) {
            String consumerName = "etl-consumer-" + i;
            executor.submit(new ConsumerWorker(consumerName));
        }
    }
}
