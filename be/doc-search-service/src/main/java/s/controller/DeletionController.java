package s.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

import s.blobstorage.StorageService;
import s.docentity.DocEntityRepository;
import s.search.ElasticSearchService;

// currently only for test cleanup
@ApplicationScoped
@Path("/delete")
public class DeletionController {

    @Inject
    ElasticSearchService searchService;

    @Inject
    DocEntityRepository repository;

    @Inject
    StorageService storageService;

    @DELETE
    public void deleteAllDocs() {
        searchService.deleteAllDocs();
        repository.deleteAllInBatch();
        storageService.deleteAllObjects();
    }
}
