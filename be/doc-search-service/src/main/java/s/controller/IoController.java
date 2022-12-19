package s.controller;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import lombok.AllArgsConstructor;
import lombok.Data;
import s.UploadService;
import s.blobstorage.StorageService;
import s.docentity.DocEntityRepository;

@Path("/")
@ApplicationScoped
public class IoController {

    @Inject
    UploadService uploadService;

    @Inject
    StorageService storageService;

    @Inject
    DocEntityRepository repository;

    @GET
    @Path("/download")
    @Produces("application/pdf")
    public Response download(@QueryParam("filename") String filename) {
        if (!repository.existsById(filename)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        var download = storageService.download(filename);
        // https://stackoverflow.com/questions/6293893/how-do-i-force-files-to-open-in-the-browser-instead-of-downloading-pdf
        return Response.ok().header("Content-Disposition", "inline").header("filename", filename)
                .entity(download)
                .build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@MultipartForm FileUploadBody body) {

        if (repository.existsById(body.name)) {
            return Response.status(Status.CONFLICT).build();
        }

        String id = uploadService.handleUpload(body.file, body.name);

        FileUploadResult result = new FileUploadResult(id);

        return Response.ok().entity(result).build();
    }

    @Data
    @AllArgsConstructor
    static class FileUploadResult {
        private String id;
    }

    public static class FileUploadBody {

        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public InputStream file;

        @FormParam("fileName")
        @PartType(MediaType.TEXT_PLAIN)
        public String name;

    }

}
