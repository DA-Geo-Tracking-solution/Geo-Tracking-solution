package at.htlhl.geo_tracking_solution.model.cassandra;

import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

@Table("chats_by_user")
public class ChatByUser {

    @PrimaryKey
    private ChatByUserKey key;

    @Column("chat_name")
    private String chatName;

    public ChatByUser() { }

    public ChatByUser(ChatByUserKey key, String chatName) {
        this.key = key;
        this.chatName = chatName;
    }
    

    // Getters and setters
    public ChatByUserKey getKey() {
        return key;
    }

    public void setKey(ChatByUserKey key) {
        this.key = key;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    @PrimaryKeyClass
    public static class ChatByUserKey {

        @PrimaryKeyColumn(name = "user_email", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String userEmail;

        @PrimaryKeyColumn(name = "chat_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private UUID chatId;

        public ChatByUserKey() { }

        public ChatByUserKey(String userEmail, UUID chatId) {
            this.userEmail = userEmail;
            this.chatId = chatId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public UUID getChatId() {
            return chatId;
        }

        public void setChatId(UUID chatId) {
            this.chatId = chatId;
        }
    }

}
