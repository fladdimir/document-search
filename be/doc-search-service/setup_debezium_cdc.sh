#!/bin/bash

# this request is automatically executed on app startup,
# but may also be executed manually

curl --request POST \
  --url http://localhost:8083/connectors \
  --header 'Content-Type: application/json' \
  --data '{
  "name": "upload-service-outbox-connector",
  "config": {

    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "plugin.name": "pgoutput",

    "database.hostname": "localhost", 
    "database.port": "5432", 
    "database.user": "postgres", 
    "database.password": "postgres", 
    "database.dbname" : "postgres",

    "table.include.list": "public.outboxevent",
    "topic.prefix": "upload-service-outbox",

    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",

    "topic.creation.default.partitions": "2",
    "topic.creation.default.replication.factor": "1"
  }
}'
# additional config for json payload:
    # "transforms.outbox.table.expand.json.payload": "true",
    # "value.converter": "org.apache.kafka.connect.json.JsonConverter",
