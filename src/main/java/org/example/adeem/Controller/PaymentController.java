package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIResponse;
import org.example.adeem.DTO.IN.ConfirmPaymentDTO;
import org.example.adeem.DTO.IN.InitiatePaymentDTO;
import org.example.adeem.DTO.OUT.PaymentResponseDTO;
import org.example.adeem.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ==================== بدء الدفع (المريض) ====================
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InitiatePaymentDTO dto) {

        return ResponseEntity.ok(paymentService.initiatePayment(userDetails.getUsername(), dto));
    }

    // ==================== تأكيد الدفع (مؤقتاً endpoint عام للاختبار - لاحقاً webhook من البوابة) ====================
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody ConfirmPaymentDTO dto) {
        paymentService.confirmPayment(dto.getTransactionReference());
        return ResponseEntity.ok(new APIResponse("Payment confirmed, appointment is now confirmed"));
    }
}