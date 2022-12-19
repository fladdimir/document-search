package s;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import s.docentity.DataService;
import s.docentity.DocEntity;
import s.docentity.DocEntityRepository;

@QuarkusTest
class DocRepoTest {

    @Inject
    DocEntityRepository repo;

    @Inject
    DataService service;

    @AfterEach
    @BeforeEach
    void cleanup() {
        repo.deleteAllInBatch();
    }

    @Test
    void test() {
        String fileName = "myUpload";

        var result = service.handleUpload(fileName);

        assertThat(result.getFilename()).isEqualTo(fileName);
        assertThat(result.isIndexed()).isFalse();

        Optional<DocEntity> found = repo.findById(fileName);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(result);
    }

}
