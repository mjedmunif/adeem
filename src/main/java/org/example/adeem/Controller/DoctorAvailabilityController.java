package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIResponse;
import org.example.adeem.DTO.IN.AvailabilityCreateDTO;
import org.example.adeem.DTO.OUT.AvailabilityResponseDTO;
import org.example.adeem.Service.DoctorAvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    // ==================== الطبيب يضيف يوم دوام ====================
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<?> add(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvailabilityCreateDTO dto) {

        availabilityService.addAvailability(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse("Availability added successfully"));
    }

    // ==================== الطبيب يشوف جدوله هو ====================
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public ResponseEntity<List<AvailabilityResponseDTO>> getMyAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(availabilityService.getMyAvailability(userDetails.getUsername()));
    }

    // ==================== المريض يشوف جدول طبيب معين (عام) ====================
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(availabilityService.getDoctorAvailability(doctorId));
    }

    // ==================== الطبيب يحذف يوم دوام ====================
    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        availabilityService.deleteAvailability(userDetails.getUsername(), id);
        return ResponseEntity.ok(new APIResponse("Availability deleted successfully"));
    }
}