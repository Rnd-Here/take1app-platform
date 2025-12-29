package com.takeone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WSMessage {
    private String type; // INIT, MESSAGE, DELIVERY_ACK, ERROR
    private String payload; // JSON string of the actual payload
}
