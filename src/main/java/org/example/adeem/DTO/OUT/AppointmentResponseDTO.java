package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.adeem.Enums.AppointmentStatus;
import org.example.adeem.Enums.ConsultationType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Long id;
    private String doctorName;
    private String patientName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private ConsultationType consultationType;
    private String meetingLink;
}