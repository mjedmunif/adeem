package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.adeem.Enums.SenderType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatbotMessageResponseDTO {
    private Long conversationId;
    private SenderType senderType;
    private String content;
    private LocalDateTime createdAt;
}