package s;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import s.search.ElasticSearchService;
import s.search.ElasticSearchService.DocSearchResult;

@QuarkusTest
class ElasticSearchServiceTest {

    @Inject
    ElasticSearchService ess;

    private String id;

    private static final String FILE_NAME = "READ ME.pdf";

    @BeforeEach
    void beforeEach() throws ParseException, IOException {

        clearIndex();

        byte[] data = getClass().getClassLoader().getResourceAsStream(FILE_NAME).readAllBytes();
        id = FILE_NAME;
        ess.indexBlocking(data, id);
    }

    @AfterEach
    void clearIndex() {
        try {
            ess.deleteDocsIndex();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void test_content() throws IOException {

        var result = ess.search("Running the application in dev mode");

        assertResult(result);
        assertHighlight(result);
    }

    private void assertHighlight(List<DocSearchResult> result) {
        assertThat(result.get(0).getHighlights()).hasSize(5);
        assertThat(result.get(0).getHighlights())
                .allMatch(hl -> Stream.of("run", "application", "dev").anyMatch(s -> hl.toLowerCase().contains(s)));
    }

    @Test
    void test_filename() throws IOException {

        var result = ess.search(FILE_NAME);

        assertResult(result);
    }

    @Test
    void test_wildcard() throws IOException {

        var result = ess.search("*");

        assertResult(result);
        assertThat(result.get(0).getHighlights()).isEmpty();
    }

    private void assertResult(List<DocSearchResult> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(id);
        assertThat(result.get(0).getScore()).isGreaterThan(0);
    }
}
