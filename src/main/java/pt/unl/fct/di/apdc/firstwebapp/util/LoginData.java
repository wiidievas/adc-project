package pt.unl.fct.di.apdc.firstwebapp.util;

public class LoginData {
	
	public String username, password;
	
	//para ser instanciada automatica/ quando se recebe JSON do cliente
	public LoginData() { 
		
	}
	
	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
