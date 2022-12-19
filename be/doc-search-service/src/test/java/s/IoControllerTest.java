package s;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

@QuarkusTest
class IoControllerTest {

    @InjectMock
    UploadService service;

    @Test
    void test() throws URISyntaxException, IOException {
        var resource = this.getClass().getClassLoader().getResource("README.md");
        File file = new File(resource.toURI());

        given().contentType(MediaType.MULTIPART_FORM_DATA).multiPart(file).param("fileName", "myFileX.md")
                .when().post("/upload")
                .then()
                .statusCode(200);

        verify(service).handleUpload(argThat(contentEquals(resource.openStream())),
                eq("myFileX.md"));
    }

    private ArgumentMatcher<InputStream> contentEquals(InputStream expected) {
        return actual -> {
            try {
                return Arrays.equals(actual.readAllBytes(), expected.readAllBytes());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        };
    }

}