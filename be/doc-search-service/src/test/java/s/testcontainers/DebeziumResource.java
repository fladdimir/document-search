package s.testcontainers;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.debezium.testing.testcontainers.DebeziumContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class DebeziumResource implements QuarkusTestResourceLifecycleManager {

    private DebeziumContainer container;

    @Override
    public Map<String, String> start() {

        container = new DebeziumContainer("debezium/connect:2.0.1.Final");

        container.withExposedPorts(8083);
        container.withEnv(Map.of(
                "BOOTSTRAP_SERVERS", KafkaResource.NETWORK_ALIAS + ":19092",
                "GROUP_ID", "1",
                "CONFIG_STORAGE_TOPIC", "cdc.configs",
                "OFFSET_STORAGE_TOPIC", "cdc.offset",
                "STATUS_STORAGE_TOPIC", "cdc.status"));

        container.withNetwork(NetworkResource.NETWORK);

        // start container before retrieving its URL or other properties
        container.start();

        // Slf4jLogConsumer logConsumer = new
        // Slf4jLogConsumer(LoggerFactory.getLogger(getClass()));
        // container.followOutput(logConsumer);

        // return a map containing the configuration the application needs to use the
        // service
        return ImmutableMap.of(
                "quarkus.rest-client.\"s.search.SetupDebeziumOnStartup$DebeziumSetupService\".url",
                "http://localhost:" + container.getMappedPort(8083),
                "debezium-setup.postgresHost", PostgresResource.NETWORK_ALIAS);
    }

    @Override
    public void stop() {
        container.close();
    }
}