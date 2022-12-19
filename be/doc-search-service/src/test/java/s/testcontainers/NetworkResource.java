package s.testcontainers;

import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.Network;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class NetworkResource implements QuarkusTestResourceLifecycleManager {

    public static Network NETWORK = Network.newNetwork();

    @Override
    public Map<String, String> start() {
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        NETWORK.close();
    }

}
