package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        private String fullName;

        @Pattern(regexp = "^05[0-9]{8}$", message = "Invalid phone number")
        private String phoneNumber;

        // role removed on purpose - never trust client input for authorization-related fields
    }

