package at.htlhl.geo_tracking_solution.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.htlhl.geo_tracking_solution.model.cassandra.UserBySquad;
import at.htlhl.geo_tracking_solution.model.cassandra.UserBySquad.UserBySquadKey;
import at.htlhl.geo_tracking_solution.repositories.UserBySquadRepository;

@Service
public class SquadService {

    private final UserService userService;
    private final UserBySquadRepository squadRepository;

    @Autowired
    public SquadService(UserService userService, UserBySquadRepository squadRepository) {
        this.userService = userService;
        this.squadRepository = squadRepository;
    }
    
    public List<UserBySquad> getSquadsFromUser(String userEmail) {
        return squadRepository.getSquads(userEmail);
    }

    public List<UserBySquad> getUsersInSquad(UUID squadId) {
        return squadRepository.getUsers(squadId);
    }


    public boolean isUserInSquad(UUID squadId, String userEmail) {
        return squadRepository.isUserInSquad(squadId, userEmail).size() == 1;
    }

    public UUID createSquad(List<String> userEmails) throws Exception{
        UUID squadId = UUID.randomUUID();
        try {
            // Inserts if uuid not exists else throws error
            if (squadRepository.doesSquadIdExist(squadId).isPresent()) {
                throw new Exception("Squad does not exist");
            }

            for (String userEmail: userEmails) {
                if (userService.isMember(userEmail)) {
                    squadRepository.insertIfNotExists(squadId, userEmail);
                }    
            }
            return squadId;
        } catch (Exception e) {
            // Undo changes if one fails
            for (String userEmail: userEmails) {
                revertUsersBySquad(userEmail, squadId);
            }
            throw new Exception("Failed to update chat associations, changes rolled back." + e.getMessage());
        }
    }

    public void putUserInSquad(String userEmail, UUID squadId) throws Exception{
        
        if (!squadRepository.doesSquadIdExist(squadId).isPresent()) {
            throw new Exception();
        }
        try {
            // Inserts only if uuid exist else throws error

            squadRepository.insertIfNotExists(squadId, userEmail);

        } catch (Exception e) {
            // Undo changes if one fails
            revertUsersBySquad(userEmail, squadId);
            throw new Exception("Failed to update chat associations, changes rolled back.");
        }
        
    }

    private void revertUsersBySquad(String userEmail, UUID squadId) {
        UserBySquadKey userBySquadKey = new UserBySquadKey(squadId, userEmail);
        squadRepository.deleteById(userBySquadKey);
    }

    public void removeUserFromSquad(String userEmail, UUID squadId) throws Exception {
        try {
            UserBySquadKey key = new UserBySquadKey(squadId, userEmail);
            squadRepository.deleteById(key);
        } catch (Exception e) {
            throw new Exception("Failed to remove user from squad", e);
        }
    }

}