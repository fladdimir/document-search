package s.search;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import s.util.TimeLoggingInterceptor.LogExecutionTime;
import s.util.TimeLoggingInterceptor.LogExecutionTimeParameter;

/**
 * Service for indexing and searching documents.
 */
// tbd: replace elasticsearch with opensearch
@ApplicationScoped
public class ElasticSearchService { // todo: interface

    @Inject
    RestClient restClient;

    /**
     * when this method returns the index will already have been refreshed, so that
     * the given content will be searchable
     * 
     * @param data thing to index
     * @param id   of the indexed thing
     */
    @LogExecutionTime
    public void indexBlocking(byte[] data, @LogExecutionTimeParameter(name = "doc_id") String id) {
        String idEncoded = urlEncode(id);
        Request request = new Request(
                "PUT",
                "/docs/_doc/" + idEncoded);
        ElasticSearchUploadData obj = new ElasticSearchUploadData();
        obj.data = Base64.getEncoder().encodeToString(data);
        obj.filename = id;
        request.setJsonEntity(JsonObject.mapFrom(obj).toString());
        request.addParameter("pipeline", "attachment");
        request.addParameter("refresh", "wait_for");
        try {
            restClient.performRequest(request);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Data
    static class ElasticSearchUploadData {
        private String data;
        private String filename;
    }

    @LogExecutionTime
    public List<DocSearchResult> search(@LogExecutionTimeParameter(name = "search_term") String value) {

        JsonObject queryJson = createQuery(value);

        Request request = new Request(
                "GET",
                "/docs/_search");
        request.setJsonEntity(queryJson.encode());
        request.addParameter("filter_path", "took,hits.hits._id,hits.hits._score,hits.hits.highlight");
        request.addParameter("size", "1000");
        String responseBody;
        try {
            Response response = restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        JsonObject json = new JsonObject(responseBody);
        if (json.getJsonObject("hits") == null) {
            return Collections.emptyList();
        }
        JsonArray hits = json.getJsonObject("hits").getJsonArray("hits");

        return IntStream.range(0, hits.size()).mapToObj(hits::getJsonObject)
                .map(jso -> new DocSearchResult(urlDecode(jso.getString("_id")), jso.getDouble("_score"),
                        getHighlights(jso)))
                .toList();
    }

    private List<String> getHighlights(JsonObject hit) {
        if (hit.getJsonObject("highlight") == null) {
            return Collections.emptyList();
        }
        var highlights = hit.getJsonObject("highlight").getJsonArray("attachment.content");
        if (highlights == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, highlights.size()).mapToObj(highlights::getString).toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocSearchResult {
        private String id;
        private double score;
        private List<String> highlights;
    }

    private JsonObject createQuery(String value) {
        if (value == null || value.isBlank()) {
            value = "*";
        }
        return new JsonObject().put("query",
                new JsonObject().put("query_string", new JsonObject().put("query", value)))
                .put("highlight", new JsonObject("{\"fields\" : {\"*\" : {} } }"));
    }

    public void deleteAllDocs() {
        Request request = new Request("POST", "/docs/_delete_by_query");
        request.setJsonEntity("{\"query\": {\"match_all\": {} } }");
        try {
            restClient.performRequest(request);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteDocsIndex() {
        Request request = new Request("DELETE", "/docs");
        String responseBody;
        try {
            Response response = restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JsonObject json = new JsonObject(responseBody);
        boolean ack = json.getBoolean("acknowledged");
        if (!ack) {
            throw new IllegalStateException(responseBody);
        }
    }

    public void onStart(@Observes StartupEvent ev) throws IOException {

        // todo: requires elastic-search to be available on startup
        // todo: retry
        setup_attachment_ingest_pipeline();
        setup_docs_index_settings();
    }

    private void setup_docs_index_settings() throws IOException {

        Request indexExists = new Request("GET", "/docs");
        try {
            restClient.performRequest(indexExists);
        } catch (ResponseException e) { // lets assume its 404
            Request createIndex = new Request("PUT", "/docs");
            restClient.performRequest(createIndex);
        }

        Request request = new Request(
                "PUT",
                "/docs/_settings");
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html#index-max-analyzed-offset
        request.setJsonEntity(
                "{\"index.highlight.max_analyzed_offset\" : 1000000000 }");
        restClient.performRequest(request);
    }

    private void setup_attachment_ingest_pipeline() throws IOException {
        Request request = new Request(
                "PUT",
                "/_ingest/pipeline/attachment");
        // https://www.elastic.co/guide/en/elasticsearch/reference/8.5/attachment.html
        request.setJsonEntity(
                "{\"description\" : \"Extract attachment information\","
                        + "\"processors\" : [" +
                        "{ \"attachment\" : "
                        + "{\"field\" : \"data\", "
                        // + "\"remove_binary\": true, " // todo: does not work with OpenSearch ?
                        + "\"indexed_chars\": -1}}]}");

        restClient.performRequest(request);
    }

    private String urlEncode(String toEncode) {
        return URLEncoder.encode(toEncode, StandardCharsets.UTF_8);
    }

    private String urlDecode(String toDecode) {
        return URLDecoder.decode(toDecode, StandardCharsets.UTF_8);
    }

}
