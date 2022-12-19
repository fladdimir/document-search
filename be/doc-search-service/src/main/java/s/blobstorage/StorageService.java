package s.blobstorage;

import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.quarkus.runtime.StartupEvent;
import s.util.TimeLoggingInterceptor.LogExecutionTime;
import s.util.TimeLoggingInterceptor.LogExecutionTimeParameter;

@ApplicationScoped
public class StorageService {

    private static final String DOCS = "docs";

    @Inject
    MinioClient minioClient;

    @Inject
    Logger logger;

    @LogExecutionTime
    public void upload(InputStream inputStream, @LogExecutionTimeParameter(name = "file") String fileName) {
        try {
            store(inputStream, fileName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @LogExecutionTime
    public byte[] download(@LogExecutionTimeParameter(name = "file") String fileName) {
        try {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(DOCS)
                            .object(fileName)
                            .build())) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void store(InputStream inputStream, String fileName) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(DOCS)
                        .object(fileName)
                        .stream(inputStream, -1l, 5368709120l)
                        .build());
    }

    void onStart(@Observes StartupEvent ev) {
        createBucketIfNotExists();
    }

    public void createBucketIfNotExists() {
        try {
            boolean bucketExists = bucketExists();
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(DOCS).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean bucketExists()
            throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(DOCS).build());
    }

    public void deleteBucketIfExists() {
        try {
            if (bucketExists()) {

                deleteAllObjects();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteAllObjects() {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(DOCS).build());

        List<DeleteObject> objects = StreamSupport.stream(results.spliterator(), false).map(r -> {
            try {
                return r.get().objectName();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).map(DeleteObject::new).toList();

        Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                RemoveObjectsArgs.builder().bucket(DOCS).objects(objects).build());

        for (Result<DeleteError> result : deleteResults) {
            DeleteError error;
            try {
                error = result.get();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            logger.error(
                    "Error when deleting object " + error.objectName() + "; " + error.message());
        }
    }

}
