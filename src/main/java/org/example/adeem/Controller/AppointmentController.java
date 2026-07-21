package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.DTO.IN.BookAppointmentDTO;
import org.example.adeem.API.APIResponse;
import org.example.adeem.DTO.OUT.AppointmentResponseDTO;
import org.example.adeem.Service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ==================== الـ slots الفاضية ليوم معين (عام) ====================
    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    // ==================== حجز موعد (مريض بس) ====================
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<?> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookAppointmentDTO dto) {

        appointmentService.bookAppointment(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse("Appointment booked, pending payment"));
    }

    // ==================== مواعيدي (مريض) ====================
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointmentsAsPatient(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(appointmentService.getMyAppointmentsAsPatient(userDetails.getUsername()));
    }

    // ==================== مواعيدي (طبيب) ====================
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor-appointments")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointmentsAsDoctor(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(appointmentService.getMyAppointmentsAsDoctor(userDetails.getUsername()));
    }

    // ==================== إلغاء موعد ====================
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        appointmentService.cancelAppointment(userDetails.getUsername(), id);
        return ResponseEntity.ok(new APIResponse("Appointment cancelled successfully"));
    }
}