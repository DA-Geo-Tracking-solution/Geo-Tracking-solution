package at.htlhl.geo_tracking_solution.service;

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
import org.springframework.stereotype.Service;

import at.htlhl.geo_tracking_solution.model.Role;


@Service
public class GroupService {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    
    private final UserService userService;

    @Autowired
    public GroupService(UserService userService) {
        this.userService = userService;
    }

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

        List<UserRepresentation> users = userService.getGroupMembers().stream()
        .filter(user -> user.getEmail().equals(userEmail))
        .collect(Collectors.toList());
        if (users.isEmpty()) {
            throw new Exception("User with email " + userEmail + " in this group not found.");
        }
        return addUserToGroup(users.get(0), group);
    }

    public void addUserToGroupWithRoles(UserRepresentation user, GroupRepresentation group, List<String> roleNames) throws Exception {
        UserResource userResource = addUserToGroup(user, group);
        List<RoleRepresentation> roles = rolesByRoleNames(roleNames);
        userResource.roles().realmLevel().add(roles);
    }

    public GroupRepresentation createSubGroupWithRoles(String groupName) throws Exception {
        GroupsResource groupsResource = keycloak.realm(realm).groups();

        // Find the parent group
        List<GroupRepresentation> groups = userService.getUserGroups();
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
        GroupRepresentation createdSubGroup = parentGroupResource.getSubGroups(0, null, true)
            .stream().filter(subGroup -> subGroup.getName().equals(groupName)).findFirst().orElse(null);
        List<RoleRepresentation> roles = rolesByRoleNames(Role.getAll());
        groupsResource.group(createdSubGroup.getId()).roles().realmLevel().add(roles);

        return createdSubGroup;
    }

    private List<RoleRepresentation> rolesByRoleNames(List<String> roleNames) {
        return keycloak.realm(realm).roles().list().stream()
                .filter(role -> roleNames.contains(role.getName())).toList();
    }

    public String getGroupNameFromUser(String userEmail) throws Exception {
        // Search for the user by email
        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(null, null, null, userEmail, 0, 1);
        if (users.isEmpty()) {
            throw new Exception("User with email " + userEmail + " not found.");
        }
        UserRepresentation user = users.get(0);
    
        // Retrieves all groups of the user
        List<GroupRepresentation> userGroups = keycloak.realm(realm).users().get(user.getId()).groups();
    
        if (userGroups.isEmpty()) {
            throw new Exception("User with email " + userEmail + " is not in any group.");
        }

        return userGroups.get(0).getName();
    }

    public boolean isUserInGroup(String groupName, String userEmail) throws Exception {
        return getGroupNameFromUser(userEmail).equals(groupName);
    }

}
