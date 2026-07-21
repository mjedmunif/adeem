package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.adeem.Enums.ConsultationType;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookAppointmentDTO {

    @NotNull(message = "Doctor id is required")
    private Long doctorId;

    @NotNull(message = "Appointment date and time are required")
    private LocalDateTime appointmentDate;

    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;
}