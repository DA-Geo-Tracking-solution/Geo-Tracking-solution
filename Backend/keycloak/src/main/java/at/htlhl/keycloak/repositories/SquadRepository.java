package at.htlhl.keycloak.repositories;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.htlhl.keycloak.model.ChatByUser;
import at.htlhl.keycloak.model.ChatMessage;
import at.htlhl.keycloak.model.SquadData;
import at.htlhl.keycloak.model.ChatByUser.ChatByUserKey;
import at.htlhl.keycloak.model.MessageByChat.MessageByChatKey;

public interface SquadRepository extends CassandraRepository<SquadData, MessageByChatKey>{

    @Query("SELECT * FROM user_by_squad WHERE squad_id = ?0")
    List<SquadData> getUsers(UUID squadId);

    @Query("SELECT * FROM user_by_squad WHERE user_email = ?0")
    List<SquadData> getSquad(String user_email);


    @Query("SELECT squad_id FROM users_by_chat WHERE chat_id = ?0 LIMIT 1")
    Optional<UUID> doesSquadIdExist(UUID squadId);

    @Query("INSERT INTO user_by_squad (squad_id, user_email) VALUES (?0, ?1) IF NOT EXISTS")
    boolean insertIfNotExists(UUID squadId, String userEmail);

    // @Query("INSERT INTO messages_by_chat (chat_id, message_id, author_email, content, timestamp) VALUES (?0, ?1, ?2, ?3, ?4)  IF NOT EXISTS")
    // boolean insertIfNotExist( UUID chatId, UUID messageId, String authorEmail, String content, Instant timestamp );


    
}
