package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Objects;

public class RemoveData {

    public String username, targetUsername;


    public RemoveData() {

    }

    public RemoveData(String username, String targetUsername) {
        this.username = username;
        this.targetUsername = targetUsername;
    }

    public boolean validChange() { return username != null || targetUsername != null; }

    public boolean authorizeChange(String userRole, String targetRole) {
        if (userRole.equals("SU")) {
            return true;
        } else if (userRole.equals("GA") && (targetRole.equals("GBO") || targetRole.equals("USER"))) {
            return true;
        } else return userRole.equals("USER") && Objects.equals(username, targetUsername);
    }
}