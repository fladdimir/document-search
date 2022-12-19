package s.testcontainers;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class S3Resource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {
        container = new GenericContainer<>(
                DockerImageName.parse("quay.io/minio/minio"));

        container.withCommand("server /data");
        container.withExposedPorts(9000);
        container.withEnv("MINIO_ROOT_USER", "minio_user");
        container.withEnv("MINIO_ROOT_PASSWORD", "minio_pw");

        // start container before retrieving its URL or other properties
        container.start();

        // return a map containing the configuration the application needs to use the
        // service
        String address = "http://localhost" + ":" + container.getMappedPort(9000);
        return ImmutableMap.of(
                "quarkus.minio.url", address);
    }

    @Override
    public void stop() {
        container.close();
    }

}
