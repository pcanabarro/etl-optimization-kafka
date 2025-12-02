# ETL PROCESS OPTIMIZATION WITH APACHE KAFKA: A STUDY ON SCALABILITY AND LATENCY

This repository contains the final paper for Computer Science undergraduate program. The paper explores pache Kafka as a messaging system for distributed applications, focusing on practical integration between systems through Kafka to ensure scalability, fault tolerance, and real-time data processing.

    This project implements a fully functional real-time ETL pipeline using Apache Kafka, Debezium, MySQL, and PostgreSQL. Instead of inserting messages directly into Kafka, the system captures database changes (INSERT, UPDATE, DELETE) from a transactional MySQL instance through Debezium’s CDC mechanism, streams those events into Kafka topics, and processes them asynchronously with scalable Kafka consumers written in Java. The transformed data is finally persisted into a reporting PostgreSQL database, ensuring consistency and low latency end-to-end.

    The primary goal of the architecture is to demonstrate how Kafka’s partitioning, consumer groups, CDC integration, and distributed log model can improve ETL performance and scalability in data-intensive environments. The pipeline simulates real scenarios where applications write to operational databases, and the system replicates those changes in real-time to analytical storage with no data loss and without tightly coupling the systems involved.



Consumer:

Docker:

- Server ports: 3306 (mysql), 29092 (kafka), 5432 (postgres)
- Client ports: 8083 (debezium), 8085 (kafka ui)

Producer:

*WIP...*
