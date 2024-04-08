package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String username, password, email, profile, phone, 
	isPublic, occupation, workplace, address, postalCode, NIF;
	
	public static int PASSWORD_LENGTH = 8;
	
	//para ser instanciada automatica/ quando se recebe JSON do cliente
	public RegisterData() { 
		
	}
	
	public RegisterData(String username, String email, 
			String profile, String phone, String password, 
			String isPublic, String occupation, String workplace, 
            String address, String postalCode, String NIF) {
		this.username = username;
		this.email = email;
		this.profile = profile;
		this.phone = phone;
		this.password = password;
		this.isPublic = isPublic;
        this.occupation = occupation;
        this.workplace = workplace;
        this.address = address;
        this.postalCode = postalCode;
        this.NIF = NIF;
	}

	//estar na classe porque pode vir a ser usada varias vezes; ver complex do username, password, e email
	public boolean validRegistration() {
		//return verifyPassword() && verifyEmail();
		//return true;
		return username != null && password != null && email != null && profile != null;
	}
	
	//if len > min length, ret true
	boolean validLength() {
		return !(password.length() < PASSWORD_LENGTH);
	}
	
	//https://www.asciitable.com/
	boolean isNumber(char c) {
		return (c >= '0' && c <= '9');
	}
	
	boolean isSpecial(char c) {
		return (c >= '!' && c <= '/') || (c == '@');
	}
	
	//https://www.w3resource.com/java-exercises/method/java-method-exercise-11.php
	boolean validCharacters() {
		int numChars = 0;
		int specialChars = 0;
		for(int i = 0; i < password.length(); i++) {
			
			char c = password.charAt(i);
			if(isNumber(c)) numChars++;
			if(isSpecial(c)) specialChars++;
			
		}
		
		return (numChars >= 1 && specialChars >= 1);
		
	}
	
	// tem caracteres necessarios e length necessaria
	boolean verifyPassword() {
		return validLength() && validCharacters();
	}
	
	//check if its a valid domain.. necessary?
	boolean verifyDomain(String s) {
		
		return true;
	}
	
	boolean verifyEmail() {
		//separate email into email "name", "@" and "domain.x", string length should be 3 for valid emails
		String[] splitMail = email.split("(?<=@)|(?=@)");
		
		return (splitMail.length == 3 && verifyDomain(splitMail[2]));
	}
}
