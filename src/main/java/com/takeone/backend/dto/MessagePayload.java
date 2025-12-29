package com.takeone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayload {
    private String messageId;
    private Long senderId;
    private Long recipientId;
    private String encryptedContent;
    private String type;
    private Long timestamp;
}
