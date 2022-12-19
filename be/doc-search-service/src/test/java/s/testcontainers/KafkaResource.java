package s.testcontainers;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KafkaResource implements QuarkusTestResourceLifecycleManager {

    public static final String NETWORK_ALIAS = "kafka";
    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {

        // https://www.confluent.io/blog/kafka-listeners-explained/

        // container = new
        // KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));
        container = new GenericContainer<>(DockerImageName.parse("vectorized/redpanda"));

        container.withExposedPorts(9092);

        container.withNetworkAliases(NETWORK_ALIAS);

        container.withNetwork(NetworkResource.NETWORK);

        container.withCommand(
                "redpanda", "start", "--smp 1", "--reserve-memory", "0M", "--overprovisioned",
                "--set redpanda.empty_seed_starts_cluster=false", "--seeds \"kafka:33145\"", "--kafka-addr",
                "INSIDE://0.0.0.0:19092,OUTSIDE://0.0.0.0:9092", "--advertise-kafka-addr",
                "INSIDE://kafka:19092,OUTSIDE://localhost:9092", "--advertise-rpc-addr kafka:33145"

        );

        container.getPortBindings().add("9092:9092");
        container.getPortBindings().add("19092:19092");

        // start container before retrieving its URL or other properties
        container.start();

        // Slf4jLogConsumer logConsumer = new
        // Slf4jLogConsumer(LoggerFactory.getLogger(getClass()));
        // container.followOutput(logConsumer);

        // return a map containing the configuration the application needs to use the
        // service
        String bootstrapServer = "localhost:" + container.getMappedPort(9092);
        return ImmutableMap.of(
                "kafka.bootstrap.servers", bootstrapServer,
                "mp.messaging.incoming.indexing-events.bootstrap.servers", bootstrapServer);
    }

    @Override
    public void stop() {
        container.close();
    }

}
