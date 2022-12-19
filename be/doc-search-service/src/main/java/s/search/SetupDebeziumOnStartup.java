package s.search;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonObject;

// @IfBuildProfile("dev") // todo: just for (local) development
@ApplicationScoped
public class SetupDebeziumOnStartup {

    private static final String CONFIG_PREFIX = "debezium-setup.";

    @ConfigProperty(name = CONFIG_PREFIX + "postgresHost", defaultValue = "localhost")
    String postgresHost;

    @ConfigProperty(name = CONFIG_PREFIX + "postgresPort", defaultValue = "5432")
    String postgresPort;

    @ConfigProperty(name = CONFIG_PREFIX + "postgresDbName", defaultValue = "postgres")
    String postgresDbName;

    @ConfigProperty(name = CONFIG_PREFIX + "postgresUser", defaultValue = "postgres")
    String postgresUser;

    @ConfigProperty(name = CONFIG_PREFIX + "postgresPassword", defaultValue = "postgres")
    String postgresPassword;

    @Inject
    @RestClient
    DebeziumSetupService service;

    @Inject
    Logger logger;

    public void onStart(@Observes StartupEvent ev) throws Exception {
        logger.info("setting up debezium to tail outbox table transaction log");
        try {
            service.setupConnector(getBody());
        } catch (ConflictException e) {
            // ok, already setup
        }
    }

    // @IfBuildProfile("dev")
    @ApplicationScoped
    @RegisterRestClient
    @RegisterProvider(ConflictDetectingResponseMapper.class)
    public static interface DebeziumSetupService {

        @POST
        @Path("/connectors")
        @Consumes(MediaType.APPLICATION_JSON)
        void setupConnector(JsonObject body);
    }

    private JsonObject getBody() {
        JsonObject config1 = JsonObject.of(
                // connector/plugin:
                "connector.class", "io.debezium.connector.postgresql.PostgresConnector",
                "plugin.name", "pgoutput",
                // db:
                "database.hostname", postgresHost,
                "database.port", postgresPort,
                "database.user", postgresUser,
                "database.password", postgresPassword,
                "database.dbname", postgresDbName);
        JsonObject config2 = JsonObject.of(
                // table:
                "table.include.list", "public.outboxevent",
                // topic:
                "topic.prefix", "upload-service-outbox",
                "topic.creation.default.partitions", "2",
                "topic.creation.default.replication.factor", "1",
                // transforms:
                "transforms", "outbox",
                "transforms.outbox.type", "io.debezium.transforms.outbox.EventRouter");

        return JsonObject.of("name", "upload-service-outbox-connector",
                "config", config1.mergeIn(config2));
    }

    public static class ConflictException extends RuntimeException {
    }

    public static class ConflictDetectingResponseMapper implements ResponseExceptionMapper<RuntimeException> {

        @Override
        public RuntimeException toThrowable(Response response) {

            int status = response.getStatus();

            if (status == 409) {
                return new ConflictException();
            }
            return new IllegalStateException("" + response.getStatus());
        }
    }
}
