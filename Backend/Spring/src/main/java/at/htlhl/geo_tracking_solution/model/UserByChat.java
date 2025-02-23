package at.htlhl.geo_tracking_solution.model;

import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;


import java.util.UUID;

@Table("users_by_chat")
public class UserByChat {

    @PrimaryKey
    private UserByChatKey key;

    public UserByChat() { }

    public UserByChat(UserByChatKey key) {
        this.key = key;
    }    

    // Getters and setters
    public UserByChatKey getKey() {
        return key;
    }

    public void setKey(UserByChatKey key) {
        this.key = key;
    }

    @PrimaryKeyClass
    public static class UserByChatKey {

        @PrimaryKeyColumn(name = "chat_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID chatId;

        @PrimaryKeyColumn(name = "user_email", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String userEmail;

        public UserByChatKey() { }

        public UserByChatKey(UUID chatId, String userEmail) {
            this.chatId = chatId;
            this.userEmail = userEmail;
        }        

        public UUID getChatId() {
            return chatId;
        }

        public void setChatId(UUID chatId) {
            this.chatId = chatId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }
    }

}
