package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private final Gson g = new Gson();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.newBuilder().setProjectId("adc-project-419115").build().getService();
	private final KeyFactory userKeyFactory = datastore.newKeyFactory();

	public LoginResource() { } //Nothing to be done here

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data,
							 @Context HttpServletRequest request,
							 @Context HttpHeaders headers) {
		LOG.fine("Attempt to login user: " + data.username);

		//Keys should be generated outside transactions
		//construct the key form the username
		Key userKey = userKeyFactory.setKind("User").newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("User", data.username))
				.setKind("UserStats").newKey("counters");

		//generate auto a key
		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("UserLog").newKey()
		);

		Transaction txn = datastore.newTransaction();

		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				return Response.status(Status.FORBIDDEN).entity("Failed login attempt for username: " + data.username).build();
			}

			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L) //cast para long
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}

			String hashedPWD = user.getString("user_pwd");
			
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				if(user.getBoolean("user_state")) {
				Entity log = Entity.newBuilder(logKey)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon",
								//does not index this property value
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
										.setExcludeFromIndexes(true).build()
						)
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now())
						.build();
				//get the user statistics and updates it
				//copying information every time a user logins maybe is not a good solution

				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 0L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now())
						.build();

				//batch operation
				txn.put(log, ustats);
				txn.commit();

				//return token
				AuthToken authToken = new AuthToken(data.username);
				//authToken.generateToken(data.username);
				
				LOG.info("User '" + data.username + "' logged in successfully.");
				
				//return Response.ok(g.toJson(authToken)).build();
				return Response.ok(g.toJson(authToken))
		                .build();
				} else {
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity("User " + data.username + " doesnt have an active account.").build();
				}
			} else {
				//copying here is even worse - find better sol (eventually)
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
						.set("user_last_attempt", Timestamp.now())
						.build();
				txn.put(ustats);
				txn.commit();

				return Response.status(Status.FORBIDDEN).entity("Wrong password for username: " + data.username).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) { txn.rollback(); }
		}
    }
}