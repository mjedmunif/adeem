package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIResponse;
import org.example.adeem.DTO.IN.DoctorRegisterDTO;
import org.example.adeem.DTO.IN.DoctorVerificationDTO;
import org.example.adeem.Service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // ==================== تسجيل طبيب جديد (مفتوح) ====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody DoctorRegisterDTO dto) {
        doctorService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse("Doctor registration submitted, pending verification"));
    }

    // ==================== بروفايلي أنا (الطبيب المسجل دخول) ====================
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getMyProfile(userDetails.getUsername()));
    }

    // ==================== للمرضى: تصفح الأطباء الموثقين ====================
    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedDoctors() {
        return ResponseEntity.ok(doctorService.getVerifiedDoctors());
    }

    // ==================== للأدمن: طلبات التوثيق المعلقة ====================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDoctors() {
        return ResponseEntity.ok(doctorService.getPendingDoctors());
    }

    // ==================== للأدمن: قبول أو رفض طبيب ====================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/verification")
    public ResponseEntity<?> updateVerification(
            @PathVariable Long id,
            @Valid @RequestBody DoctorVerificationDTO dto) {

        doctorService.updateVerificationStatus(id, dto);
        return ResponseEntity.ok(new APIResponse("Doctor verification status updated"));
    }
}