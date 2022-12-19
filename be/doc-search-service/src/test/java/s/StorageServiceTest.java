package s;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import s.blobstorage.StorageService;

@QuarkusTest
class StorageServiceTest {

    @Inject
    StorageService service;

    @BeforeEach
    @AfterEach
    void cleanup() {
        service.deleteBucketIfExists();
    }

    @Test
    void test() throws Exception {

        var resource = this.getClass().getClassLoader().getResource("README.md");
        service.upload(resource.openStream(), "README.md");
        var dl = service.download("README.md");

        assertThat(dl).containsExactly(resource.openStream().readAllBytes());
    }

}
