package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;

import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.PasswordChangeData;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;
import javax.ws.rs.core.Response.Status;

@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {

    private final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build().getService();

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(PasswordChangeData data) {
        LOG.fine("Password change attempt by user: " + data.userID);
		AuthToken token = AuthToken.getMapValue(data.userID);
		if(token == null || !token.isValid()) {
			return Response.status(Status.FORBIDDEN).
					entity("You don't have a valid session. Please log in again").build();
		}
        if (!data.validPasswordChange()) {
            return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.userID);
            Entity user = txn.get(userKey);
            if (user == null) {
                txn.rollback();
                return Response.status(Status.CONFLICT).entity("User doesn't exists.").build();
            } else if (!user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.password1))) {
                txn.rollback();
                return Response.status(Status.UNAUTHORIZED).entity("Wrong password for username: " + data.userID).build();
            } else {
                Entity newUser = Entity.newBuilder(user)
                        .set("user_pwd", DigestUtils.sha512Hex(data.newPassword))
                        .build();

                txn.put(newUser);
                txn.commit();
                return Response.ok("Password Changed with Success").build();
            }
        } catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if(txn.isActive()) { txn.rollback(); }
        }
    }
}