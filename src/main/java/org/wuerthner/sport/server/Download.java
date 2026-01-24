package org.wuerthner.sport.server;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.wuerthner.sport.persistence.dao.GenericDao;

import java.io.File;

@Path("/download")
public class Download {
    @Inject
    public GenericDao dao;

    @GET
    public Response download(@QueryParam("login") String login) {
        if (!isValidToken(login)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        File file = new File("/opt/download/probenmaterial.zip");
        Response build;
        if (!file.exists()) {
            build = Response.status(Response.Status.NOT_FOUND)
                    .entity("<script>alert('Kein Material vorhanden!');</script>")
                    .type("text/html")
                    .build();
        } else {
            build = Response.ok(file)
                    .header("Content-Disposition",
                            "attachment; filename=\"probenmaterial.zip\"")
                    .build();
        }
        return build;
    }

    private boolean isValidToken(String login) {
        String value = dao.getUserIdByUUID(login);
        System.out.println("Download login: " + value);
        return !(value==null || value.isBlank() || value.contains("unknown"));
    }
}

