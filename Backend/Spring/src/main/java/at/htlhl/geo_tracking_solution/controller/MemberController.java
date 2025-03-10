package at.htlhl.geo_tracking_solution.controller;

import io.swagger.v3.oas.annotations.Operation;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import at.htlhl.geo_tracking_solution.model.Chat;
import at.htlhl.geo_tracking_solution.model.Chat.Member;
import at.htlhl.geo_tracking_solution.model.cassandra.ChatByUser;
import at.htlhl.geo_tracking_solution.model.cassandra.GPSData;
import at.htlhl.geo_tracking_solution.model.cassandra.MessageByChat;
import at.htlhl.geo_tracking_solution.model.cassandra.UserByChat;
import at.htlhl.geo_tracking_solution.model.cassandra.UserBySquad;
import at.htlhl.geo_tracking_solution.model.cassandra.MessageByChat.MessageByChatKey;
import at.htlhl.geo_tracking_solution.model.keycloak.User;
import at.htlhl.geo_tracking_solution.service.ChatService;
import at.htlhl.geo_tracking_solution.service.GPSDataService;
import at.htlhl.geo_tracking_solution.service.MessageService;
import at.htlhl.geo_tracking_solution.service.SquadService;
import at.htlhl.geo_tracking_solution.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/member")
public class MemberController {

