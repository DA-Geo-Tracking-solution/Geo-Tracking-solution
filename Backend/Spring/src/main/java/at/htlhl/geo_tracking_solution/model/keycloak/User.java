package at.htlhl.geo_tracking_solution.model.keycloak;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

public class User {
    private String username;
    private String email;
    private String firstname;
    private String lastname;

    public User(String username, String email, String firstname, String lastname) {
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public User(UserRepresentation userRep) {
        this.username = userRep.getUsername();
        this.email = userRep.getEmail();
        this.firstname = userRep.getFirstName();
        this.lastname = userRep.getLastName();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public static User MapToUser(Map<String, Object> userMap ) {
        return new User(
            (String) userMap.get("username"),
            (String) userMap.get("userEmail"),
            (String) userMap.get("firstname"),
            (String) userMap.get("lastname")
        );
    }

    public UserRepresentation getUserRepresentation(String password) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(this.getUsername());
        userRepresentation.setEmail(this.getEmail());
        userRepresentation.setFirstName(this.getFirstname());
        userRepresentation.setLastName(this.getLastname());
        userRepresentation.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        userRepresentation.setCredentials(List.of(credential));
        return userRepresentation;
    }
}
