package s.docentity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataService {

    @Inject
    DocEntityRepository repo;

    public DocEntity handleUpload(String fileName) {
        var de = new DocEntity();
        de.setFilename(fileName);
        return repo.save(de);
    }

}
