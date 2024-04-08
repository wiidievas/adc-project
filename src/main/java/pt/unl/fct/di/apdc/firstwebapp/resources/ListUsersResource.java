package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/listusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersResource {

	 private final Gson g = new Gson();
	 private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	 private List<String> userLogs = new ArrayList<String>();
	
	 DatastoreOptions options = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build();
	 Datastore datastore = options.getService();
	 private KeyFactory userKeyFactory = datastore.newKeyFactory();
	
	 public ListUsersResource() { } //Nothing to be done here
	 
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response checkUsernameAvailable(ListUsersData data) {	
		AuthToken token = AuthToken.getMapValue(data.username);
		if(token == null || !token.isValid()) {
		
		Transaction txn = datastore.newTransaction();
		
		//n sei se chamar diretamente o username era o melhor
		
		Key userOneKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
	    Entity currentUser = txn.get(userOneKey);
	    
	    String userRole = (String) currentUser.getString("user_role");
	    
	    
		if(userRole.equals("USER")) {
			userList();
		}
		else if(userRole.equals("GBO")) {
			roleList("USER");
		} else if(userRole.equals("GA")) {
			roleList("USER");
			roleList("GBO");
			roleList("GA");
		}
		else { // tudo de todos
			roleList("USER");
			roleList("GBO");
			roleList("GA");
			roleList("SU");
		}
		
		
		return Response.ok(g.toJson(userLogs)).build();
		}
		else {
			AuthToken.removeMapValue(data.username);
			return Response.status(Status.FORBIDDEN).entity("User has no valid session.").build();

		}
		
	}

	//usr-UN,EM,NOME de contas ativas (e perfil publico)
	private void userList() {
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("User")
				.setFilter(CompositeFilter.and(
						PropertyFilter.eq("user_role", "USER"),
						PropertyFilter.eq("user_state", true)
						))
				.setLimit(3)
				.build();

		QueryResults<Entity> logs = datastore.run(query);
		//List<String> usernames = new ArrayList();
		
		logs.forEachRemaining(user -> {
			userLogs .add("User: " +user.getString("user_name"));
			userLogs .add("Email: " + user.getString("user_email"));
			userLogs .add("Name: " + user.getString("user_profile"));
			
		});
	}
	
	private void roleList(String role) {//users - todos os users, todos os atributos
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("User")
				.setFilter(
						PropertyFilter.eq("user_role", role)
						)
				.build();
		
		QueryResults<Entity> logs = datastore.run(query);
		
		logs.forEachRemaining(user -> {
			userLogs.add("User: " + user.getString("user_name"));
			userLogs.add("Email: " + user.getString("user_email"));
			userLogs .add("Name: " + user.getString("user_profile"));
			userLogs.add("Phone: " + (String) user.getString("user_phone"));
			userLogs .add("Hashed Pw: " + user.getString("user_pwd"));
			userLogs.add("Role: " + user.getString("user_role"));
			userLogs.add( "Active: " +user.getBoolean("user_state"));
			userLogs.add("Public account?: " + user.getBoolean("is_Public"));
			userLogs.add( "Occupation: " +user.getBoolean("occupation"));
			userLogs.add( "Workplace: " +user.getBoolean("workplace"));
			userLogs.add( "Address: " +user.getBoolean("address"));
			userLogs.add( "Postal Code: " +user.getBoolean("postal_Code"));
			userLogs.add( "NIF: " +user.getBoolean("NIF"));			
		});
	}
	
}
