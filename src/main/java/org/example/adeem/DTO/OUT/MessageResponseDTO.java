package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private Long appointmentId;
    private Long senderId;
    private String senderName;
    private String content;
    private String attachmentUrl;
    private LocalDateTime sentAt;
    private boolean isRead;
}