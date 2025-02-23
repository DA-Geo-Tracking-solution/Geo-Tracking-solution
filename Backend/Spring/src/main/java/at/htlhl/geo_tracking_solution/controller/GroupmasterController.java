package at.htlhl.geo_tracking_solution.controller;

import io.swagger.v3.oas.annotations.Operation;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import at.htlhl.geo_tracking_solution.model.Role;
import at.htlhl.geo_tracking_solution.model.keycloak.Group;
import at.htlhl.geo_tracking_solution.model.keycloak.User;
import at.htlhl.geo_tracking_solution.service.GroupService;
import at.htlhl.geo_tracking_solution.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/groupmaster")
public class GroupmasterController {

    private GroupService groupService;
    private UserService userService;

    @Autowired
    public GroupmasterController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @PostMapping("/user")
    @Operation(description = "Creates new user in format { {username, userEmail, firstname, lastname}, temporaryPassword } in group")
    public String createUser(@RequestBody Map<String, Object> request) {
        
        User userModel = User.MapToUser((Map<String, Object>) request.get("user"));
        String temporaryPassword = (String) request.get("temporaryPassword");

        return "{ \"message\": \"" + userService.createUser(userModel.getUserRepresentation(temporaryPassword)) + "\" }";
    }

    @PostMapping("/subgroup")
    @Operation(description = "Creates new subrgoup in format { name, groupmasterEmail, [ memberEmails ] } and adds a groupmaster")
    public String createSubGroup(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String groupmasterEmail = (String) request.get("groupmasterEmail");
        Object memberObject = request.get("memberEmails");
        List<String> memberEmails = new ArrayList<>();
        try {
            if (memberObject != null && memberObject instanceof List) {
                memberEmails = (List<String>) memberObject;
            }
            List<UserRepresentation> groupmasterList = groupService.getGroupMembers().stream()
                .filter(user -> user.getEmail().equals(groupmasterEmail))
                .collect(Collectors.toList());

            if (groupmasterList.isEmpty()) {
                throw new Exception("User with email " + groupmasterEmail + " in this group not found.");
            }
            UserRepresentation groupmaster = groupmasterList.get(0);

        
            GroupRepresentation createdGroup = groupService.createSubGroupWithRoles(name);
            groupService.addUserToGroupWithRoles(groupmaster, createdGroup, Role.getAll());
            for (String memberEmail: memberEmails) {
                groupService.addUserToGroupByUseremail(memberEmail, createdGroup);
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Succesful created";
    }

    @DeleteMapping("/user/{userEmail}")
    @Operation(description = "Deletes user by { userEmail } in group")
    public String deleteUser(@PathVariable("userEmail") String userEmail) {
        return "{ \"message\": \"" + userService.deleteUserByEmail(userEmail) + "\" }";
    }

    @GetMapping("/subgroups")
    @Operation(description = "optionally")
    public Object getSubGroups() {
        return "get all subgroups as a tree (not implemented)";
    }
}
