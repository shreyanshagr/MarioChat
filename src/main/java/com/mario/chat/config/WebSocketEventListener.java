package com.mario.chat.config;

import com.mario.chat.enums.MessageType;
import com.mario.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;

    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getSessionAttributes() != null) {
            String username = (String) headerAccessor.getSessionAttributes().get("username");

            if (username != null) {
                log.info("User disconnected: {}", username);

                ChatMessage leaveMessage = ChatMessage.builder()
                        .messageType(MessageType.LEAVE)
                        .sender(username)
                        .build();

                // Broadcast the leave message
                messageTemplate.convertAndSend("/topic/public", leaveMessage);
            }
        }
    }
}

