package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.adeem.Enums.VerificationStatus;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DoctorResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String specialty;
    private BigDecimal pricePerSession;
    private String licenseNumber;
    private VerificationStatus verificationStatus;
    private String bio;
}