    private SquadService squadService;
    private UserService userService;
    private GPSDataService gpsDataService;
    private ChatService chatService;
    private MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MemberController(
            SquadService squadService,
            UserService userService,
            GPSDataService gpsDataService,
            ChatService chatService,
            MessageService messageService,
            SimpMessagingTemplate messagingTemplate) {
        this.squadService = squadService;
        this.userService = userService;
        this.gpsDataService = gpsDataService;
        this.chatService = chatService;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    // Todo only if in same group
    @GetMapping("/user/{userEmail}")
    @Operation(description = "Returns data of specific User (not only users in the same group)")
    public UserRepresentation getUserByEmail(@PathVariable("userEmail") String userEmail) throws ResponseStatusException{
        if (userService.isMember(userEmail)) {
            UserRepresentation user = userService.getUserByEmail(userEmail);
            return user;
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not in the same group");
        }
    }

    @PatchMapping("/user")
    @Operation(description = "Edits Userinformation in format { {username, userEmail, firstname, lastname} } of User")
    public String editUser(@RequestBody Map<String, Object> request) throws ResponseStatusException{
        User userModel = User.MapToUser((Map<String, Object>) request.get("user"));
        String message = userService.updateUser(userModel);
        return "{ \"message\": \"" + message + "\" }";
    }

    @GetMapping("/group")
    @Operation(description = "Returns the Group of a User")
    public String getGroup() throws ResponseStatusException{
        List<String> groupNames = userService.getUserGroups().stream()
            .map(GroupRepresentation::getName).collect(Collectors.toList());

        if (!groupNames.isEmpty()) {
            return "\"" + groupNames.get(0) + "\"";
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is in no Group");
        }
    }

    @GetMapping("/group-members")
    @Operation(description = "Returns a List of all members in the group of the requester in format { username, email, firstname, lastname }")
    public List<User> getGroupMembers() {
        List<UserRepresentation> userRepresentations = userService.getGroupMembers();
        List<User> users = new ArrayList<>();
        for (UserRepresentation userRep : userRepresentations) {
            users.add(new User(userRep));
        }
        return users;
    }

    @GetMapping("/squads")
    @Operation(description = "Returns all Squads of a User")
    public List<UserBySquad> getSquads() {
        return squadService.getSquadsFromUser(userService.getUserEmail());
    }

    @GetMapping("/squad-members-locations")
    @Operation(description = "Returns some users location data in format { userEmail, { longitude, latitude }, timestamp } of your group")
    public List<GPSData> getSquadMembersCoordinates(@RequestParam Instant earliestTime) {
        List<GPSData> userLocations = new ArrayList<>();
        List<String> userEmails = new ArrayList<>();
        
        for (UserBySquad squad : squadService.getSquadsFromUser(userService.getUserEmail())) {
            List<String> userEmailsInSquad = new ArrayList<>();
            for (UserBySquad user : squadService.getUsersInSquad(squad.getKey().getSquadId())) {
                String userEmail =  user.getKey().getUserEmail();
                if (!userEmails.contains(userEmail)) {
                    userLocations.addAll(gpsDataService.getGPSDataOf(userEmail, earliestTime));
                    if (!userEmailsInSquad.contains(userEmail)) {
                        userEmailsInSquad.add(userEmail);
                    }
                }
            }
            userEmails.addAll(userEmailsInSquad);
        }
        return userLocations;
    }

    @GetMapping("/chats")
    @Operation(description = "Returns all chats a user has in format { chatId, chatName, [member] }")
    public List<Chat> getChats() {
        // Todo better Performance
        String userEmail = userService.getUserEmail();
        List<ChatByUser> chatsByUser = chatService.getChatsFromUser(userEmail);

        ArrayList<Chat> chats = new ArrayList<>();

        for (ChatByUser chatByUser : chatsByUser) {
            List<UserByChat> usersByChat = chatService.getUsersInChat(chatByUser.getKey().getChatId());

            List<Member> members = new ArrayList<>();
            for (UserByChat userByChat : usersByChat) {
                String email = userByChat.getKey().getUserEmail();
                UserRepresentation user = userService.getUserByEmail(email);
                String name = (user != null) ? user.getUsername() : ("Unknown user with email: " + email);
                members.add(new Member(
                        name,
                        email));
            }

            chats.add(new Chat(
                    chatByUser.getKey().getChatId(),
                    chatByUser.getChatName(),
                    members));
        }

        return chats;
    }

    @PostMapping("/chat")
    @Operation(description = "Create a chat with format { chatName, [useremail] }")
    public String createChat(@RequestBody Map<String, Object> request) throws ResponseStatusException{
        List<String> userEmails = (List<String>) request.get("userEmails");
        if (userEmails == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmails do not exist");
        }
        if (!userEmails.contains(userService.getUserEmail())) {
            userEmails.add(userService.getUserEmail());
        }
        String chatName = (String) request.get("chatName");
        if (chatName == null) {
            chatName = "Unkown ChatName";
        }

        Chat chat = chatService.createChat(chatName, userEmails);
        for (String userEmail : userEmails) {
            messagingTemplate.convertAndSend("/topic/chatCreation/" + userEmail,  chat);
        }
        return "\"created Succesfully\"";

    }

    @PatchMapping("/chat/{chatId}/user")
    @Operation(description = "Puts a user in a chat in format { userEmail, chatName } as well if chat does not exist")
    public String putUserInChat(@PathVariable("chatId") UUID chatId, @RequestBody Map<String, String> request) throws ResponseStatusException{
        String userEmail = request.get("userEmail");
        if (userEmail == null || userService.getUserByEmail(userEmail) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail does not exist");
        }
        String chatName = request.get("chatName");
        if (chatName == null) {
            chatName = "Unkown ChatName";
        }
        
        Chat chat = chatService.putUserInChat(userEmail, chatId, chatName);
        messagingTemplate.convertAndSend("/topic/chatCreation/" + userEmail,  chat);
        return "\"updated Succesfully\"";
    }

    @GetMapping("chat/{chatId}/messages")
    @Operation(description = "Returns some messages in format { timestamp, message, author } of a chat")
    public Object getChatMessages(@PathVariable("chatId") UUID chatId) {
        List<MessageByChat> messageByChats = messageService.getMessagesInChat(chatId);
        return messageByChats;
    }

    @PostMapping("chat/{chatId}/message")
    @Operation(description = "Adds a message in format { message } to a chat")
    public Object addChatMessage(@PathVariable("chatId") UUID chatId, @RequestBody String message) {
        if (chatService.isUserInChat(chatId, userService.getUserEmail())) {
            Instant time = Instant.now();
            String authorEmail = userService.getUserEmail();
            UUID messageId = messageService.createMessagesInChat(chatId, authorEmail, message, time);
            messagingTemplate.convertAndSend("/topic/chat/" + chatId,  new MessageByChat((new MessageByChatKey(chatId, time, messageId )), authorEmail, message));
        } else {
            return "You are not in this chat: " + chatId;
        }
        return new ArrayList<>();
    }

}
