package s.search;

import java.time.Instant;

import io.debezium.outbox.quarkus.ExportedEvent;

class IndexingRequiredEvent implements ExportedEvent<String, String> {

    private static final String TYPE = "IndexingEvent";
    private static final String EVENT_TYPE = "IndexingRequired";

    private final String id;
    private final String content;
    private final Instant timestamp;

    public IndexingRequiredEvent(String id, String content, Instant timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Override
    public String getAggregateId() {
        return id;
    }

    @Override
    public String getAggregateType() {
        return TYPE;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getPayload() {
        return content;
    }

}
