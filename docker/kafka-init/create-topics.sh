#!/bin/bash
sleep 10

kafka-topics --create \
  --topic etl_topic \
  --bootstrap-server kafka-broker:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

kafka-topics --create \
  --topic dbhistory_source_db \
  --bootstrap-server kafka-broker:9092 \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

echo "✅ Tópico 'etl_topic' criado (3 partitions, RF=1)"
echo "✅ Tópico 'dbhistory_source_db' criado (1 partition, RF=1)"
