package s.search;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import s.docentity.DocEntity;

/**
 * Service for firing asynchronous indexing events
 */
@ApplicationScoped
public class AsyncIndexingService {

    // todo: needs to be in the file-service

    @Inject
    Event<IndexingRequiredEvent> outbox;

    @Transactional(value = TxType.MANDATORY)
    public void indexAsync(DocEntity doc) {
        IndexingRequiredEvent event = new IndexingRequiredEvent(doc.getFilename(), doc.getFilename(),
                Instant.now());
        outbox.fire(event);
    }

}
