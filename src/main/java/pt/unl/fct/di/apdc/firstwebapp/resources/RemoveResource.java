package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
//import com.google.gson.Gson;
import pt.unl.fct.di.apdc.firstwebapp.util.RemoveData;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;
import javax.ws.rs.core.Response.Status;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {

    //private final Gson g = new Gson();
    
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build().getService();

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(RemoveData data) {
        LOG.fine("Removal of user: " + data.targetUsername + " - attempted by user: " + data.username);

        AuthToken token = AuthToken.getMapValue(data.username);
		if(token == null || !token.isValid()) {
			return Response.status(Status.FORBIDDEN).
					entity("You don't have a valid session. Please log in again").build();
		}
        
        
        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
            Entity target = txn.get(targetKey);
            if (user == null || target == null) {
                txn.rollback();
                return Response.status(Status.CONFLICT).entity("User doesn't exists.").build();

            } else if (!data.authorizeChange(user.getString("user_role"), target.getString("user_role"))) {
                txn.rollback();
                return Response.status(Status.UNAUTHORIZED).entity("User doesn't have permission.").build();

            } else {

                txn.delete(targetKey);
                txn.commit();

                return Response.ok("User Removed with Success").build();
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