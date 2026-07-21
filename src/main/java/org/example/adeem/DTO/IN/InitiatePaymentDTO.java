package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiatePaymentDTO {
    @NotNull(message = "Appointment id is required")
    private Long appointmentId;
}
