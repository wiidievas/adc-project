package pt.unl.fct.di.apdc.firstwebapp.resources;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ListUsersData;
import pt.unl.fct.di.apdc.firstwebapp.util.LogoutData;

import com.google.cloud.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	 private final Gson g = new Gson();
	 private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	 
	 public LogoutResource() { } //Nothing to be done here
	 
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logOut(LogoutData data) {
		AuthToken token = AuthToken.getMapValue(data.username);
		if(token == null || !token.isValid()) {
			
			AuthToken.removeMapValue(data.username);
			return Response.ok().build();
		}
		else {
			return Response.status(Status.FORBIDDEN).entity("User has no valid session.").build();
		}
		/*AuthToken authToken = AuthToken.getInstance();
		if(authToken.username != null) {
			authToken.nullToken();
			return Response.ok("Sucessfully logged out.").build();
		}
		
		else {
			return Response.ok().entity("You do not have a session.").build();
			}*/
        //Map<String, Cookie> cookies = headers.getCookies();
		//Cookie authTokenCookie = cookies.get("root");
		//if(authTokenCookie != null ) {
			//String authTokenJson = authTokenCookie.getValue();
            // Now you have the authToken JSON string, you can deserialize it if needed
            // Example: Deserialize the JSON string to an AuthToken object
            //7AuthToken authToken = g.fromJson(authTokenJson, AuthToken.class);
			//cookies.remove(authTokenCookie);
			
			
			//return Response.ok("User successfully logged out.").build();
		}

		//else 
		//return Response.status(Status.FORBIDDEN).build();
		//return Response.ok().build();
	}

