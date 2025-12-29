package com.takeone.backend.repository;

import com.takeone.backend.entity.PendingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {

    List<PendingMessage> findByRecipientIdOrderByCreatedAtAsc(Long recipientId);

    Optional<PendingMessage> findByRecipientIdAndMessageId(Long recipientId, String messageId);
}
