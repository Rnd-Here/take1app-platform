package com.takeone.backend.service;

import com.takeone.backend.dto.MessagePayload;
import com.takeone.backend.entity.PendingMessage;
import com.takeone.backend.repository.PendingMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageRelayService {

    private final PendingMessageRepository pendingMessageRepository;
    private final UserStatusService userStatusService;
    private final FirebaseService firebaseService;

    /**
     * Process an incoming message from the WebSocket.
     * Returns true if the message was delivered immediately (recipient online),
     * false if it was stored as pending.
     */
    @Transactional
    public boolean handleIncomingMessage(MessagePayload payload) {
        log.info("Relaying message {} from {} to {}", payload.getMessageId(), payload.getSenderId(),
                payload.getRecipientId());

        boolean isRecipientOnline = userStatusService.isUserOnline(payload.getRecipientId());

        if (isRecipientOnline) {
            // Logic to send via WebSocket will be in the Handler,
            // but we might still save it briefly or check for delivery ack.
            // For now, if online, we assume the handler will push it.
            return true;
        } else {
            // Recipient offline: Store in MySQL and send Push Notification
            savePendingMessage(payload);
            sendPushNotification(payload);
            return false;
        }
    }

    private void savePendingMessage(MessagePayload payload) {
        PendingMessage pending = PendingMessage.builder()
                .messageId(payload.getMessageId())
                .senderId(payload.getSenderId())
                .recipientId(payload.getRecipientId())
                .encryptedContent(payload.getEncryptedContent())
                .messageType(payload.getType())
                .createdAt(LocalDateTime.now())
                .build();
        pendingMessageRepository.save(pending);
        log.info("Stored pending message {} for offline user {}", payload.getMessageId(), payload.getRecipientId());
    }

    private void sendPushNotification(MessagePayload payload) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "NEW_MESSAGE");
        data.put("senderId", String.valueOf(payload.getSenderId()));
        data.put("messageId", payload.getMessageId());

        firebaseService.sendNotificationToUser(
                payload.getRecipientId(),
                "New Message",
                "You have a new encrypted message",
                data);
    }

    @Transactional
    public void markAsDelivered(Long recipientId, String messageId) {
        pendingMessageRepository.findByRecipientIdAndMessageId(recipientId, messageId)
                .ifPresent(msg -> {
                    pendingMessageRepository.delete(msg);
                    log.info("Message {} delivered to {}, removed from pending", messageId, recipientId);
                });
    }

    public List<PendingMessage> getPendingMessages(Long userId) {
        return pendingMessageRepository.findByRecipientIdOrderByCreatedAtAsc(userId);
    }
}
