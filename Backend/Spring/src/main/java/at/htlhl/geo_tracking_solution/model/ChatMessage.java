package at.htlhl.geo_tracking_solution.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessage {
    private UUID message_id;
    private String authorEmail;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public ChatMessage(UUID message_id, String authorEmail, String content, LocalDateTime timestamp) {
        this.message_id = message_id;
        this.authorEmail = authorEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    public UUID getMessage_id() {
        return message_id;
    }
    
    public void setMessage_id(UUID message_id) {
        this.message_id = message_id;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
