package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ShowTokenData;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpSession;
@Path("/token")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowTokenResource {

    private final Gson g = new Gson();
	
	public ShowTokenResource() {
		
	}
	
	@GET
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAuthToken(ShowTokenData data) {
		AuthToken token = AuthToken.getMapValue(data.username);
		if(token == null || !token.isValid()) {
		//get authtoken of user "current session"
		//AuthToken authToken = AuthToken.getInstance();
		//return Response.ok(g.toJson(authToken)).build();
			return Response.ok(g.toJson(token)).build();
		}
		else {
			AuthToken.removeMapValue(data.username);
			return Response.status(Status.FORBIDDEN).entity("User has no valid session.").build();

		}
	}
	
}
