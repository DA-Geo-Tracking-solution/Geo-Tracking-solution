package at.htlhl.geo_tracking_solution.model;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Chat {
    private UUID chatId;
    private String chatName;
    private List<Member> members;

    public Chat (UUID chatId, String chatName, Member[] members ) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.members = Arrays.asList(members);
    }


    public Chat (UUID chatId, String chatName, List<Member> members ) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.members = members;
    }

    public UUID getChatId() {
        return this.chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return this.chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
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
