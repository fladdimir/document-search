# document search

## quick-start

```sh
# start all needed services:
# postgres, kafka (>redpanda, +console), debezium, minio, elasticsearch 
docker-compose up

# build and run the application:
# (the build container will download the java dependencies, which may take some time)
docker-compose up -f docker-compose.app.yml

```

## local dev ui

### elasticsearch

check index for uploaded documents:  
<http://localhost:9200/docs/_search?pretty=true&q>=*:*

### minio

<http://localhost:9000>  
(-> redirect to console, credentials from docker-compose.yml, e.g. 'minio_user' + 'minio_pw')

### redpanda kafka console

<http://localhost:8080>

## tests

with testcontainers:

```sh
cd be/doc-search-service
./gradlew testcontainersTest
```

with all services running locally (e.g. via `docker-compose up`)

```sh
cd be/doc-search-service
./gradlew test
```
