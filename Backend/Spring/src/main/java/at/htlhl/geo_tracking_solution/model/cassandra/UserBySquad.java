package at.htlhl.geo_tracking_solution.model.cassandra;

import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;


import java.util.UUID;

@Table("users_by_squad")
public class UserBySquad {

    @PrimaryKey
    private UserBySquadKey key;

    public UserBySquad() { }

    public UserBySquad(UserBySquadKey key) {
        this.key = key;
    }    

    // Getters and setters
    public UserBySquadKey getKey() {
        return key;
    }

    public void setKey(UserBySquadKey key) {
        this.key = key;
    }

    @PrimaryKeyClass
    public static class UserBySquadKey {

        @PrimaryKeyColumn(name = "squad_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID squadId;

        @PrimaryKeyColumn(name = "user_email", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String userEmail;

        public UserBySquadKey() { }

        public UserBySquadKey(UUID squadId, String userEmail) {
            this.squadId = squadId;
            this.userEmail = userEmail;
        }        

        public UUID getSquadId() {
            return squadId;
        }

        public void setSquadId(UUID squadId) {
            this.squadId = squadId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }
    }

}
