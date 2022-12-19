package s.testcontainers;

import java.util.Map;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    public static final String NETWORK_ALIAS = "postgres";
    private JdbcDatabaseContainer<?> container;

    @Override
    public Map<String, String> start() {

        container = new PostgreSQLContainer<>("postgres:latest");

        container.withNetwork(NetworkResource.NETWORK);
        container.withNetworkAliases(NETWORK_ALIAS);

        container.withExposedPorts(5432);
        container.withDatabaseName("postgres");
        container.withUsername("postgres");
        container.withPassword("postgres");

        container.withCommand("postgres",
                "-c",
                "log_statement=all",
                "-c",
                "wal_level=logical");

        // start container before retrieving its URL or other properties
        container.start();

        String jdbcUrl = container.getJdbcUrl();

        // return a map containing the configuration the application needs to use the
        // service
        return ImmutableMap.of(
                "quarkus.datasource.username", container.getUsername(),
                "quarkus.datasource.password", container.getPassword(),
                "quarkus.datasource.jdbc.url", jdbcUrl);
    }

    @Override
    public void stop() {
        container.close();
    }
}