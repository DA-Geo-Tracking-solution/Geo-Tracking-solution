package at.htlhl.geo_tracking_solution.model.cassandra;

import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;
import java.util.UUID;

@Table("messages_by_chat")
public class MessageByChat {

    @PrimaryKey
    private MessageByChatKey key;

    @Column("author_email")
    private String authorEmail;
    @Column("content")
    private String content;

    public MessageByChat() { }

    public MessageByChat(MessageByChatKey key, String authorEmail, String content) {
        this.key = key;
        this.authorEmail = authorEmail;
        this.content = content;
    }
    
    // Getters and setters
    public MessageByChatKey getKey() {
        return key;
    }

    public void setKey(MessageByChatKey key) {
        this.key = key;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @PrimaryKeyClass
    public static class MessageByChatKey {

        @PrimaryKeyColumn(name = "chat_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID chatId;

        @PrimaryKeyColumn(name = "timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private Instant timestamp;

        @PrimaryKeyColumn(name = "message_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
        private UUID messageId;

        public MessageByChatKey() { }

        public MessageByChatKey(UUID chatId, Instant timestamp, UUID messageId) {
            this.chatId = chatId;
            this.timestamp = timestamp;
            this.messageId = messageId;
        }

        public UUID getChatId() {
            return chatId;
        }

        public void setChatId(UUID chatId) {
            this.chatId = chatId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }

    }

}
