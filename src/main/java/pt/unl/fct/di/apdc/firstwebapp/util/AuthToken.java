package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthToken {
	
	public static final long EXPIRATION_TIME = 1000*60*60; //1h
	
	public String username, tokenID;
	public long creationData, expirationData;
	private static Map<String, AuthToken> authTokenMap = new HashMap<>();
	
    //private static AuthToken instance;
	
	public AuthToken() {
		
	}
	
	public AuthToken(String username) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
		authTokenMap.put(username, this);
	}
	
	public boolean isValid() {
		return System.currentTimeMillis()<expirationData;
	}
	
	 public static AuthToken getMapValue(String tokenUsername) {
	        return authTokenMap.get(tokenUsername);
	    }
	 
	 public static void removeMapValue(String tokenUsername) {
		 authTokenMap.remove(tokenUsername);
	 }
	
   /* public static AuthToken getInstance() {
        if (instance == null) {
            instance = new AuthToken();
        }
        return instance;
    }*/

    // Public method to generate a token
   /* public void generateToken(String username) {
        this.username = username;
        this.tokenID = UUID.randomUUID().toString();
        this.creationData = System.currentTimeMillis();
        this.expirationData = this.creationData + EXPIRATION_TIME;
    }
    
    public void nullToken() {
    	instance = null;
    	this.username = null;
        this.tokenID = null;
        this.creationData = 0L;
        this.expirationData = 0L;
    }*/
	

}
