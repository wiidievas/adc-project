package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeStateData {

    public String usernameOne, usernameTwo;
    public boolean state;


    public ChangeStateData() {

    }

    public ChangeStateData(String user1, String user2, boolean state) {
        this.usernameOne = user1;
        this.usernameTwo = user2;
        this.state = state;

    }

    public boolean authorizeStateChange(String userOneRole, String userTwoRole) {
        if(userOneRole.toUpperCase().equals("SU")) return true;
        else if(userOneRole.equals("GA")) {
            if(userTwoRole.toUpperCase().equals("GBO")) {
                return true;
            }
            if(userTwoRole.toUpperCase().equals("USER")) {
                return true;
            }
            else return false;
        }
        else if(userOneRole.equals("GBO")){
                if(userTwoRole.toUpperCase().equals("USER")) {
                    return true;
                }
                else return false;

        }else {
            return false;
        }
    }
}