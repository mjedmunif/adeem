package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotAskDTO {

    @NotBlank(message = "Message content is required")
    private String content;

    private Long conversationId; // null = محادثة جديدة، غير null = استمرار محادثة موجودة
}