#!/bin/bash
# Aguarda o Kafka ficar pronto
sleep 10

# Cria o tópico etl_topic com 3 partições e fator de replicação 1
kafka-topics --create \
  --topic etl_topic \
  --bootstrap-server kafka-broker:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# Apenas para debug
echo "✅ Tópico 'etl_topic' criado (3 partitions, RF=1)"
