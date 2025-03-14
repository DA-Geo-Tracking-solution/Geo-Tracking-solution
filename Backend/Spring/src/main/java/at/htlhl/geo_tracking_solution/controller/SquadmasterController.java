package at.htlhl.geo_tracking_solution.controller;

import io.swagger.v3.oas.annotations.Operation;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import at.htlhl.geo_tracking_solution.model.cassandra.GPSData;
import at.htlhl.geo_tracking_solution.service.GPSDataService;
import at.htlhl.geo_tracking_solution.service.SquadService;
import at.htlhl.geo_tracking_solution.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/squadmaster")
public class SquadmasterController {

    private SquadService squadService;
    private GPSDataService gpsDataService;
    private UserService userService;

    @Autowired
    public SquadmasterController(SquadService squadService, GPSDataService gpsDataService, UserService userService) {
        this.squadService = squadService;
        this.userService = userService;
        this.gpsDataService = gpsDataService;
    }

    @PostMapping("/squad")
    @Operation(description = "Creates a squad with an array of user emails")
    public ResponseEntity<UUID> createSquad(@RequestBody List<String> userEmails) {
        String squadmasterEmail = userService.getUserEmail();
        if (!userEmails.contains(squadmasterEmail)) {
            userEmails.add(squadmasterEmail);
        }

        try {
            UUID squadId = squadService.createSquad(userEmails);
            return ResponseEntity.status(HttpStatus.CREATED).body(squadId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create squad", e);
        }
    }

    @PostMapping("/{squadId}/users")
    @Operation(description = "Adds a user to a squad. The userEmail is provided in the request body.")
    public ResponseEntity<Void> addUserToSquad(
            @PathVariable("squadId") UUID squadId,
            @RequestBody String userEmail) {

        if (!userService.isMember(userEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not in Group");
        }

        String squadmasterEmail = userService.getUserEmail();
        if (!squadService.isUserInSquad(squadId, squadmasterEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not in this Squad");
        }

        try {  
            squadService.putUserInSquad(userEmail, squadId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add user to squad" + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{squadId}/users/{userEmail}")
    @Operation(description = "Removes a user from a squad. The userEmail is specified in the path.")
    public ResponseEntity<Void> removeUserFromSquad(
            @PathVariable("squadId") UUID squadId,
            @PathVariable("userEmail") String userEmail) {
        
        if (!userService.isMember(userEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not in Group");
        }

        String squadmasterEmail = userService.getUserEmail();
        if (!squadService.isUserInSquad(squadId, squadmasterEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not in this Squad");
        }

        if (squadmasterEmail.equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't remove yourself from this Squad");
        }

        try {
            squadService.removeUserFromSquad(userEmail, squadId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to remove user from squad", e);
        }
    }

    @GetMapping("/group-members-locations")
    @Operation(description = "Returns some users location data in format { userEmail, { longitude, latitude }, timestamp } of your group")
    public List<GPSData> getGroupmembersCoordinates(@RequestParam Instant earliestTime) {
        List<GPSData> userLocations = new ArrayList<>();
        for (UserRepresentation user : userService.getGroupMembers()) {
            userLocations.addAll(gpsDataService.getGPSDataOf(user.getEmail(), earliestTime));
        }
        return userLocations;
    }
}
