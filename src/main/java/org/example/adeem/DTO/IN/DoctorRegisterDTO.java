package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorRegisterDTO {

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

    // بيانات الطبيب
    @NotBlank(message = "Specialty is required")
    private String specialty;

    @NotNull(message = "Price per session is required")
    @Positive(message = "Price must be positive")
    private BigDecimal pricePerSession;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String licenseDocumentUrl; // رابط الملف بعد الرفع (نتعامل معه لاحقاً برفع ملفات)

    private String bio;


    // ملاحظة: verificationStatus ما يجي من المستخدم أبداً
    // يكون PENDING دايماً بشكل افتراضي من الـ Service - نفس مبدأ الـ role بالـ User

}
