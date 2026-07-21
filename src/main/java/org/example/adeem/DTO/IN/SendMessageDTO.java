package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageDTO {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @NotBlank(message = "Message content is required")
    private String content;

    private String attachmentUrl; // اختياري - لرفع الصور لاحقاً
}