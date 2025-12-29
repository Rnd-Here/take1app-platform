package com.takeone.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeone.backend.dto.MessagePayload;
import com.takeone.backend.dto.WSMessage;
import com.takeone.backend.service.MessageRelayService;
import com.takeone.backend.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelayHandler extends TextWebSocketHandler {

    // Map: UserId -> WebSocketSession
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final MessageRelayService messageRelayService;
    private final UserStatusService userStatusService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // userId should be passed via HandshakeInterceptor or as a query param
        Long userId = getUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            userStatusService.setUserOnline(userId);
            log.info("WebSocket connection established for user: {}", userId);

            // Push pending messages to the user immediately upon connection
            pushPendingMessages(userId, session);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WSMessage wsMsg = objectMapper.readValue(message.getPayload(), WSMessage.class);
        Long senderId = getUserId(session);

        if (senderId == null)
            return;

        switch (wsMsg.getType()) {
            case "MESSAGE" -> handleIncomingMessage(senderId, wsMsg.getPayload());
            case "DELIVERY_ACK" -> handleDeliveryAck(senderId, wsMsg.getPayload());
            default -> log.warn("Unknown message type: {}", wsMsg.getType());
        }
    }

    private void handleIncomingMessage(Long senderId, String payloadJson) throws IOException {
        MessagePayload payload = objectMapper.readValue(payloadJson, MessagePayload.class);
        payload.setSenderId(senderId);

        WebSocketSession recipientSession = sessions.get(payload.getRecipientId());

        if (recipientSession != null && recipientSession.isOpen()) {
            // Recipient online: Relay immediately
            WSMessage relayMsg = WSMessage.builder()
                    .type("MESSAGE")
                    .payload(payloadJson)
                    .build();
            recipientSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(relayMsg)));
            log.info("Relayed message {} directly to online recipient {}", payload.getMessageId(),
                    payload.getRecipientId());
        } else {
            // Recipient offline: Delegate to service (MySQL + Push)
            messageRelayService.handleIncomingMessage(payload);
        }
    }

    private void handleDeliveryAck(Long recipientId, String payloadJson) throws IOException {
        // Recipient of message sends ack back to server
        // Payload here is just the messageId (or a small object)
        Map<String, String> ackData = objectMapper.readValue(payloadJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                });
        String messageId = ackData.get("messageId");

        messageRelayService.markAsDelivered(recipientId, messageId);
    }

    private void pushPendingMessages(Long userId, WebSocketSession session) throws IOException {
        messageRelayService.getPendingMessages(userId).forEach(msg -> {
            try {
                MessagePayload payload = MessagePayload.builder()
                        .messageId(msg.getMessageId())
                        .senderId(msg.getSenderId())
                        .recipientId(msg.getRecipientId())
                        .encryptedContent(msg.getEncryptedContent())
                        .type(msg.getMessageType())
                        .timestamp(
                                msg.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .build();

                WSMessage wsMsg = WSMessage.builder()
                        .type("MESSAGE")
                        .payload(objectMapper.writeValueAsString(payload))
                        .build();

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wsMsg)));
            } catch (IOException e) {
                log.error("Failed to push pending message {} to user {}", msg.getMessageId(), userId, e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            sessions.remove(userId);
            userStatusService.setUserOffline(userId);
            log.info("WebSocket connection closed for user: {}. Status: {}", userId, status);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }
}
