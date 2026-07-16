package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DoctorPublicResponseDTO {
    private Long id;
    private String fullName;
    private String specialty;
    private BigDecimal pricePerSession;
    private String bio;
    // بدون email، بدون phoneNumber، بدون licenseNumber
}