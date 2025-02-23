package at.htlhl.geo_tracking_solution.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import at.htlhl.geo_tracking_solution.model.Role;


@Service
public class GroupService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    //public static final List<String> ALL_ROLE_NAMES = Arrays.asList("member", "squadmaster", "groupmaster");
    

    public UserResource addUserToGroup(UserRepresentation user, GroupRepresentation group) throws Exception {
        RealmResource realmResource = keycloak.realm(realm);

        UserResource userResource = realmResource.users().get(user.getId());
        List<GroupRepresentation> currentGroups = userResource.groups();
        for (GroupRepresentation currentGroup : currentGroups) {
            userResource.leaveGroup(currentGroup.getId());
            System.out.println("User removed from group: " + group.getName());
        }
        userResource.joinGroup(group.getId());

        return userResource;
    }

    public UserResource addUserToGroupByUseremail(String userEmail, GroupRepresentation group) throws Exception {
        List<UserRepresentation> users = getGroupMembers().stream()
        .filter(user -> user.getEmail().equals(userEmail))
        .collect(Collectors.toList());
        if (users.isEmpty()) {
            throw new Exception("User with email " + userEmail + " in this group not found.");
        }

        return addUserToGroup(users.get(0), group);
    }

    public void addUserToGroupWithRoles(UserRepresentation user, GroupRepresentation group, List<String> roleNames) throws Exception {
        UserResource userResource = addUserToGroup(user, group);

        // Assign roles to the user
        List<RoleRepresentation> roles = rolesByRoleNames(roleNames);
        userResource.roles().realmLevel().add(roles);
    }

     public GroupRepresentation createSubGroupWithRoles(String groupName) throws Exception {
        GroupsResource groupsResource = keycloak.realm(realm).groups();

        // Find the parent group
        List<GroupRepresentation> groups = getUserGroups();
        if (groups.isEmpty()) {
            throw new Exception("Invalid Request! User is in no group! >8[)");
        }
        GroupRepresentation parentGroup = groupsResource.groups().stream()
            .filter(group -> group.getName().equals(groups.get(0).getName())).findFirst()
            .orElseThrow(() -> new Exception("Parent group does not exist!"));


        // Create a new subgroup
        GroupResource parentGroupResource = groupsResource.group(parentGroup.getId());
        if (parentGroupResource.getSubGroups(0, null, true).stream().anyMatch(subGroup -> subGroup.getName().equals(groupName))) {
            throw new Exception("Subgroup already exists!");
        }
        GroupRepresentation newSubGroup = new GroupRepresentation();
        newSubGroup.setName(groupName);
        parentGroupResource.subGroup(newSubGroup);

        // Add roles to subgroup
        GroupRepresentation createdSubGroup = parentGroupResource.getSubGroups(0, null, true) // if more information needed change true to false
            .stream().filter(subGroup -> subGroup.getName().equals(groupName)).findFirst().orElse(null);
        List<RoleRepresentation> roles = rolesByRoleNames(Role.getAll());
        groupsResource.group(createdSubGroup.getId()).roles().realmLevel().add(roles);

        return createdSubGroup;
    }

    public List<GroupRepresentation> getSubGroupsOf(GroupRepresentation parentGroup) {
            return parentGroup.getSubGroups();
    }

    public List<UserRepresentation> getGroupMembers() {
        List<GroupRepresentation> groups = getUserGroups();

        if (!groups.isEmpty()) {
            String groupId = groups.get(0).getId();
            return keycloak.realm(realm).groups().group(groupId).members();
        }
        return List.of();
    }

    private List<RoleRepresentation> rolesByRoleNames(List<String> roleNames) {
        return keycloak.realm(realm).roles().list().stream()
                .filter(role -> roleNames.contains(role.getName()))
                .toList();
    }

    private GroupRepresentation getGroupFromName(String groupName) throws Exception{
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups(groupName, 0, 1);
        if (groups.isEmpty()) {
            throw new Exception("Group with name " + groupName + " not found.");
        }
        return groups.get(0);
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get(StandardClaimNames.SUB);
    }

    private List<GroupRepresentation> getUserGroups() {
        return keycloak.realm(realm).users().get(getUserId()).groups();
    }
}
