package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Locale;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;

import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AttributesChangeData;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


@Path("/changeattributes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeAttributesResource {

    private final Gson g = new Gson();

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    DatastoreOptions options = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build();
    Datastore datastore = options.getService();

    //trocar qualquer atributo -> post, alteramos a DB
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttributes (AttributesChangeData data) {
        LOG.fine("State change of user: " + data.targetID + " - attempted by user: " + data.userID);
        
        AuthToken token = AuthToken.getMapValue(data.userID);
		if(token == null || !token.isValid()) {
			return Response.status(Status.FORBIDDEN).
					entity("You don't have a valid session. Please log in again").build();
		}

        if (!data.validAttributesChange()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.userID);
            Entity user = txn.get(userKey);

            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.targetID);
            Entity target = txn.get(targetKey);

            if (user == null || target == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Wrong user IDs.").build();

            } else if (!data.authorizeAttributesChange(user.getString("user_role"), target.getString("user_Role"))) {
                txn.rollback();
                return Response.status(Response.Status.UNAUTHORIZED).entity("User doesn't have permission.").build();
            } else {
                Entity.Builder newTarget = Entity.newBuilder(target);
                if (!user.getString("user_role").equals("USER")) {
                    if (!data.email.isEmpty()) {
                        newTarget.set("user_email", data.email);
                    }
                    if (!data.userName.isEmpty()) {
                        newTarget.set("user_profile", data.userName);
                    }
                    if (!data.role.isEmpty()) {
                        newTarget.set("user_role", data.role);
                    }
                    if (!data.state.isEmpty()) {
                        newTarget.set("user_state", data.state.equals("true"));
                    }
                }
                
                if (!data.phoneNumber.isEmpty()) {
                    newTarget.set("user_phone", data.phoneNumber);
                }
                if (!data.password.isEmpty()) {
                    newTarget.set("user_pwd", DigestUtils.sha512Hex(data.password));
                }
                if (!data.isPublic.isEmpty()) {
                    newTarget.set("is_Public", data.isPublic.equals("true"));
                }
                if (!data.occupation.isEmpty()) {
                    newTarget.set("occupation", data.occupation);
                }
                if (!data.workplace.isEmpty()) {
                    newTarget.set("workplace", data.workplace);
                }
                if (!data.address.isEmpty()) {
                    newTarget.set("address", data.address);
                }
                if (!data.postalCode.isEmpty()) {
                    newTarget.set("postal_Code", data.postalCode);
                }
                if (!data.NIF.isEmpty()) {
                    newTarget.set("NIF", data.NIF);
                }

                txn.put(newTarget.build());
                txn.commit();

                LOG.info("Successfully changed attributes of " + data.targetID);
                return Response.ok("Successfully updated user").build();
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