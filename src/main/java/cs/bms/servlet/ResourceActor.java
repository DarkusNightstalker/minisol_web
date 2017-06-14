/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.servlet;

import cs.bms.service.interfac.IActorService;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Jhoan Brayam
 */
@Path("actor")
@javax.enterprise.context.RequestScoped
public class ResourceActor {

    @Autowired
    @Qualifier("actorService")
    protected IActorService actorService;

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject search(@QueryParam("q") String q, @QueryParam("page") int page, @QueryParam("d") Short digitsIdentity) {
        final JsonArrayBuilder builder = Json.createArrayBuilder();
        if (q.length() == 0) {
            return Json.createObjectBuilder().add("items", builder).add("total_count", 0L).build();
        }
        q = "%" + q + "%";

        List<Object[]> data = null;
        Long count = null;
        if (digitsIdentity == null || digitsIdentity == 0) {
            data = actorService.listHQLPage("SELECT "
                    + "a.id,"
                    + "a.name,"
                    + "a.identityDocument.abbr,"
                    + "a.identityNumber "
                    + "FROM Actor a WHERE LOWER(a.name) LIKE LOWER(?) OR a.identityNumber LIKE ? ", page, 6, q, q);
            count = (Long) actorService.getByHQL("SELECT COUNT(a.id) FROM Actor a WHERE LOWER(a.name) LIKE LOWER(?) OR a.identityNumber LIKE ? ", q, q);
        } else {
            data = actorService.listHQLPage("SELECT "
                    + "a.id,"
                    + "a.name,"
                    + "a.identityDocument.abbr,"
                    + "a.identityNumber "
                    + "FROM Actor a WHERE (LOWER(a.name) LIKE LOWER(?) OR a.identityNumber LIKE ?) AND a.identityDocument.length_ = ? ", page, 6, q, q, digitsIdentity);
            count = (Long) actorService.getByHQL("SELECT COUNT(a.id) FROM Actor a WHERE (LOWER(a.name) LIKE LOWER(?) OR a.identityNumber LIKE ?) AND a.identityDocument.length_ = ? ", q, q, digitsIdentity);
        }

        data.forEach(item -> {
            builder.add(Json.createObjectBuilder()
                    .add("id", (Long) item[0])
                    .add("name", (String) item[1])
                    .add("identityDocument", (String) item[2])
                    .add("identityNumber", (String) item[3]).build());
        });
        return Json.createObjectBuilder().add("total_count", count).add("items", builder).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject searchById(@PathParam("id") Long id) {
        Object[] data = (Object[]) actorService.getByHQL("SELECT "
                + "a.id,"
                + "a.name,"
                + "a.identityDocument.abbr,"
                + "a.identityNumber "
                + "FROM Actor a WHERE a.id = ? ", id);
        if (data == null) {
            return null;
        }
        return Json.createObjectBuilder()
                .add("id", (Long) data[0])
                .add("name", (String) data[1])
                .add("identityDocument", (String) data[2])
                .add("identityNumber", (String) data[3]).build();

    }
}
