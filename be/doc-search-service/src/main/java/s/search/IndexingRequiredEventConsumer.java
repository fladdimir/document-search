package s.search;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.reactive.messaging.annotations.Blocking;

/**
 * Message consumer for indexing events
 */
@ApplicationScoped
public class IndexingRequiredEventConsumer {

    @Inject
    FaultTolerantIndexingRequestProcessor service;

    @Incoming("indexing-events")
    @Blocking(ordered = false) // messages only affect distinct documents, so ordering is expendable
    public void receive(String message) {

        // todo: souround with try/catch -> dead-letter

        message = getPayload(message);

        service.processIndexingRequest(message);
    }

    private String getPayload(String message) {
        return message.replace("\"", "");
    }

}
