package s;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import s.blobstorage.StorageService;
import s.docentity.DataService;
import s.search.AsyncIndexingService;

@ApplicationScoped
public class UploadService {

    @Inject
    StorageService storageService;

    @Inject
    DataService dataService;

    @Inject
    AsyncIndexingService indexingService;

    @Transactional
    public String handleUpload(InputStream inputStream, String fileName) {
        try {
            var bytes = inputStream.readAllBytes();
            storageService.upload(new ByteArrayInputStream(bytes), fileName);
            // todo: when we break somewhere from here the data has already been stored in
            // the storage -> cyclic cleanup batch to check for orphan storage entries
            // necessary
            var dataEntity = dataService.handleUpload(fileName);
            indexingService.indexAsync(dataEntity);
            return dataEntity.getFilename();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
