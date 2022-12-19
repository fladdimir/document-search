package s.controller;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import s.search.ElasticSearchService;
import s.search.ElasticSearchService.DocSearchResult;

@ApplicationScoped
@Path("/search")
public class SearchController {

    @Inject
    ElasticSearchService searchService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocSearchResult> search(@QueryParam("for") String searchString) {
        return searchService.search(searchString);
    }

}
