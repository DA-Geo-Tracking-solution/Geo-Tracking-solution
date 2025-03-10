package at.htlhl.geo_tracking_solution.repositories;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.htlhl.geo_tracking_solution.model.ChatMessage;
import at.htlhl.geo_tracking_solution.model.cassandra.ChatByUser;
import at.htlhl.geo_tracking_solution.model.cassandra.MessageByChat;
import at.htlhl.geo_tracking_solution.model.cassandra.ChatByUser.ChatByUserKey;
import at.htlhl.geo_tracking_solution.model.cassandra.MessageByChat.MessageByChatKey;

@Repository
public interface MessageByChatRepository extends CassandraRepository<MessageByChat, MessageByChatKey> {

    @Query("SELECT * FROM messages_by_chat WHERE chat_id = ?0")
    List<MessageByChat> getMessagesFromChat(UUID chatId);


    @Query("INSERT INTO messages_by_chat (chat_id, message_id, author_email, content, timestamp) VALUES (?0, ?1, ?2, ?3, ?4)  IF NOT EXISTS")
    boolean insertIfNotExist( UUID chatId, UUID messageId, String authorEmail, String content, Instant timestamp );
}
