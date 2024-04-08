package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Entity;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;



@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

    private final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    DatastoreOptions options = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build();
    Datastore datastore = options.getService();

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegister(RegisterData data) {
        LOG.fine("Register attempt by user: " + data.username);

        if (!data.validRegistration()) {
            return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            if(user != null) {
                txn.rollback();
                return Response.status(Status.CONFLICT).entity("User already exists.").build();
            } else {
                user = Entity.newBuilder(userKey)
                        .set("user_name", data.username)
                        .set("user_email", data.email)
                        .set("user_profile", data.profile)
                        .set("user_phone", data.phone)
                        .set("user_pwd", DigestUtils.sha512Hex(data.password))
                        .set("user_role", "USER")
                        .set("user_state", false)
                        .set("is_Public", data.isPublic.equals("on"))
                        .set("occupation", data.occupation)
                        .set("workplace", data.workplace)
                        .set("address", data.address)
                        .set("postal_Code", data.postalCode)
                        .set("NIF", data.NIF)
                        .build();
                txn.add(user);
                txn.commit();
                return Response.ok("Success").build();
            }
        } finally {
            if(txn.isActive()) { txn.rollback(); }
        }
    }
}