package pt.unl.fct.di.apdc.firstwebapp.util;

public class LogoutData {

	
public String username;
	
	//para ser instanciada automatica/ quando se recebe JSON do cliente
	public LogoutData() { 
		
	}
	
	public LogoutData(String username) {
		this.username = username;
	}
	
}
