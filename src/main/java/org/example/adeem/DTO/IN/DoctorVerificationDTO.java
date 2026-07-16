package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.adeem.Enums.VerificationStatus;

@Getter
@Setter
public class DoctorVerificationDTO {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status; // الأدمن يحدد: VERIFIED أو REJECTED
}