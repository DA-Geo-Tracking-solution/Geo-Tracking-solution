package at.htlhl.geo_tracking_solution.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.htlhl.geo_tracking_solution.model.ChatMessage;
import at.htlhl.geo_tracking_solution.model.MessageByChat;
import at.htlhl.geo_tracking_solution.repositories.MessageByChatRepository;

@Service
public class MessageService {
    
    @Autowired
    private MessageByChatRepository messageByChatRepository;

    public List<MessageByChat> getMessagesInChat(UUID chatId) {
        return messageByChatRepository.getMessagesFromChat(chatId);
    }

    public UUID createMessagesInChat(UUID chatId, String authorEmail, String content, Instant timestamp) {
        UUID messageId = UUID.randomUUID();
        while (!messageByChatRepository.insertIfNotExist(chatId, messageId, authorEmail, content, timestamp)){
            messageId = UUID.randomUUID();
        }
        return messageId;
    }

}
