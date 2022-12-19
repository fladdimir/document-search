package s;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import s.search.ElasticSearchService.DocSearchResult;

/**
 * E2E-Test with uploads and search
 */
@QuarkusTest
public class E2eTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        RestAssured.given()
                .when().delete("/delete")
                .then().statusCode(204);
    }

    @Test
    void test() throws Throwable {

        search("", 0);

        upload("READ ME.pdf");

        await_search("", 1);

        upload("lismoi.pdf");

        await_search("", 2);

        search("quarkus", 1, "READ ME.pdf");
        search("traduire", 1, "lismoi.pdf");
        search("mythical", 0);
    }

    private void await_search(String term, int numExpectedResults, String... hitIds) {
        Awaitility.await().pollDelay(1, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> search(term, numExpectedResults, hitIds));
    }

    private void search(String term, int numExpectedResults, String... hitIds) {
        Response response = RestAssured.given().param("for", term).when().get("/search").andReturn();
        response.then().statusCode(200);
        List<DocSearchResult> body = Arrays.asList(response.as(DocSearchResult[].class));
        Assertions.assertThat(body).hasSize(numExpectedResults);
        if (hitIds.length > 0)
            Assertions.assertThat(body.stream().map(DocSearchResult::getId)).containsExactly(hitIds);
    }

    private void upload(String name) throws URISyntaxException {
        var resource = this.getClass().getClassLoader().getResource(name);
        var file = new File(resource.toURI());

        RestAssured.given().contentType(MediaType.MULTIPART_FORM_DATA).multiPart(file).param("fileName", name)
                .when().post("/upload")
                .then()
                .statusCode(200);
    }

}
