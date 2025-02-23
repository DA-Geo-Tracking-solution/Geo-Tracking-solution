package at.htlhl.geo_tracking_solution.model.keycloak;

import java.util.List;

public class Group {
    private User groupMaster;
    private List<User> squadMaster;
    private List<User> members;

    public User getGroupMaster() {
        return groupMaster;
    }

    public void setGroupMaster(User groupMaster) {
        this.groupMaster = groupMaster;
    }

    public List<User> getSquadMaster() {
        return squadMaster;
    }

    public void setSquadMaster(List<User> squadMaster) {
        this.squadMaster = squadMaster;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
