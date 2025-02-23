package at.htlhl.geo_tracking_solution.model;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;

public enum Role {
    Member("member"),
    Squadmaster("squadmaster"),
    Groupmaster("groupmaster");

    private String value;

    private Role(String value) {
        this.value = value;
    }

    public static List<String> getAll() {
        return List.of(Role.Member.toString(), Role.Squadmaster.toString(), Role.Groupmaster.toString());
    }

    public List<String> getAsList(){
        return List.of(value);
    }

    public static List<RoleRepresentation> getAsRoleList(List<String> roleNames, Keycloak keycloak, String realm) {
        return keycloak.realm(realm).roles().list().stream().filter(role -> roleNames.contains(role.getName())).toList();
    }

    @Override
    public String toString() {
        return value;
    }
}