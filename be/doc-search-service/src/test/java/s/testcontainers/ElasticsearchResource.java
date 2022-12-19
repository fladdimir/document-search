package s.testcontainers;

import java.nio.file.Path;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ElasticsearchResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {
        Path dockerfile = Path.of("../../opensearch/Dockerfile");

        container = new GenericContainer<>(
                new ImageFromDockerfile().withDockerfile(dockerfile));

        container.withExposedPorts(9200);

        container.withEnv("OPENSEARCH_JAVA_OPTS", "-Xms2g -Xmx8g");
        container.withEnv("plugins.security.disabled", "true");
        container.withEnv("discovery.type", "single-node");
        container.withEnv("http.max_content_length", "100MB");
        // TODO:
        container.withEnv("network.publish_host", "localhost");
        container.withExposedPorts(9200);

        // start container before retrieving its URL or other properties
        container.start();

        // return a map containing the configuration the application needs to use the
        // service
        String address = "localhost:" + container.getMappedPort(9200);
        return ImmutableMap.of(
                "quarkus.elasticsearch.hosts", address);
    }

    @Override
    public void stop() {
        container.close();
    }

}
