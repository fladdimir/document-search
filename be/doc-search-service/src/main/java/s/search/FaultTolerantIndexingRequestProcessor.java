package s.search;

import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Retry;

import s.blobstorage.StorageService;
import s.docentity.DocEntityRepository;

/**
 * Service for processing of indexing requests.
 */
@ApplicationScoped
class FaultTolerantIndexingRequestProcessor {

    @Inject
    IndexingRequestProcessor transactionalProcessor;

    @Retry(maxRetries = 5, delay = 10, delayUnit = ChronoUnit.SECONDS, jitter = 200
    // todo: , retryOn = MyRetryableException.class // only on transient failures
    )
    public void processIndexingRequest(String docEntityId) {
        transactionalProcessor.processIndexingRequest(docEntityId);
    }

    @ApplicationScoped
    static class IndexingRequestProcessor {

        @Inject
        DocEntityRepository docRepo;

        @Inject
        StorageService storageService;

        @Inject
        ElasticSearchService searchService;

        @ActivateRequestContext
        public void processIndexingRequest(String docEntityId) {

            var doc = docRepo.findById(docEntityId).get();

            var data = storageService.download(doc.getFilename());

            searchService.indexBlocking(data, doc.getFilename());

            // if we fail from here, we will just execute the indexing again

            doc.setIndexed(true);
            docRepo.save(doc);
        }
    }

}
