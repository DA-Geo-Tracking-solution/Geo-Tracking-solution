package at.htlhl.geo_tracking_solution.model;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SquadData {
    private UUID squadId;
    private List<Member> members;

    public SquadData (UUID squadId, Member[] members ) {
        this.squadId = squadId;
        this.members = Arrays.asList(members);
    }


    public SquadData (UUID squadId, List<Member> members ) {
        this.squadId = squadId;
        this.members = members;
    }

    public UUID getSquadId() {
        return this.squadId;
    }

    public void setSquadId(UUID chatId) {
        this.squadId = chatId;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }


    public static class Member {
        private String name;
        private String email;

        public Member(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}