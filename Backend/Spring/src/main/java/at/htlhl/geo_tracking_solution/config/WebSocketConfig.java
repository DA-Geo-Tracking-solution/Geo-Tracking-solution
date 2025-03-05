package at.htlhl.geo_tracking_solution.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import at.htlhl.geo_tracking_solution.service.ChatService;
import at.htlhl.geo_tracking_solution.service.GPSDataService;
import at.htlhl.geo_tracking_solution.service.GroupService;
import at.htlhl.geo_tracking_solution.service.SquadService;
import at.htlhl.geo_tracking_solution.service.UserService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private ChatService chatService;
    private SquadService squadService;

    @Autowired
    public WebSocketConfig(ChatService chatService, SquadService squadService) {
        this.chatService = chatService;
        this.squadService = squadService;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    onConnect(accessor);
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    onSubscribe(accessor);
                }

                return message;
            }
        });
    }

    private void onConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Invalid Authorisation-Header") {};
        }
    
        String token = authHeader.substring(7);
    
        try {
            Jwt jwt = jwtDecoder().decode(token);
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    
            if (sessionAttributes == null) {
                sessionAttributes = new HashMap<>();
                accessor.setSessionAttributes(sessionAttributes);
            }
    
            // JWT in den Sitzungsattributen speichern
            sessionAttributes.put("jwt", jwt);
    
            String userEmail = jwt.getClaims().get("email") instanceof String
                ? (String) jwt.getClaims().get("email")
                : jwt.getSubject();
    
            if (userEmail == null) {
                throw new AuthenticationException("E-Mail konnte nicht aus dem JWT extrahiert werden") {};
            }
    
            var auth = new UsernamePasswordAuthenticationToken(
                userEmail, 
                null,
                new KeycloakJwtAuthenticationConverter().convert(jwt).getAuthorities()
            );
    
            accessor.setUser(auth);
        
        } catch (JwtException e) {
            throw new AuthenticationException("Invalid JWT: " + e.getMessage()) {};
        }
    }

    private void onSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/geolocation/squad/")) {
            UUID squadId = UUID.fromString(destination.substring(25));

            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor
                    .getUser();
            if (!squadService.isUserInSquad(squadId, (String) auth.getPrincipal())) {
                throw new AuthenticationException("User not authorized to subscribe to " + destination) {
                };
            }
        } else if (destination != null && destination.startsWith("/topic/chat/")) {
            UUID chatId = UUID.fromString(destination.substring(12));

            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor
                    .getUser();
            if (!chatService.isUserInChat(chatId, (String) auth.getPrincipal())) {
                throw new AuthenticationException("User not authorized to subscribe to " + destination) {};
            }                         
        } else if (destination != null && destination.startsWith("/topic/chatCreation/")) {
            String userEmail = destination.substring(20);
            
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
            if (!userEmail.equals((String) auth.getPrincipal())) {
                throw new AuthenticationException("User not authorized to subscribe to " + destination) {};
            }                         
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/geo-tracking-solution")
                .setAllowedOriginPatterns("*");
        // .addInterceptors(new JwtHandshakeInterceptor(jwtDecoder())); // Allow all origins
        // .withSockJS(); // SockJS fallback
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Replace with your JwtDecoder logic, e.g., using Keycloak's public key to
        // decode the JWT
        return JwtDecoders.fromIssuerLocation("http://localhost:8081/realms/geo-tracking-solution");
    }

}
