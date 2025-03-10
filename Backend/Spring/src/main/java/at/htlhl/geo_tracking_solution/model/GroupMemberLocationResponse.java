package at.htlhl.geo_tracking_solution.model;

import java.time.Instant;
import java.util.Map;

public class GroupMemberLocationResponse {
    private String userId;
    private Map<String, Object> location;
    private Instant timestamp;

    public String getUserId() {
        return userId;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
