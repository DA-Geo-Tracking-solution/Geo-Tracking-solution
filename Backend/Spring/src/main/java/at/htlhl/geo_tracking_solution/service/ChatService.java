package at.htlhl.geo_tracking_solution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import at.htlhl.geo_tracking_solution.model.Chat;
import at.htlhl.geo_tracking_solution.model.Chat.Member;
import at.htlhl.geo_tracking_solution.model.cassandra.ChatByUser;
import at.htlhl.geo_tracking_solution.model.cassandra.UserByChat;
import at.htlhl.geo_tracking_solution.model.cassandra.UserByChat.UserByChatKey;
import at.htlhl.geo_tracking_solution.repositories.ChatByUserRepository;
import at.htlhl.geo_tracking_solution.repositories.UserByChatRepository;

@Service
public class ChatService {

    private final UserService userService;
    private final UserByChatRepository usersByChatRepository;
    private final ChatByUserRepository chatsByUserRepository;

    @Autowired
    public ChatService(UserService userService, UserByChatRepository usersByChatRepository, ChatByUserRepository chatsByUserRepository) {
        this.userService = userService;
        this.usersByChatRepository = usersByChatRepository;
        this.chatsByUserRepository = chatsByUserRepository;
    }
    
    public List<ChatByUser> getChatsFromUser(String userEmail) {
        return chatsByUserRepository.getChatsFromUser(userEmail);
    }

    public List<UserByChat> getUsersInChat(UUID chatId) {
        return usersByChatRepository.getUsersInChat(chatId);
    } 

    public boolean isUserInChat(UUID chatId, String userEmail) {
        return usersByChatRepository.isUserInChat(chatId, userEmail).size() == 1;
    }

    public Chat createChat(String chatName, List<String> userEmails) throws ResponseStatusException{
        UUID chatId = UUID.randomUUID();
        try {
            // Generate Unique chatID
            while (usersByChatRepository.doesChatIdExist(chatId).isPresent()) {
                chatId = UUID.randomUUID();
            }

            List<Member> members = new ArrayList<>();

            for (String userEmail: userEmails) {
                UserRepresentation user = userService.getUserByEmail(userEmail);
               
                if (user != null) {
                    usersByChatRepository.insertIfNotExists(chatId, userEmail);
                    chatsByUserRepository.insertIfNotExists(userEmail, chatId, chatName);
                    members.add(new Member(user.getUsername(), userEmail));
                }    
            }
            return new Chat(chatId, chatName, members);
        } catch (Exception e) {
            // Undo changes if one fails
            for (String userEmail: userEmails) {
                revertUsersByChat(userEmail, chatId);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create chat, changes rolled back." + e.getMessage());
        }
    }

    public Chat putUserInChat(String userEmail, UUID chatId, String chatName) throws ResponseStatusException{
        if (!isUserInChat(chatId, userService.getUserEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission Denied: You are not in this Chat");
        }
        try {
            // Inserts only if chatId exist else throws error
            if (!usersByChatRepository.doesChatIdExist(chatId).isPresent()) {
                throw new Exception();
            }
            usersByChatRepository.insertIfNotExists(chatId, userEmail);
            chatsByUserRepository.insertIfNotExists(userEmail, chatId, chatName);

            List<Member> members = new ArrayList<>();
            for (UserByChat userByChat: getUsersInChat(chatId)) {
                String memberEmail = userByChat.getKey().getUserEmail();
                UserRepresentation user = userService.getUserByEmail(memberEmail);
                members.add(new Member(user.getUsername(), memberEmail));
            }
            return new Chat(chatId, chatName, members);
        } catch (Exception e) {
            // Undo changes if one fails
            revertUsersByChat(userEmail, chatId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update chat associations, changes rolled back.");
        }
    }

    private void revertUsersByChat(String userEmail, UUID chatId) {
        UserByChatKey userByChatKey = new UserByChatKey(chatId, userEmail);
        usersByChatRepository.deleteById(userByChatKey);
    }    
}
