package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.esprit.projetintegre.dto.request.MessageRequest;
import tn.esprit.projetintegre.dto.response.MessageResponse;
import tn.esprit.projetintegre.services.MessageService;

/**
 * WebSocket controller for real-time messaging.
 *
 * Requires WebSocket configuration (not yet in the project).
 * Add the following dependency to pom.xml:
 *
 *   <dependency>
 *       <groupId>org.springframework.boot</groupId>
 *       <artifactId>spring-boot-starter-websocket</artifactId>
 *   </dependency>
 *
 * Then create a WebSocketConfig class:
 *
 *   @Configuration
 *   @EnableWebSocketMessageBroker
 *   public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
 *       @Override
 *       public void configureMessageBroker(MessageBrokerRegistry registry) {
 *           registry.enableSimpleBroker("/topic", "/queue");
 *           registry.setApplicationDestinationPrefixes("/app");
 *       }
 *       @Override
 *       public void registerStompEndpoints(StompEndpointRegistry registry) {
 *           registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
 *       }
 *   }
 */
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a message to a chat room.
     * Client subscribes to: /topic/room/{chatRoomId}
     * Client sends to:      /app/chat.sendToRoom
     */
    @MessageMapping("/chat.sendToRoom")
    public void sendToRoom(@Payload MessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        messagingTemplate.convertAndSend(
                "/topic/room/" + request.getChatRoomId(),
                response
        );
    }

    /**
     * Send a private message between two users.
     * Receiver subscribes to: /queue/messages (user-specific queue)
     * Client sends to:        /app/chat.sendPrivate
     */
    @MessageMapping("/chat.sendPrivate")
    public void sendPrivate(@Payload MessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        // Send to receiver's private queue
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                response
        );
        // Also send back to sender so they see their own message
        messagingTemplate.convertAndSendToUser(
                request.getSenderId().toString(),
                "/queue/messages",
                response
        );
    }

    /**
     * Broadcast a message to all subscribers of a topic.
     * Client subscribes to: /topic/broadcast
     * Client sends to:      /app/chat.broadcast
     */
    @MessageMapping("/chat.broadcast")
    @SendTo("/topic/broadcast")
    public MessageResponse broadcast(@Payload MessageRequest request) {
        return messageService.sendMessage(request);
    }
}
