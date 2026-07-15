package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.adeem.Enums.Role;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private LocalDateTime createdAt;
}