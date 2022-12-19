package s.testcontainers;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import s.E2eTest;
import s.testcontainers.sysprop.SetSystemProperty;

/**
 * E2E-Test against newly started testcontainers
 */
// ./gradlew testcontainersTest
@Tag("testcontainers")
// ./gradlew uberJarBuild
// ./gradlew quarkusBuild -Dquarkus.package.type=uber-jar
@SetSystemProperty(key = "build.output.directory", value = "build")
@QuarkusIntegrationTest
@QuarkusTestResource(value = NetworkResource.class, parallel = true, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = PostgresResource.class, parallel = true, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = KafkaResource.class, parallel = true, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = S3Resource.class, parallel = true, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = ElasticsearchResource.class, parallel = true, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = DebeziumResource.class, parallel = true, restrictToAnnotatedClass = true)
class TestcontainersE2eTest extends E2eTest {

}
