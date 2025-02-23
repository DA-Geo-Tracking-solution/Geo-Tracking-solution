package at.htlhl.geo_tracking_solution.repositories;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import at.htlhl.geo_tracking_solution.model.ChatByUser;
import at.htlhl.geo_tracking_solution.model.ChatByUser.ChatByUserKey;

@Repository
public interface ChatByUserRepository extends CassandraRepository<ChatByUser, ChatByUserKey> {

    @Query("SELECT * FROM chats_by_user WHERE user_email = ?0")
    List<ChatByUser> getChatsFromUser(String user_email);

    /*@Query("SELECT chat_id FROM chats_by_user WHERE chat_id = ?0 LIMIT 1")
    Optional<UUID> doesChatIdExist(UUID chatId);*/ // Allow DataFiltering needed


    @Query("INSERT INTO chats_by_user (user_email, chat_id, chat_name) VALUES (?0, ?1, ?2) IF NOT EXISTS")
    boolean insertIfNotExists(String userEmail, UUID chatId, String chatName);
}
